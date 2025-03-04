 /****************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *     Markus Schorn (Wind River Systems) -  bug 284447
 *     James Blackburn (Broadcom Corp.)   -  bug 340978
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 458832
 *     Christian Georgi (SAP SE)          -  bug 458811
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.ide.dialogs.ResourceEncodingFieldEditor;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.LineDelimiterEditor;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The IDEWorkspacePreferencePage is the page used to set IDE-specific preferences settings
 * related to workspace.

 *Note:This class extends from PreferencePage,and there's no WorkspacePreferencePage class.
 *Hence when the IDE settings doesn't appear in this preference page, this page will be empty.
 */
public class IDEWorkspacePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IEclipseContext e4Context;

	private Button autoBuildButton;

    private Button autoSaveAllButton;

    private IntegerFieldEditor saveInterval;

	private FieldEditor workspaceName;
	private Button showLocationInWindowTitle;

	private Button autoRefreshButton;

	private Button lightweightRefreshButton;

	private Button closeUnrelatedProjectButton;

	private ResourceEncodingFieldEditor encodingEditor;

	private LineDelimiterEditor lineSeparatorEditor;

    //A boolean to indicate if the user settings were cleared.
	private boolean clearUserSettings = false;

	private RadioGroupFieldEditor openReferencesEditor;

	private StringFieldEditor systemExplorer;

    @Override
	protected Control createContents(Composite parent) {

    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IIDEHelpContextIds.WORKSPACE_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

		PreferenceLinkArea area = new PreferenceLinkArea(composite, SWT.NONE,
				"org.eclipse.ui.preferencePages.Startup", IDEWorkbenchMessages.IDEWorkspacePreference_relatedLink,//$NON-NLS-1$
				(IWorkbenchPreferenceContainer) getContainer(),null);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		area.getControl().setLayoutData(data);

		createSpace(composite);
        createAutoBuildPref(composite);
        createAutoRefreshControls(composite);
        createSaveAllBeforeBuildPref(composite);
        createCloseUnrelatedProjPrefControls(composite);

		createSpace(composite);
		createSaveIntervalGroup(composite);
		createWindowTitleGroup(composite);

		createSpace(composite);
		createWorkspaceLocationGroup(composite);

		createSpace(composite);
		createOpenPrefControls(composite);

		createSpace(composite);
		createSystemExplorerGroup(composite);
		createSpace(composite);

		Composite lower = new Composite(composite,SWT.NONE);
		GridLayout lowerLayout = new GridLayout();
		lowerLayout.marginWidth = 0;
		lowerLayout.numColumns = 2;
		lowerLayout.makeColumnsEqualWidth = true;
		lower.setLayout(lowerLayout);

		lower.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		createEncodingEditorControls(lower);
		createLineSeparatorEditorControls(lower);
		applyDialogFont(composite);

        return composite;
    }

    /**
     * Creates controls for the preference to open required projects when
     * opening a project.
	 * @param parent The parent control
	 */
	private void createOpenPrefControls(Composite parent) {
		String name = IDEInternalPreferences.OPEN_REQUIRED_PROJECTS;
		String label = IDEWorkbenchMessages.IDEWorkspacePreference_openReferencedProjects;
        String[][] namesAndValues = {
                { IDEWorkbenchMessages.Always, IDEInternalPreferences.PSPM_ALWAYS },
                { IDEWorkbenchMessages.Never, IDEInternalPreferences.PSPM_NEVER },
                { IDEWorkbenchMessages.Prompt, IDEInternalPreferences.PSPM_PROMPT } };
		openReferencesEditor = new RadioGroupFieldEditor(name, label, 3, namesAndValues, parent, true);
		openReferencesEditor.setPreferenceStore(getIDEPreferenceStore());
		openReferencesEditor.setPage(this);
		openReferencesEditor.load();
	}
	/**
     * Creates controls for the preference to close unrelated projects.
	 * @param parent The parent control
	 */
	private void createCloseUnrelatedProjPrefControls(Composite parent) {
		closeUnrelatedProjectButton = new Button(parent, SWT.CHECK);
		closeUnrelatedProjectButton.setText(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_AlwaysCloseWithoutPrompt);
		closeUnrelatedProjectButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_closeUnrelatedProjectsToolTip);
		closeUnrelatedProjectButton.setSelection(getIDEPreferenceStore().getBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS));
	}

	protected void createSaveAllBeforeBuildPref(Composite composite) {
        autoSaveAllButton = new Button(composite, SWT.CHECK);
        autoSaveAllButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_savePriorToBuilding);
        autoSaveAllButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_savePriorToBuildingToolTip);
        autoSaveAllButton.setSelection(getIDEPreferenceStore().getBoolean(
                IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
    }

    protected void createAutoBuildPref(Composite composite) {
        autoBuildButton = new Button(composite, SWT.CHECK);
        autoBuildButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_autobuild);
        autoBuildButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_autobuildToolTip);
        autoBuildButton.setSelection(ResourcesPlugin.getWorkspace()
                .isAutoBuilding());
    }

	/**
	 * Create a composite that contains entry fields specifying the workspace
	 * location.
	 *
	 * @param composite
	 *            the Composite the group is created in.
	 */
	private void createWorkspaceLocationGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupComposite);

		// true workspace location
		Label locationLabel = new Label(groupComposite, SWT.NONE);
		locationLabel.setText(IDEWorkbenchMessages.IDEWorkspacePreference_workspaceLocation);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(locationLabel);
		Text workspacePath = new Text(groupComposite, SWT.READ_ONLY);
		workspacePath.setBackground(workspacePath.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		workspacePath.setText(TextProcessor.process(Platform.getLocation().toOSString()));
		workspacePath.setSelection(workspacePath.getText().length());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
				.hint(convertHorizontalDLUsToPixels(200), SWT.DEFAULT).applyTo(workspacePath);

		// show workspace location in window title
		boolean showLocationIsSetOnCommandLine = e4Context.containsKey(E4Workbench.FORCED_SHOW_LOCATION);
		showLocationInWindowTitle = new Button(groupComposite, SWT.CHECK);
		showLocationInWindowTitle.setText(IDEWorkbenchMessages.IDEWorkspacePreference_showLocationInWindowTitle);
		showLocationInWindowTitle.setSelection(showLocationIsSetOnCommandLine
				|| getIDEPreferenceStore().getBoolean(IDEInternalPreferences.SHOW_LOCATION));
		showLocationInWindowTitle.setEnabled(!showLocationIsSetOnCommandLine);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(showLocationInWindowTitle);

		if (showLocationIsSetOnCommandLine) {
			Composite noteComposite = createNoteComposite(composite.getFont(), groupComposite,
					WorkbenchMessages.Preference_note,
					IDEWorkbenchMessages.IDEWorkspacePreference_showLocationInWindowTitle_lockedByCommandLine);
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(noteComposite);
		}
	}

    /**
     * Create a composite that contains entry fields specifying save interval
     * preference.
     *
     * @param composite the Composite the group is created in.
     */
    private void createSaveIntervalGroup(Composite composite) {
        Composite groupComposite = new Composite(composite, SWT.LEFT);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        groupComposite.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        groupComposite.setLayoutData(gd);

        saveInterval = new IntegerFieldEditor(
                IDEInternalPreferences.SAVE_INTERVAL, IDEWorkbenchMessages.WorkbenchPreference_saveInterval,
                groupComposite);

        // @issue we should drop our preference constant and let clients use
        // core's pref. ours is not up-to-date anyway if someone changes this
        // interval directly thru core api.
        saveInterval.setPreferenceStore(getIDEPreferenceStore());
        saveInterval.setPage(this);
        saveInterval.setTextLimit(Integer.toString(
                IDEInternalPreferences.MAX_SAVE_INTERVAL).length());
        saveInterval.setErrorMessage(NLS.bind(IDEWorkbenchMessages.WorkbenchPreference_saveIntervalError, new Integer(IDEInternalPreferences.MAX_SAVE_INTERVAL)));
        saveInterval
                .setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        saveInterval.setValidRange(1, IDEInternalPreferences.MAX_SAVE_INTERVAL);

        IWorkspaceDescription description = ResourcesPlugin.getWorkspace()
                .getDescription();
        long interval = description.getSnapshotInterval() / 60000;
        saveInterval.setStringValue(Long.toString(interval));

        saveInterval.setPropertyChangeListener(event -> {
		    if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(saveInterval.isValid());
			}
		});

    }

    /**
     * Create a composite that contains entry fields specifying the workspace name
     * preference.
     *
     * @param composite the Composite the group is created in.
     */
    private void createWindowTitleGroup(Composite composite) {
        Composite groupComposite = new Composite(composite, SWT.LEFT);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        groupComposite.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        groupComposite.setLayoutData(gd);

        workspaceName = new StringFieldEditor(
                IDEInternalPreferences.WORKSPACE_NAME, IDEWorkbenchMessages.IDEWorkspacePreference_workspaceName,
                groupComposite);

        workspaceName.setPreferenceStore(getIDEPreferenceStore());
        workspaceName.load();
        workspaceName.setPage(this);
    }

	/**
	 * Create the Refresh controls
	 *
	 * @param parent
	 */
    private void createAutoRefreshControls(Composite parent) {

        this.autoRefreshButton = new Button(parent, SWT.CHECK);
        this.autoRefreshButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_RefreshButtonText);
        this.autoRefreshButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_RefreshButtonToolTip);

        this.lightweightRefreshButton = new Button(parent, SWT.CHECK);
        this.lightweightRefreshButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_RefreshLightweightButtonText);
        this.lightweightRefreshButton.setToolTipText(IDEWorkbenchMessages.IDEWorkspacePreference_RefreshLightweightButtonToolTip);

		boolean lightweightRefresh = Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES,
				ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false, null);
		boolean autoRefresh = Platform.getPreferencesService().getBoolean(ResourcesPlugin.PI_RESOURCES,
				ResourcesPlugin.PREF_AUTO_REFRESH, false, null);

        this.autoRefreshButton.setSelection(autoRefresh);
        this.lightweightRefreshButton.setSelection(lightweightRefresh);
    }

    /**
	 * Create a composite that contains the encoding controls
	 *
	 * @param parent
	 */
    private void createEncodingEditorControls(Composite parent){
		Composite encodingComposite = new Composite(parent,SWT.NONE);
		encodingComposite.setLayout(new GridLayout());
		encodingComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		encodingEditor = new ResourceEncodingFieldEditor(IDEWorkbenchMessages.WorkbenchPreference_encoding, encodingComposite, ResourcesPlugin
				.getWorkspace().getRoot());

		encodingEditor.setPage(this);
		encodingEditor.load();
		encodingEditor.setPropertyChangeListener(event -> {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(encodingEditor.isValid());
			}

		});
    }

    /**
     * Create a composite that contains the line delimiter controls
     *
     * @param parent
     */
    private void createLineSeparatorEditorControls(Composite parent){
    	Composite lineComposite = new Composite(parent,SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		lineComposite.setLayout(gridLayout);

		lineComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		lineSeparatorEditor = new LineDelimiterEditor(lineComposite);
		lineSeparatorEditor.doLoad();
    }

	/**
	 * Create the widget for the system explorer command.
	 *
	 * @param composite
	 */
	protected void createSystemExplorerGroup(Composite composite) {
		Composite groupComposite = new Composite(composite, SWT.LEFT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		groupComposite.setLayout(layout);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		groupComposite.setLayoutData(gd);

		systemExplorer = new StringFieldEditor(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER,
				IDEWorkbenchMessages.IDEWorkbenchPreference_workbenchSystemExplorer, 40, groupComposite);
		Text textControl = systemExplorer.getTextControl(groupComposite);
		BidiUtils.applyBidiProcessing(textControl, BidiUtils.LEFT_TO_RIGHT);
		gd = (GridData) textControl.getLayoutData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		systemExplorer.setPreferenceStore(getIDEPreferenceStore());
		systemExplorer.setPage(this);

		systemExplorer.load();

		systemExplorer.setPropertyChangeListener(event -> {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(systemExplorer.isValid());
			}
		});
	}

    /**
     * Returns the IDE preference store.
     * @return the preference store.
     */
    protected IPreferenceStore getIDEPreferenceStore() {
        return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
    }

	/**
     * Creates a tab of one horizontal spans.
     *
     * @param parent
     *            the parent in which the tab should be created
     */
    protected static void createSpace(Composite parent) {
        Label vfiller = new Label(parent, SWT.LEFT);
        GridData gridData = new GridData();
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = GridData.CENTER;
        gridData.grabExcessVerticalSpace = false;
        vfiller.setLayoutData(gridData);
    }

	/**
     * Creates the composite which will contain all the preference controls for
     * this page.
     *
     * @param parent
     *            the parent composite
     * @return the composite for this page
     */
    protected Composite createComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        return composite;
    }

	@Override
	public void init(org.eclipse.ui.IWorkbench workbench) {
		e4Context = workbench.getService(IEclipseContext.class);
    }

    /**
     * The default button has been pressed.
     */
    @Override
	protected void performDefaults() {

        // core holds onto this preference.
        boolean autoBuild = ResourcesPlugin.getPlugin().getPluginPreferences()
                .getDefaultBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
        autoBuildButton.setSelection(autoBuild);

        IPreferenceStore store = getIDEPreferenceStore();
        autoSaveAllButton
                .setSelection(store
                        .getDefaultBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD));
        saveInterval.loadDefault();
		showLocationInWindowTitle.setSelection(store.getDefaultBoolean(IDEInternalPreferences.SHOW_LOCATION));
        workspaceName.loadDefault();

        boolean closeUnrelatedProj = store.getDefaultBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS);
        closeUnrelatedProjectButton.setSelection(closeUnrelatedProj);

        boolean lightweightRefresh = ResourcesPlugin.getPlugin()
                .getPluginPreferences().getDefaultBoolean(
                		ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH);
        boolean autoRefresh = ResourcesPlugin.getPlugin()
		        .getPluginPreferences().getDefaultBoolean(
		                ResourcesPlugin.PREF_AUTO_REFRESH);
        autoRefreshButton.setSelection(autoRefresh);
        lightweightRefreshButton.setSelection(lightweightRefresh);

        clearUserSettings = true;

		List encodings = WorkbenchEncoding.getDefinedEncodings();
		Collections.sort(encodings);
        encodingEditor.loadDefault();
		lineSeparatorEditor.loadDefault();
		openReferencesEditor.loadDefault();

		systemExplorer.loadDefault();

        super.performDefaults();
    }

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    @Override
	public boolean performOk() {
        // set the workspace auto-build flag
        IWorkspaceDescription description = ResourcesPlugin.getWorkspace()
                .getDescription();
        if (autoBuildButton.getSelection() != ResourcesPlugin.getWorkspace()
                .isAutoBuilding()) {
            try {
                description.setAutoBuilding(autoBuildButton.getSelection());
                ResourcesPlugin.getWorkspace().setDescription(description);
            } catch (CoreException e) {
                IDEWorkbenchPlugin.log(
                        "Error changing auto build workspace setting.", e//$NON-NLS-1$
                                .getStatus());
            }
        }

        IPreferenceStore store = getIDEPreferenceStore();

        // store the save all prior to build setting
        store.setValue(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD,
                autoSaveAllButton.getSelection());

        // store the workspace save interval
        // @issue we should drop our preference constant and let clients use
        // core's pref. ours is not up-to-date anyway if someone changes this
        // interval directly thru core api.
        long oldSaveInterval = description.getSnapshotInterval() / 60000;
        long newSaveInterval = new Long(saveInterval.getStringValue())
                .longValue();
        if (oldSaveInterval != newSaveInterval) {
            try {
                description.setSnapshotInterval(newSaveInterval * 60000);
                ResourcesPlugin.getWorkspace().setDescription(description);
                store.firePropertyChangeEvent(
                        IDEInternalPreferences.SAVE_INTERVAL, new Integer(
                                (int) oldSaveInterval), new Integer(
                                (int) newSaveInterval));
            } catch (CoreException e) {
                IDEWorkbenchPlugin.log(
                        "Error changing save interval preference", e //$NON-NLS-1$
                                .getStatus());
            }
        }

		store.setValue(IDEInternalPreferences.SHOW_LOCATION, showLocationInWindowTitle.getSelection());

        workspaceName.store();

		systemExplorer.store();

        Preferences preferences = ResourcesPlugin.getPlugin()
                .getPluginPreferences();

        boolean autoRefresh = autoRefreshButton.getSelection();
        preferences.setValue(ResourcesPlugin.PREF_AUTO_REFRESH, autoRefresh);
        boolean lightweightRefresh = lightweightRefreshButton.getSelection();
        preferences.setValue(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, lightweightRefresh);

        boolean closeUnrelatedProj = closeUnrelatedProjectButton.getSelection();
        getIDEPreferenceStore().setValue(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, closeUnrelatedProj);


        if (clearUserSettings) {
			IDEEncoding.clearUserEncodings();
		}
        encodingEditor.store();
		lineSeparatorEditor.store();
		openReferencesEditor.store();

		return super.performOk();
    }

}
