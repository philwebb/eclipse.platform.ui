/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 272564
 *     Craig Foote (Footeware.ca) - https://bugs.eclipse.org/325743
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *     Cornel Izbasa <cizbasa@info.uvt.ro> - Bug 417447
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import java.util.HashSet;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.views.properties.PropertiesMessages;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.ShowInContext;

/**
 * Main class for the Property Sheet View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.PropertySheet"</code>.
 * </p>
 * <p>
 * Note that property <it>sheets</it> and property sheet pages are not the
 * same thing as property <it>dialogs</it> and their property pages (the property
 * pages extension point is for contributing property pages to property dialogs).
 * Within the property sheet view, all pages are <code>IPropertySheetPage</code>s.
 * </p>
 * <p>
 * Property sheet pages are discovered by the property sheet view automatically
 * when a part is first activated. The property sheet view asks the active part
 * for its property sheet page; this is done by invoking
 * <code>getAdapter(IPropertySheetPage.class)</code> on the part. If the part
 * returns a page, the property sheet view then creates the controls for that
 * property sheet page (using <code>createControl</code>), and adds the page to
 * the property sheet view. Whenever this part becomes active, its corresponding
 * property sheet page is shown in the property sheet view (which may or may not
 * be visible at the time). A part's property sheet page is discarded when the
 * part closes. The property sheet view has a default page (an instance of
 * <code>PropertySheetPage</code>) which services all parts without a property
 * sheet page of their own.
 * </p>
 * <p>
 * The workbench will automatically instantiates this class when a Property
 * Sheet view is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 *
 * @see IPropertySheetPage
 * @see PropertySheetPage
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertySheet extends PageBookView implements ISelectionListener, IShowInTarget, IShowInSource, IRegistryEventListener {
    /**
     * No longer used but preserved to avoid api change
     */
    public static final String HELP_CONTEXT_PROPERTY_SHEET_VIEW = IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW;

    /**
     * Extension point used to modify behavior of the view
     */
    private static final String EXT_POINT = "org.eclipse.ui.propertiesView"; //$NON-NLS-1$

    /**
     * The initial selection when the property sheet opens
     */
    private ISelection bootstrapSelection;

    /**
     * The current selection of the property sheet
     */
    private ISelection currentSelection;

    /**
     * The current part for which this property sheets is active
     */
	private IWorkbenchPart currentPart;

	/**
	 * Whether this property sheet instance is pinned or not
	 */
	private IAction pinPropertySheetAction;

	/**
	 * Set of workbench parts, which should not be used as a source for PropertySheet
	 */
	private HashSet ignoredViews;

    /**
     * Creates a property sheet view.
     */
    public PropertySheet() {
        super();
        pinPropertySheetAction = new PinPropertySheetAction();
        RegistryFactory.getRegistry().addListener(this, EXT_POINT);
    }

    @Override
	protected IPage createDefaultPage(PageBook book) {
		IPageBookViewPage page = (IPageBookViewPage) Adapters.adapt(this, IPropertySheetPage.class);
        if(page == null) {
        	page = new PropertySheetPage();
        }
        initPage(page);
        page.createControl(book);
        return page;
    }

	/**
     * The <code>PropertySheet</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    @Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        pinPropertySheetAction.addPropertyChangeListener(new IPropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (IAction.CHECKED.equals(event.getProperty())) {
					updateContentDescription();
				}
			}
		});
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(pinPropertySheetAction);

		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolBarManager.add(pinPropertySheetAction);

        getSite().getPage().getWorkbenchWindow().getWorkbench().getHelpSystem()
				.setHelp(getPageBook(),
						IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW);
    }

    @Override
	public void dispose() {
        // run super.
        super.dispose();

        // remove ourselves as a selection and registry listener
        getSite().getPage().removePostSelectionListener(this);
        RegistryFactory.getRegistry().removeListener(this);

        currentPart = null;
        currentSelection = null;
        pinPropertySheetAction = null;
    }

    @Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
        // Get a custom property sheet page but not if the part is also a
		// PropertySheet. In this case the child property sheet would
		// accidentally reuse the parent's property sheet page.
    	if(part instanceof PropertySheet) {
    		return null;
    	}
		IPropertySheetPage page = Adapters.adapt(part, IPropertySheetPage.class);
        if (page != null) {
            if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
            page.createControl(getPageBook());
            return new PageRec(part, page);
        }

        // Use the default page
        return null;
    }

    @Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IPropertySheetPage page = (IPropertySheetPage) rec.page;
        page.dispose();
        rec.dispose();
    }

    @Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page == null) {
			return null;
		}
		ISelection originalSel = page.getSelection();
		IWorkbenchPart activePart = page.getActivePart();
		if (activePart != null && activePart != this) {
			bootstrapSelection = originalSel;
			return activePart;
		}
		if (originalSel == null || originalSel.isEmpty()) {
			return null;
		}

		IEditorPart activeEditor = page.getActiveEditor();
		if (activeEditor != null && isImportant(activeEditor)) {
			ISelection selection = activeEditor.getSite().getSelectionProvider().getSelection();
			if (originalSel.equals(selection)) {
				bootstrapSelection = originalSel;
				return activeEditor;
			}
		}
		IViewReference[] viewrefs = page.getViewReferences();
		for (IViewReference ref : viewrefs) {
			IWorkbenchPart part = ref.getPart(false);
			if (part == null || part == this || !page.isPartVisible(part)) {
				continue;
			}
			if (!isImportant(part)) {
				continue;
			}
			ISelection selection = part.getSite().getSelectionProvider().getSelection();
			if (originalSel.equals(selection)) {
				bootstrapSelection = originalSel;
				return part;
			}
		}
		return null;
    }

    @Override
	public void init(IViewSite site) throws PartInitException {
   		site.getPage().addPostSelectionListener(this);
   		super.init(site);
    }

	@Override
	public void saveState(IMemento memento) {
		// close all but the primary/parent property sheet
		String secondaryId = getViewSite().getSecondaryId();
		if (null == secondaryId) {
			super.saveState(memento);
		} else {
			getViewSite().getPage().hideView(this);
		}
	}

    @Override
	protected boolean isImportant(IWorkbenchPart part) {
		// Don't interfere with other property views
    	String partID = part.getSite().getId();
		boolean isPropertyView = getSite().getId().equals(partID);
		return !isPinned() && !isPropertyView && !isViewIgnored(partID);
    }

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(currentPart)) {
			if (isPinned())
				pinPropertySheetAction.setChecked(false);
			currentPart = null;
		}
		super.partClosed(part);
	}

	@Override
	protected void partVisible(IWorkbenchPart part) {
	    super.partVisible(part);
	}

    @Override
	protected void partHidden(IWorkbenchPart part) {
    	// Explicitly ignore parts becoming hidden as this
    	// can cause issues when the Property View is maximized
    	// See bug 325743 for more details
    }

	/**
     * The <code>PropertySheet</code> implementation of this <code>IPartListener</code>
     * method first sees if the active part is an <code>IContributedContentsView</code>
     * adapter and if so, asks it for its contributing part.
     */
    @Override
	public void partActivated(IWorkbenchPart part) {
		IContributedContentsView view = Adapters.adapt(part, IContributedContentsView.class);
        IWorkbenchPart source = null;
        if (view != null) {
			source = view.getContributingPart();
		}
        if (source != null) {
			super.partActivated(source);
		} else {
			super.partActivated(part);
		}

        if(isImportant(part)) {
        	currentPart = part;
        	// reset the selection (to allow selectionChanged() accept part change for empty selections)
        	currentSelection = null;
        }

        // When the view is first opened, pass the selection to the page
        if (bootstrapSelection != null) {
            IPropertySheetPage page = (IPropertySheetPage) getCurrentPage();
            if (page != null) {
				page.selectionChanged(part, bootstrapSelection);
			}
            bootstrapSelection = null;
        }
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// we ignore selection if we are hidden OR selection is coming from
		// another source as the last one
		if (part == null || !part.equals(currentPart)) {
			return;
		}
		boolean visible = getSite() != null && getSite().getPage().isPartVisible(this);
		if (!visible) {
			return;
		}

		// we ignore null selection, or if we are pinned, or our own selection
		// or same selection
		if (sel == null || !isImportant(part) || sel.equals(currentSelection)) {
			return;
		}

        currentPart = part;
        currentSelection = sel;

        // pass the selection to the page
        IPropertySheetPage page = (IPropertySheetPage) getCurrentPage();
        if (page != null) {
			page.selectionChanged(currentPart, currentSelection);
		}

        updateContentDescription();
    }

	private void updateContentDescription() {
		if (isPinned() && currentPart != null) {
			setContentDescription(NLS.bind(PropertiesMessages.Selection_description, currentPart.getTitle()));
		} else {
			setContentDescription(""); //$NON-NLS-1$
		}
	}

    /**
	 * The <code>PropertySheet</code> implementation of this
	 * <code>PageBookView</code> method handles the <code>ISaveablePart</code>
	 * adapter case by calling <code>getSaveablePart()</code>.
	 *
	 * @since 3.2
	 */
	@Override
	protected Object getViewAdapter(Class key) {
		if (ISaveablePart.class.equals(key)) {
			return getSaveablePart();
		}
		return super.getViewAdapter(key);
	}

	/**
	 * Returns an <code>ISaveablePart</code> that delegates to the source part
	 * for the current page if it implements <code>ISaveablePart</code>, or
	 * <code>null</code> otherwise.
	 *
	 * @return an <code>ISaveablePart</code> or <code>null</code>
	 * @since 3.2
	 */
	protected ISaveablePart getSaveablePart() {
		IWorkbenchPart part = getCurrentContributingPart();
		if (part instanceof ISaveablePart) {
			return (ISaveablePart) part;
		}
		return null;
	}

	/**
	 * @return whether this property sheet is currently pinned
	 * @since 3.4
	 */
	public boolean isPinned() {
		return pinPropertySheetAction != null && pinPropertySheetAction.isChecked();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	@Override
	public ShowInContext getShowInContext() {
		return new PropertyShowInContext(currentPart, currentSelection);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	@Override
	public boolean show(ShowInContext aContext) {
		if (!isPinned()
				&& aContext instanceof PropertyShowInContext) {
			PropertyShowInContext context = (PropertyShowInContext) aContext;
			partActivated(context.getPart());
			selectionChanged(context.getPart(), context.getSelection());
			return true;
		}
		return false;
	}

	/***
	 * @param pinned Whether this sheet should be pinned
	 * @since 3.4
	 */
	public void setPinned(boolean pinned) {
		pinPropertySheetAction.setChecked(pinned);
		updateContentDescription();
	}

	private HashSet getIgnoredViews() {
		if (ignoredViews == null) {
			ignoredViews = new HashSet();
	        IExtensionRegistry registry = RegistryFactory.getRegistry();
	        IExtensionPoint ep = registry.getExtensionPoint(EXT_POINT);
			if (ep != null) {
				IExtension[] extensions = ep.getExtensions();
				for (IExtension extension : extensions) {
					IConfigurationElement[] elements = extension.getConfigurationElements();
					for (IConfigurationElement element : elements) {
						if ("excludeSources".equalsIgnoreCase(element.getName())) { //$NON-NLS-1$
							String id = element.getAttribute("id"); //$NON-NLS-1$
							if (id != null)
								ignoredViews.add(id);
						}
					}
				}
			}
		}
		return ignoredViews;
	}

	private boolean isViewIgnored(String partID) {
		return getIgnoredViews().contains(partID);
	}

	/**
	 * @see org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtension[])
	 * @since 3.5
	 */
	@Override
	public void added(IExtension[] extensions) {
		ignoredViews = null;
	}

	/**
	 * @see org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtensionPoint[])
	 * @since 3.5
	 */
	@Override
	public void added(IExtensionPoint[] extensionPoints) {
		ignoredViews = null;
	}

	/**
	 * @see org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtension[])
	 * @since 3.5
	 */
	@Override
	public void removed(IExtension[] extensions) {
		ignoredViews = null;
	}

	/**
	 * @see org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtensionPoint[])
	 * @since 3.5
	 */
	@Override
	public void removed(IExtensionPoint[] extensionPoints) {
		ignoredViews = null;
	}

}
