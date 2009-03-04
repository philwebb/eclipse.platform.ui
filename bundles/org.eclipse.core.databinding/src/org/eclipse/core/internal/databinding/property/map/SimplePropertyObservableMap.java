/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @since 1.2
 */
public class SimplePropertyObservableMap extends AbstractObservableMap
		implements IPropertyObservable {
	private Object source;
	private SimpleMapProperty property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener listener;

	private Map cachedMap;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableMap(Realm realm, Object source,
			SimpleMapProperty property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	public Object getKeyType() {
		return property.getKeyType();
	}

	public Object getValueType() {
		return property.getValueType();
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	protected void firstListenerAdded() {
		if (!isDisposed()) {
			cachedMap = new HashMap(this);
			stale = false;

			if (listener == null) {
				listener = property
						.adaptListener(new ISimplePropertyListener() {
							public void handleEvent(SimplePropertyEvent event) {
								if (!isDisposed() && !updating) {
									if (event.type == SimplePropertyEvent.CHANGE) {
										modCount++;
										notifyIfChanged((MapDiff) event.diff);
									} else if (event.type == SimplePropertyEvent.STALE
											&& !stale) {
										stale = true;
										fireStale();
									}
								}
							}
						});
			}
			if (listener != null)
				listener.addTo(source);
		}
	}

	protected void lastListenerRemoved() {
		if (listener != null)
			listener.removeFrom(source);

		cachedMap.clear();
		cachedMap = null;
		stale = false;
	}

	// Queries

	private Map getMap() {
		return property.getMap(source);
	}

	// Single change operations

	private EntrySet es = new EntrySet();

	public Set entrySet() {
		getterCalled();
		return es;
	}

	private class EntrySet extends AbstractSet {
		public Iterator iterator() {
			return new EntrySetIterator();
		}

		public int size() {
			return getMap().size();
		}
	}

	private class EntrySetIterator implements Iterator {
		private volatile int expectedModCount = modCount;
		Map map = new HashMap(getMap());
		Iterator iterator = map.entrySet().iterator();
		Map.Entry last = null;

		public boolean hasNext() {
			getterCalled();
			checkForComodification();
			return iterator.hasNext();
		}

		public Object next() {
			getterCalled();
			checkForComodification();
			last = (Map.Entry) iterator.next();
			return last;
		}

		public void remove() {
			getterCalled();
			checkForComodification();

			iterator.remove(); // stay in sync
			MapDiff diff = Diffs.createMapDiffSingleRemove(last.getKey(), last
					.getValue());

			boolean wasUpdating = updating;
			updating = true;
			try {
				property.setMap(source, map, diff);
			} finally {
				updating = wasUpdating;
			}

			notifyIfChanged(null);

			last = null;
			expectedModCount = modCount;
		}

		private void checkForComodification() {
			if (expectedModCount != modCount)
				throw new ConcurrentModificationException();
		}
	}

	public Set keySet() {
		getterCalled();
		// AbstractMap depends on entrySet() to fulfil keySet() API, so all
		// getterCalled() and comodification checks will still be handled
		return super.keySet();
	}

	public Object put(Object key, Object value) {
		checkRealm();

		Map map = new HashMap(getMap());

		boolean add = !map.containsKey(key);

		Object oldValue = map.put(key, value);

		MapDiff diff;
		if (add)
			diff = Diffs.createMapDiffSingleAdd(key, value);
		else
			diff = Diffs.createMapDiffSingleChange(key, oldValue, value);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setMap(source, map, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return oldValue;
	}

	public void putAll(Map m) {
		checkRealm();

		Map map = new HashMap(getMap());

		Map oldValues = new HashMap();
		Map newValues = new HashMap();
		Set changedKeys = new HashSet();
		Set addedKeys = new HashSet();
		for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();
			Object key = entry.getKey();
			Object newValue = entry.getValue();
			if (map.containsKey(key)) {
				changedKeys.add(key);
				oldValues.put(key, map.get(key));
			} else {
				addedKeys.add(key);
			}
			map.put(key, newValue);

			newValues.put(key, newValue);
		}

		MapDiff diff = Diffs.createMapDiff(addedKeys, Collections.EMPTY_SET,
				changedKeys, oldValues, newValues);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setMap(source, map, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);
	}

	public Object remove(Object key) {
		checkRealm();
		return super.remove(key);
	}

	public Collection values() {
		getterCalled();
		// AbstractMap depends on entrySet() to fulfil values() API, so all
		// getterCalled() and comodification checks will still be handled
		return super.values();
	}

	private void notifyIfChanged(MapDiff diff) {
		if (hasListeners()) {
			Map oldMap = cachedMap;
			Map newMap = cachedMap = property.getMap(source);
			if (diff == null)
				diff = Diffs.computeMapDiff(oldMap, newMap);
			if (!diff.isEmpty() || stale) {
				stale = false;
				fireMapChange(diff);
			}
		}
	}

	public boolean isStale() {
		getterCalled();
		return stale;
	}

	public Object getObserved() {
		return source;
	}

	public IProperty getProperty() {
		return property;
	}

	public synchronized void dispose() {
		if (!isDisposed()) {
			if (listener != null)
				listener.removeFrom(source);
			property = null;
			source = null;
			listener = null;
			stale = false;
		}
		super.dispose();
	}
}
