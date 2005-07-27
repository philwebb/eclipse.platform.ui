/******************************************************************************* * Copyright (c) 2005 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.ui.internal.keys;import java.io.IOException;import java.util.ArrayList;import java.util.Arrays;import java.util.Collection;import java.util.HashSet;import java.util.Iterator;import java.util.ResourceBundle;import java.util.Set;import org.eclipse.core.commands.CommandManager;import org.eclipse.core.commands.common.NamedHandleObject;import org.eclipse.core.commands.common.NamedHandleObjectComparator;import org.eclipse.core.commands.common.NotDefinedException;import org.eclipse.core.commands.contexts.ContextManager;import org.eclipse.core.runtime.IStatus;import org.eclipse.core.runtime.Status;import org.eclipse.jface.bindings.Binding;import org.eclipse.jface.bindings.BindingManager;import org.eclipse.jface.bindings.Scheme;import org.eclipse.jface.bindings.keys.KeySequence;import org.eclipse.jface.bindings.keys.KeySequenceText;import org.eclipse.jface.bindings.keys.KeyStroke;import org.eclipse.jface.bindings.keys.ParseException;import org.eclipse.jface.contexts.IContextIds;import org.eclipse.jface.dialogs.ErrorDialog;import org.eclipse.jface.dialogs.IDialogConstants;import org.eclipse.jface.dialogs.MessageDialog;import org.eclipse.jface.preference.PreferencePage;import org.eclipse.jface.viewers.ArrayContentProvider;import org.eclipse.jface.viewers.ComboViewer;import org.eclipse.jface.viewers.ITableLabelProvider;import org.eclipse.jface.viewers.ITreeContentProvider;import org.eclipse.jface.viewers.LabelProvider;import org.eclipse.jface.viewers.NamedHandleObjectLabelProvider;import org.eclipse.jface.viewers.StructuredSelection;import org.eclipse.jface.viewers.TreeViewer;import org.eclipse.jface.viewers.Viewer;import org.eclipse.jface.viewers.ViewerSorter;import org.eclipse.swt.SWT;import org.eclipse.swt.events.SelectionAdapter;import org.eclipse.swt.events.SelectionEvent;import org.eclipse.swt.graphics.Image;import org.eclipse.swt.graphics.Point;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Button;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.Menu;import org.eclipse.swt.widgets.MenuItem;import org.eclipse.swt.widgets.Text;import org.eclipse.swt.widgets.Tree;import org.eclipse.swt.widgets.TreeColumn;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;import org.eclipse.ui.contexts.IContextService;import org.eclipse.ui.internal.WorkbenchPlugin;import org.eclipse.ui.internal.util.Util;import org.eclipse.ui.keys.IBindingService;/** * <p> * A preference page that is capable of displaying and editing the bindings * between commands and user input events. These are typically things like * keyboard shortcuts. * </p> * <p> * This preference page has four general types of methods. Create methods are * called when the page is first made visisble. They are responsible for * creating all of the widgets, and laying them out within the preference page. * Fill methods populate the contents of the widgets that contain collections of * data from which items can be selected. The select methods respond to * selection events from the user, such as a button press or a table selection. * The update methods update the contents of various widgets based on the * current state of the user interface. For example, the command name label will * always try to match the current select in the binding table. * </p> *  * @since 3.2 */public final class NewKeysPreferencePage extends PreferencePage implements		IWorkbenchPreferencePage {	/**	 * <p>	 * A content provider that expects a collection of bindings.  All	 * </p>	 * <p>	 * In the case of an active grouping within the preference page, this	 * content provider also handles sorting the bindings into their respective	 * groups.	 * </p>	 */	private static final class BindingContentProvider implements			ITreeContentProvider {		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()		 */		public final void dispose() {			// Do nothing		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)		 */		public final Object[] getChildren(final Object parentElement) {			// TODO Blindly assuming a flat list for now.			return null;		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)		 */		public final Object[] getElements(final Object inputElement) {			if (inputElement instanceof Object[])				return (Object[]) inputElement;			if (inputElement instanceof Collection)				return ((Collection) inputElement).toArray();			return new Object[0];		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)		 */		public final Object getParent(final Object element) {			// TODO This blindly assumes a flat list.			return null;		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)		 */		public boolean hasChildren(Object element) {			// TODO This blindly assumes a flat list.			return false;		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,		 *      java.lang.Object, java.lang.Object)		 */		public final void inputChanged(final Viewer viewer,				final Object oldInput, final Object newInput) {			// Do nothing		}	}	/**	 * A label provider that simply extracts the command name and the formatted	 * trigger sequence from a given binding, and matches them to the correct	 * column.	 */	private static final class BindingLabelProvider extends LabelProvider			implements ITableLabelProvider {		/**		 * The index of the column containing the command name.		 */		private static final int COLUMN_COMMAND_NAME = 0;		/**		 * The index of the column containing the trigger sequence.		 */		private static final int COLUMN_TRIGGER_SEQUENCE = 1;		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object,		 *      int)		 */		public final Image getColumnImage(final Object element,				final int columnIndex) {			// TODO Should there be an image?			return null;		}		/*		 * (non-Javadoc)		 * 		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object,		 *      int)		 */		public final String getColumnText(final Object element,				final int columnIndex) {			final Binding binding = (Binding) element;			switch (columnIndex) {			case COLUMN_COMMAND_NAME:				try {					return binding.getParameterizedCommand().getName();				} catch (final NotDefinedException e) {					return null;				}			case COLUMN_TRIGGER_SEQUENCE:				return binding.getTriggerSequence().format();			default:				return null;			}		}	}	/**	 * Sorts the bindings in the filtered tree based on the current grouping.	 */	private final class BindingSorter extends ViewerSorter {		public final int category(final Object element) {			final Binding binding = (Binding) element;			switch (grouping) {			case GROUPING_CATEGORY:				// TODO This has to be done with something other than the hash.				try {					return binding.getParameterizedCommand().getCommand()							.getCategory().hashCode();				} catch (final NotDefinedException e) {					return 0;				}			case GROUPING_CONTEXT:				// TODO This has to be done with something other than the hash.				return binding.getContextId().hashCode();			case GROUPING_NONE:			default:				return 0;			}		}		public final int compare(final Viewer viewer, final Object a,				final Object b) {			final Binding x = (Binding) a;			final Binding y = (Binding) b;			return Util.compare(x.getParameterizedCommand(), y					.getParameterizedCommand());		}	}	/**	 * The constant value for <code>grouping</code> when the bindings should	 * be grouped by category.	 */	private static final int GROUPING_CATEGORY = 0;	/**	 * The constant value for <code>grouping</code> when the bindings should	 * be grouped by context.	 */	private static final int GROUPING_CONTEXT = 1;	/**	 * The constant value for <code>grouping</code> when the bindings should	 * not be grouped (i.e., they should be displayed in a flat list).	 */	private static final int GROUPING_NONE = 2;	/**	 * The number of items to show in the bindings table tree.	 */	private static final int ITEMS_TO_SHOW = 7;	/**	 * A comparator that can be used for display of	 * <code>NamedHandleObject</code> instances to the end user.	 */	private static final NamedHandleObjectComparator NAMED_HANDLE_OBJECT_COMPARATOR = new NamedHandleObjectComparator();	/**	 * The resource bundle from which translations can be retrieved.	 */	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle			.getBundle(NewKeysPreferencePage.class.getName());	/**	 * Sorts the given array of <code>NamedHandleObject</code> instances based	 * on their name. This is generally useful if they will be displayed to an	 * end users.	 * 	 * @param objects	 *            The objects to be sorted; must not be <code>null</code>.	 * @return The same array, but sorted in place; never <code>null</code>.	 */	private static final NamedHandleObject[] sortByName(			final NamedHandleObject[] objects) {		Arrays.sort(objects, NAMED_HANDLE_OBJECT_COMPARATOR);		return objects;	}	/**	 * The workbench's binding service. This binding service is used to access	 * the current set of bindings, and to persist changes.	 */	private IBindingService bindingService;	/**	 * The workbench's context service. This context service is used to access	 * the list of contexts.	 */	private IContextService contextService;	/**	 * The filtered tree containing the list of commands and bindings to edit.	 */	private DougsSuperFilteredTree filteredTree;	/**	 * The grouping for the bindings tree. Either there should be no group	 * (i.e., flat list), or the bindings should be grouped by either category	 * or context.	 */	private int grouping = GROUPING_NONE;	/**	 * A binding manager local to this preference page. When the page is	 * initialized, the current bindings are read out from the binding service	 * and placed in this manager. This manager is then updated as the user	 * makes changes. When the user has finished, the contents of this manager	 * are compared with the contents of the binding service. The changes are	 * then persisted.	 */	private BindingManager localChangeManager;	/**	 * The combo box containing the list of possible schemes to choose from.	 * This value is <code>null</code> until the contents are created.	 */	private ComboViewer schemeCombo = null;	/**	 * The combo box containing the list of possible contexts to choose from.	 * This value is <code>null</code> until the contents are create.	 */	private ComboViewer whenCombo = null;	/**	 * Creates the button bar across the bottom of the preference page. This	 * button bar contains the "Advanced..." button.	 * 	 * @param parent	 *            The composite in which the button bar should be placed; never	 *            <code>null</code>.	 * @return The button bar composite; never <code>null</code>.	 */	private final Control createButtonBar(final Composite parent) {		GridLayout layout;		GridData gridData;		int widthHint;		// Create the composite to house the button bar.		final Composite buttonBar = new Composite(parent, SWT.NONE);		layout = new GridLayout(1, false);		layout.marginWidth = 0;		buttonBar.setLayout(layout);		gridData = new GridData();		gridData.horizontalAlignment = SWT.END;		buttonBar.setLayoutData(gridData);		// Advanced button.		final Button advancedButton = new Button(buttonBar, SWT.PUSH);		gridData = new GridData();		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);		advancedButton.setText(Util.translateString(RESOURCE_BUNDLE,				"advancedButton.Text")); //$NON-NLS-1$		gridData.widthHint = Math.max(widthHint, advancedButton.computeSize(				SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;		advancedButton.setLayoutData(gridData);		return buttonBar;	}	/*	 * (non-Javadoc)	 * 	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)	 */	protected final Control createContents(final Composite parent) {		GridLayout layout = null;		// Creates a composite to hold all of the page contents.		final Composite page = new Composite(parent, SWT.NONE);		layout = new GridLayout(1, false);		layout.marginWidth = 0;		page.setLayout(layout);		createSchemeControls(page);		createTree(page);		createTreeControls(page);		createDataControls(page);		createButtonBar(page);		fill();		update();		return page;	}	private final Control createDataControls(final Composite parent) {		GridLayout layout;		GridData gridData;		// Creates the data area.		final Composite dataArea = new Composite(parent, SWT.NONE);		layout = new GridLayout(2, true);		layout.marginWidth = 0;		dataArea.setLayout(layout);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		dataArea.setLayoutData(gridData);		// LEFT DATA AREA		// Creates the left data area.		final Composite leftDataArea = new Composite(dataArea, SWT.NONE);		layout = new GridLayout(3, false);		leftDataArea.setLayout(layout);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.verticalAlignment = SWT.TOP;		gridData.horizontalAlignment = SWT.FILL;		leftDataArea.setLayoutData(gridData);		// The command name label.		final Label commandNameLabel = new Label(leftDataArea, SWT.NONE);		commandNameLabel.setText(Util.translateString(RESOURCE_BUNDLE,				"commandNameLabel.Text")); //$NON-NLS-1$); //$NON-NLS-1$		// The current command name.		final Label commandNameValueLabel = new Label(leftDataArea, SWT.NONE);		// TODO This should be update dynamically		commandNameValueLabel.setText("Word Completion"); //$NON-NLS-1$		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalSpan = 2;		gridData.horizontalAlignment = SWT.FILL;		commandNameValueLabel.setLayoutData(gridData);		// The binding label.		final Label bindingLabel = new Label(leftDataArea, SWT.NONE);		bindingLabel.setText(Util.translateString(RESOURCE_BUNDLE,				"bindingLabel.Text")); //$NON-NLS-1$		// The key sequence entry widget.		final Text bindingText = new Text(leftDataArea, SWT.BORDER);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		gridData.widthHint = 200;		bindingText.setLayoutData(gridData);		final KeySequenceText keySequenceText = new KeySequenceText(bindingText);		try {			keySequenceText.setKeySequence(KeySequence.getInstance("ALT+/")); //$NON-NLS-1$		} catch (final ParseException e) {			// TODO This should be done dynamically.		}		keySequenceText.setKeyStrokeLimit(4);		// Button for adding trapped key strokes		final Button addKeyButton = new Button(leftDataArea, SWT.LEFT				| SWT.ARROW);		addKeyButton.setToolTipText(Util.translateString(RESOURCE_BUNDLE,				"addKeyButton.ToolTipText")); //$NON-NLS-1$		gridData = new GridData();		gridData.heightHint = schemeCombo.getCombo().getTextHeight();		addKeyButton.setLayoutData(gridData);		// Arrow buttons aren't normally added to the tab list. Let's fix that.		final Control[] tabStops = dataArea.getTabList();		final ArrayList newTabStops = new ArrayList();		for (int i = 0; i < tabStops.length; i++) {			Control tabStop = tabStops[i];			newTabStops.add(tabStop);			if (bindingText.equals(tabStop)) {				newTabStops.add(addKeyButton);			}		}		final Control[] newTabStopArray = (Control[]) newTabStops				.toArray(new Control[newTabStops.size()]);		dataArea.setTabList(newTabStopArray);		// Construct the menu to attach to the above button.		final Menu addKeyMenu = new Menu(addKeyButton);		final Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();		while (trappedKeyItr.hasNext()) {			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();			final MenuItem menuItem = new MenuItem(addKeyMenu, SWT.PUSH);			menuItem.setText(trappedKey.format());			menuItem.addSelectionListener(new SelectionAdapter() {				public void widgetSelected(SelectionEvent e) {					keySequenceText.insert(trappedKey);					bindingText.setFocus();					bindingText.setSelection(bindingText.getTextLimit());				}			});		}		addKeyButton.addSelectionListener(new SelectionAdapter() {			public void widgetSelected(SelectionEvent selectionEvent) {				Point buttonLocation = addKeyButton.getLocation();				buttonLocation = dataArea.toDisplay(buttonLocation.x,						buttonLocation.y);				Point buttonSize = addKeyButton.getSize();				addKeyMenu.setLocation(buttonLocation.x, buttonLocation.y						+ buttonSize.y);				addKeyMenu.setVisible(true);			}		});		// The when label.		final Label whenLabel = new Label(leftDataArea, SWT.NONE);		whenLabel.setText(Util.translateString(RESOURCE_BUNDLE,				"whenLabel.Text")); //$NON-NLS-1$		// The when combo.		whenCombo = new ComboViewer(leftDataArea);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		gridData.horizontalSpan = 2;		whenCombo.getCombo().setLayoutData(gridData);		whenCombo.setLabelProvider(new NamedHandleObjectLabelProvider());		whenCombo.setContentProvider(new ArrayContentProvider());		// RIGHT DATA AREA		// Creates the right data area.		final Composite rightDataArea = new Composite(dataArea, SWT.NONE);		layout = new GridLayout(1, false);		rightDataArea.setLayout(layout);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.verticalAlignment = SWT.TOP;		gridData.horizontalAlignment = SWT.FILL;		rightDataArea.setLayoutData(gridData);		// The description label.		final Label descriptionLabel = new Label(rightDataArea, SWT.NONE);		descriptionLabel.setText(Util.translateString(RESOURCE_BUNDLE,				"descriptionLabel.Text")); //$NON-NLS-1$		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		descriptionLabel.setLayoutData(gridData);		// The description value.		final Label descriptionValueLabel = new Label(rightDataArea, SWT.WRAP);		// TODO This value should be updated dynamically.		descriptionValueLabel				.setText("Context insensitive completion monkey doo eats zim gir whadda whadda"); //$NON-NLS-1$		gridData = new GridData();		gridData.horizontalAlignment = SWT.FILL;		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalIndent = 30;		gridData.verticalIndent = 5;		gridData.widthHint = 200;		descriptionValueLabel.setLayoutData(gridData);		return dataArea;	}	private final Control createSchemeControls(final Composite parent) {		GridLayout layout;		GridData gridData;		int widthHint;		// Create a composite to hold the controls.		final Composite schemeControls = new Composite(parent, SWT.NONE);		layout = new GridLayout(3, false);		layout.marginWidth = 0;		schemeControls.setLayout(layout);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		schemeControls.setLayoutData(gridData);		// Create the label.		final Label schemeLabel = new Label(schemeControls, SWT.NONE);		schemeLabel.setText(Util.translateString(RESOURCE_BUNDLE,				"schemeLabel.Text")); //$NON-NLS-1$		// Create the combo.		schemeCombo = new ComboViewer(schemeControls);		schemeCombo.setLabelProvider(new NamedHandleObjectLabelProvider());		schemeCombo.setContentProvider(new ArrayContentProvider());		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		schemeCombo.getCombo().setLayoutData(gridData);		// Create the delete button.		final Button deleteSchemeButton = new Button(schemeControls, SWT.PUSH);		gridData = new GridData();		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);		deleteSchemeButton.setText(Util.translateString(RESOURCE_BUNDLE,				"deleteSchemeButton.Text")); //$NON-NLS-1$		gridData.widthHint = Math.max(widthHint, deleteSchemeButton				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;		deleteSchemeButton.setLayoutData(gridData);		return schemeControls;	}	private final Control createTree(final Composite parent) {		filteredTree = new DougsSuperFilteredTree(parent, SWT.SINGLE				| SWT.FULL_SELECTION | SWT.BORDER);		final GridLayout layout = new GridLayout(2, false);		layout.marginWidth = 0;		filteredTree.setLayout(layout);		final GridData gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.grabExcessVerticalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		gridData.verticalAlignment = SWT.FILL;		// Create a maximum height that is ITEMS_TO_SHOW items high.		final Tree tree = filteredTree.getViewer().getTree();		final int itemHeight = tree.getItemHeight();		if (itemHeight > 1) {			gridData.heightHint = ITEMS_TO_SHOW * itemHeight;		}		filteredTree.setLayoutData(gridData);		// Create the columns for the tree.		final TreeColumn commandNameColumn = new TreeColumn(tree, SWT.LEFT,				BindingLabelProvider.COLUMN_COMMAND_NAME);		commandNameColumn.setText(Util.translateString(RESOURCE_BUNDLE,				"commandNameColumn.Text")); //$NON-NLS-1$		final TreeColumn triggerSequenceColumn = new TreeColumn(tree, SWT.LEFT,				BindingLabelProvider.COLUMN_TRIGGER_SEQUENCE);		triggerSequenceColumn.setText(Util.translateString(RESOURCE_BUNDLE,				"triggerSequenceColumn.Text")); //$NON-NLS-1$		// Set up the providers for the viewer.		final TreeViewer viewer = filteredTree.getViewer();		viewer.setLabelProvider(new BindingLabelProvider());		viewer.setContentProvider(new BindingContentProvider());		viewer.setSorter(new BindingSorter());		return filteredTree;	}	private final Control createTreeControls(final Composite parent) {		GridLayout layout;		GridData gridData;		int widthHint;		// Creates controls related to the tree.		final Composite treeControls = new Composite(parent, SWT.NONE);		layout = new GridLayout(2, false);		layout.marginWidth = 0;		treeControls.setLayout(layout);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		treeControls.setLayoutData(gridData);		// Create the show all check box.		final Button showAllCheckBox = new Button(treeControls, SWT.CHECK);		gridData = new GridData();		gridData.grabExcessHorizontalSpace = true;		gridData.horizontalAlignment = SWT.FILL;		showAllCheckBox.setLayoutData(gridData);		showAllCheckBox.setText(Util.translateString(RESOURCE_BUNDLE,				"showAllCheckBox.Text")); //$NON-NLS-1$		// Create the delete binding button.		final Button deleteBindingButton = new Button(treeControls, SWT.PUSH);		gridData = new GridData();		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);		deleteBindingButton.setText(Util.translateString(RESOURCE_BUNDLE,				"deleteBindingButton.Text")); //$NON-NLS-1$		gridData.widthHint = Math.max(widthHint, deleteBindingButton				.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;		deleteBindingButton.setLayoutData(gridData);		return treeControls;	}	/**	 * Copies all of the information from the workbench into a local change	 * manager, and then the local change manager is used to populate the	 * contents of the various widgets on the page.	 * 	 * The widgets affected by this method are: scheme combo, bindings	 * table/tree model, and the when combo.	 */	private final void fill() {		// Make an internal binding manager to track changes.		localChangeManager = new BindingManager(new ContextManager(),				new CommandManager());		final Scheme[] definedSchemes = bindingService.getDefinedSchemes();		try {			for (int i = 0; i < definedSchemes.length; i++) {				final Scheme scheme = definedSchemes[i];				final Scheme copy = localChangeManager						.getScheme(scheme.getId());				copy.define(scheme.getName(), scheme.getDescription(), scheme						.getParentId());			}			localChangeManager					.setActiveScheme(bindingService.getActiveScheme());		} catch (final NotDefinedException e) {			throw new Error(					"There is a programmer error in the keys preference page"); //$NON-NLS-1$		}		localChangeManager.setLocale(bindingService.getLocale());		localChangeManager.setPlatform(bindingService.getPlatform());		localChangeManager.setBindings(bindingService.getBindings());		// Update the scheme combo.		schemeCombo				.setInput(sortByName(localChangeManager.getDefinedSchemes()));		setScheme(localChangeManager.getActiveScheme());		// Update the table tree.		final TreeViewer viewer = filteredTree.getViewer();		viewer.setInput(localChangeManager				.getActiveBindingsDisregardingContextFlat());		final TreeColumn[] columns = viewer.getTree().getColumns();		for (int i = 0; i < columns.length; i++) {			columns[i].pack();		}		// Update the when combo.		whenCombo.setInput(sortByName(contextService.getDefinedContexts()));	}	/*	 * (non-Javadoc)	 * 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)	 */	public final void init(final IWorkbench workbench) {		bindingService = (IBindingService) workbench				.getAdapter(IBindingService.class);		contextService = (IContextService) workbench				.getAdapter(IContextService.class);	}	/**	 * Logs the given exception, and opens an error dialog saying that something	 * went wrong. The exception is assumed to have something to do with the	 * preference store.	 * 	 * @param exception	 *            The exception to be logged; must not be <code>null</code>.	 */	private final void logPreferenceStoreException(final Throwable exception) {		final String message = Util.translateString(RESOURCE_BUNDLE,				"PreferenceStoreError.Message"); //$NON-NLS-1$		final String title = Util.translateString(RESOURCE_BUNDLE,				"PreferenceStoreError.Title"); //$NON-NLS-1$		String exceptionMessage = exception.getMessage();		if (exceptionMessage == null) {			exceptionMessage = message;		}		final IStatus status = new Status(IStatus.ERROR,				WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);		WorkbenchPlugin.log(message, status);		ErrorDialog.openError(schemeCombo.getCombo().getShell(), title,				message, status);	}	protected final void performDefaults() {		// Ask the user to confirm		final String title = Util.translateString(RESOURCE_BUNDLE,				"restoreDefaultsMessageBoxText"); //$NON-NLS-1$		final String message = Util.translateString(RESOURCE_BUNDLE,				"restoreDefaultsMessageBoxMessage"); //$NON-NLS-1$		final boolean confirmed = MessageDialog.openConfirm(getShell(), title,				message);		if (confirmed) {			// Fix the scheme in the local changes.			final String defaultSchemeId = bindingService.getDefaultSchemeId();			final Scheme defaultScheme = localChangeManager					.getScheme(defaultSchemeId);			try {				localChangeManager.setActiveScheme(defaultScheme);			} catch (final NotDefinedException e) {				// At least we tried....			}			// Fix the bindings in the local changes.			final Binding[] currentBindings = localChangeManager.getBindings();			final int currentBindingsLength = currentBindings.length;			final Set trimmedBindings = new HashSet();			for (int i = 0; i < currentBindingsLength; i++) {				final Binding binding = currentBindings[i];				if (binding.getType() != Binding.USER) {					trimmedBindings.add(binding);				}			}			final Binding[] trimmedBindingArray = (Binding[]) trimmedBindings					.toArray(new Binding[trimmedBindings.size()]);			localChangeManager.setBindings(trimmedBindingArray);			// Apply the changes.			try {				bindingService.savePreferences(defaultScheme,						trimmedBindingArray);			} catch (final IOException e) {				logPreferenceStoreException(e);			}		}		setScheme(localChangeManager.getActiveScheme());		super.performDefaults();	}	public final boolean performOk() {		// Save the preferences.		try {			bindingService.savePreferences(					localChangeManager.getActiveScheme(), localChangeManager							.getBindings());		} catch (final IOException e) {			logPreferenceStoreException(e);		}		return super.performOk();	}	/**	 * Sets the currently selected scheme. Setting the scheme always triggers an	 * update of the underlying widgets.	 * 	 * @param scheme	 *            The scheme to select; may be <code>null</code>.	 */	private final void setScheme(final Scheme scheme) {		schemeCombo.setSelection(new StructuredSelection(scheme));	}	/**	 * Updates all of the controls on this preference page in response to a user	 * interaction.	 */	private final void update() {		updateSchemeCombo();		updateWhenCombo();	}	private final void updateSchemeCombo() {	}	private final void updateWhenCombo() {		// TODO This should be updated based on the active context		whenCombo.setSelection(new StructuredSelection(contextService				.getContext(IContextIds.CONTEXT_ID_WINDOW)), true);	}}