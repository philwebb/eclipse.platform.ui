/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430873
 ******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;

/**
 * A viewer comparator is used by a {@link StructuredViewer} to
 * reorder the elements provided by its content provider.
 * <p>
 * The default <code>compare</code> method compares elements using two steps.
 * The first step uses the values returned from <code>category</code>.
 * By default, all elements are in the same category.
 * The second level uses strings obtained from the content viewer's label
 * provider via <code>ILabelProvider.getText()</code>.
 * The strings are compared using a comparator from {@link Policy#getComparator()}
 * which by default does a case sensitive string comparison.
 * </p>
 * <p>
 * Subclasses may implement the <code>isSorterProperty</code> method;
 * they may reimplement the <code>category</code> method to provide
 * categorization; and they may override the <code>compare</code> methods
 * to provide a totally different way of sorting elements.
 * </p>
 * @see IStructuredContentProvider
 * @see StructuredViewer
 *
 * @since 3.2
 */
public class ViewerComparator {
	/**
	 * The comparator to use to sort a viewer's contents.
	 */
	private Comparator comparator;

	/**
     * Creates a new {@link ViewerComparator}, which uses the default comparator
     * to sort strings.
	 *
	 */
	public ViewerComparator(){
		this(null);
	}

	/**
     * Creates a new {@link ViewerComparator}, which uses the given comparator
     * to sort strings.
     *
	 * @param comparator
	 */
	public ViewerComparator(Comparator comparator){
		this.comparator = comparator;
	}

	/**
	 * Returns the comparator used to sort strings.
	 *
	 * @return the comparator used to sort strings
	 */
	protected Comparator getComparator() {
		if (comparator == null){
			comparator = Policy.getComparator();
		}
		return comparator;
	}

    /**
     * Returns the category of the given element. The category is a
     * number used to allocate elements to bins; the bins are arranged
     * in ascending numeric order. The elements within a bin are arranged
     * via a second level sort criterion.
     * <p>
     * The default implementation of this framework method returns
     * <code>0</code>. Subclasses may reimplement this method to provide
     * non-trivial categorization.
     * </p>
     *
     * @param element the element
     * @return the category
     */
    public int category(Object element) {
        return 0;
    }

    /**
     * Returns a negative, zero, or positive number depending on whether
     * the first element is less than, equal to, or greater than
     * the second element.
     * <p>
     * The default implementation of this method is based on
     * comparing the elements' categories as computed by the <code>category</code>
     * framework method. Elements within the same category are further
     * subjected to a case insensitive compare of their label strings, either
     * as computed by the content viewer's label provider, or their
     * <code>toString</code> values in other cases. Subclasses may override.
     * </p>
     *
     * @param viewer the viewer
     * @param e1 the first element
     * @param e2 the second element
     * @return a negative number if the first element is less  than the
     *  second element; the value <code>0</code> if the first element is
     *  equal to the second element; and a positive number if the first
     *  element is greater than the second element
     */
    public int compare(Viewer viewer, Object e1, Object e2) {
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2) {
			return cat1 - cat2;
		}

        String name1 = getLabel(viewer, e1);
        String name2 = getLabel(viewer, e2);

        // use the comparator to compare the strings
        return getComparator().compare(name1, name2);
    }

	private String getLabel(Viewer viewer, Object e1) {
		String name1;
		if (viewer == null || !(viewer instanceof ContentViewer)) {
			name1 = e1.toString();
		} else {
			IBaseLabelProvider prov = ((ContentViewer) viewer)
					.getLabelProvider();
			if (prov instanceof ILabelProvider) {
				ILabelProvider lprov = (ILabelProvider) prov;
				name1 = lprov.getText(e1);
			} else {
				name1 = e1.toString();
			}
		}
		if (name1 == null) {
			name1 = "";//$NON-NLS-1$
		}
		return name1;
	}

    /**
     * Returns whether this viewer sorter would be affected
     * by a change to the given property of the given element.
     * <p>
     * The default implementation of this method returns <code>false</code>.
     * Subclasses may reimplement.
     * </p>
     *
     * @param element the element
     * @param property the property
     * @return <code>true</code> if the sorting would be affected,
     *    and <code>false</code> if it would be unaffected
     */
    public boolean isSorterProperty(Object element, String property) {
        return false;
    }

    /**
     * Sorts the given elements in-place, modifying the given array.
     * <p>
     * The default implementation of this method uses the
     * java.util.Arrays#sort algorithm on the given array,
     * calling <code>compare</code> to compare elements.
     * </p>
     * <p>
     * Subclasses may reimplement this method to provide a more optimized implementation.
     * </p>
     *
     * @param viewer the viewer
     * @param elements the elements to sort
     */
	public void sort(final Viewer viewer, Object[] elements) {
		try {
			Arrays.sort(elements, (a, b) -> ViewerComparator.this.compare(viewer, a, b));
		} catch (IllegalArgumentException e) {
			String msg = "Workaround for comparator violation:\n\t- set system property java.util.Arrays.useLegacyMergeSort=true\n\t- use a 1.6 JRE "  //$NON-NLS-1$
					+ "\nmessage: " + e.getLocalizedMessage() //$NON-NLS-1$
					+ "\nthis: " + getClass().getName() //$NON-NLS-1$
					+ "\ncomparator: " + (comparator != null ? comparator.getClass().getName() : null) //$NON-NLS-1$
					+ "\narray:"; //$NON-NLS-1$
			for (Object element : elements) {
				msg += "\n\t" + getLabel(viewer, element); //$NON-NLS-1$
			}
			Policy.getLog().log(new Status(IStatus.ERROR, "org.eclipse.jface", msg)); //$NON-NLS-1$
			throw e;
		}
	}
}
