/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts.api;

import java.util.List;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IContextRegistry {

	/**
	 * Registers an IContextRegistryListener instance with this context registry.
	 *
	 * @param contextRegistryListener the IContextRegistryListener instance to register.
	 */
	void addContextRegistryListener(IContextRegistryListener contextRegistryListener);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	List getContextDefinitions();

	/**
	 * Unregisters an IContextRegistryListener instance with this context registry.
	 *
	 * @param contextRegistryListener the IContextRegistryListener instance to unregister.
	 */
	void removeContextRegistryListener(IContextRegistryListener contextRegistryListener);
}
