/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerItemTest extends ModelReconcilerTest {

	private void testItem_Enabled(boolean applicationState, boolean userChange,
			boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setEnabled(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setEnabled(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		item.setEnabled(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.isEnabled());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.isEnabled());
		} else {
			// user change must override application state
			assertEquals(userChange, item.isEnabled());
		}
	}

	@Test
	public void testItem_Enabled_TrueTrueTrue() {
		testItem_Enabled(true, true, true);
	}

	@Test
	public void testItem_Enabled_TrueTrueFalse() {
		testItem_Enabled(true, true, false);
	}

	@Test
	public void testItem_Enabled_TrueFalseTrue() {
		testItem_Enabled(true, false, true);
	}

	@Test
	public void testItem_Enabled_TrueFalseFalse() {
		testItem_Enabled(true, false, false);
	}

	@Test
	public void testItem_Enabled_FalseTrueTrue() {
		testItem_Enabled(false, true, true);
	}

	@Test
	public void testItem_Enabled_FalseTrueFalse() {
		testItem_Enabled(false, true, false);
	}

	@Test
	public void testItem_Enabled_FalseFalseTrue() {
		testItem_Enabled(false, false, true);
	}

	@Test
	public void testItem_Enabled_FalseFalseFalse() {
		testItem_Enabled(false, false, false);
	}

	private void testItem_Selected(boolean applicationState,
			boolean userChange, boolean newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setSelected(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setSelected(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.isSelected());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.isSelected());
		} else {
			// user change must override application state
			assertEquals(userChange, item.isSelected());
		}
	}

	@Test
	public void testItem_Selected_TrueTrueTrue() {
		testItem_Selected(true, true, true);
	}

	@Test
	public void testItem_Selected_TrueTrueFalse() {
		testItem_Selected(true, true, false);
	}

	@Test
	public void testItem_Selected_TrueFalseTrue() {
		testItem_Selected(true, false, true);
	}

	@Test
	public void testItem_Selected_TrueFalseFalse() {
		testItem_Selected(true, false, false);
	}

	@Test
	public void testItem_Selected_FalseTrueTrue() {
		testItem_Selected(false, true, true);
	}

	@Test
	public void testItem_Selected_FalseTrueFalse() {
		testItem_Selected(false, true, false);
	}

	@Test
	public void testItem_Selected_FalseFalseTrue() {
		testItem_Selected(false, false, true);
	}

	@Test
	public void testItem_Selected_FalseFalseFalse() {
		testItem_Selected(false, false, false);
	}

	private void testItem_Type(ItemType applicationState, ItemType userChange,
			ItemType newApplicationState) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		MMenuItem item = ems.createModelElement(MDirectMenuItem.class);

		menu.getChildren().add(item);
		window.setMainMenu(menu);

		item.setType(applicationState);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		item.setType(userChange);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();
		item = (MMenuItem) menu.getChildren().get(0);

		item.setType(newApplicationState);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(newApplicationState, item.getType());

		applyAll(deltas);

		if (userChange == applicationState) {
			// no change from the user, the new state is applied
			assertEquals(newApplicationState, item.getType());
		} else {
			// user change must override application state
			assertEquals(userChange, item.getType());
		}
	}

	@Test
	public void testItem_Type_PushPushPush() {
		testItem_Type(ItemType.PUSH, ItemType.PUSH, ItemType.PUSH);
	}

	@Test
	public void testItem_Type_PushPushCheck() {
		testItem_Type(ItemType.PUSH, ItemType.PUSH, ItemType.CHECK);
	}

	@Test
	public void testItem_Type_PushCheckPush() {
		testItem_Type(ItemType.PUSH, ItemType.CHECK, ItemType.PUSH);
	}

	@Test
	public void testItem_Type_PushCheckCheck() {
		testItem_Type(ItemType.PUSH, ItemType.CHECK, ItemType.CHECK);
	}

	@Test
	public void testItem_Type_CheckCheckCheck() {
		testItem_Type(ItemType.CHECK, ItemType.CHECK, ItemType.CHECK);
	}

	@Test
	public void testItem_Type_CheckCheckRadio() {
		testItem_Type(ItemType.CHECK, ItemType.CHECK, ItemType.RADIO);
	}

	@Test
	public void testItem_Type_CheckRadioCheck() {
		testItem_Type(ItemType.CHECK, ItemType.RADIO, ItemType.CHECK);
	}

	@Test
	public void testItem_Type_CheckRadioRadio() {
		testItem_Type(ItemType.CHECK, ItemType.RADIO, ItemType.RADIO);
	}

	@Test
	public void testItem_Type_RadioRadioRadio() {
		testItem_Type(ItemType.RADIO, ItemType.RADIO, ItemType.RADIO);
	}
}
