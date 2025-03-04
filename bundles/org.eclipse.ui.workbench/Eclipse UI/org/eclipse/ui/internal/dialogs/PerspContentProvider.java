/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPerspectiveRegistry;

public class PerspContentProvider implements IStructuredContentProvider {

    /**
     * Create a new <code>PerspContentProvider</code>.
     */
    public PerspContentProvider() {
        //no-op
    }

    @Override
	public void dispose() {
        //no-op
    }

    @Override
	public Object[] getElements(Object element) {
        if (element instanceof IPerspectiveRegistry) {
            return ((IPerspectiveRegistry) element).getPerspectives();
        }
        return null;
    }

    @Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        //no-op
    }
}
