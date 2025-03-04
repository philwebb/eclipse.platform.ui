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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerWindowTest extends ModelReconcilerTest {

	@Test
	public void testWindow_X() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setX(100);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		applyAll(deltas);

		assertEquals(200, window.getX());
	}

	@Test
	public void testWindow_Y() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setY(100);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setY(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getY());

		applyAll(deltas);

		assertEquals(200, window.getY());
	}

	@Test
	public void testWindow_Width() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setWidth(100);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setWidth(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getWidth());

		applyAll(deltas);

		assertEquals(200, window.getWidth());
	}

	@Test
	public void testWindow_Height() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setHeight(100);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setHeight(200);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getHeight());

		applyAll(deltas);

		assertEquals(200, window.getHeight());
	}

	@Test
	public void testWindow_Menu_Set() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(window.getMainMenu());

		applyAll(deltas);

		menu = window.getMainMenu();
		assertNotNull(menu);
	}

	@Test
	public void testWindow_Menu_Unset() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setMainMenu(null);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(menu, window.getMainMenu());

		applyAll(deltas);

		assertNull(window.getMainMenu());
	}

	private void testWindow_Menu_Visible(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MMenu menu = ems.createModelElement(MMenu.class);
		menu.setToBeRendered(before);
		window.setMainMenu(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, menu.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, menu.isToBeRendered());
	}

	@Test
	public void testWindow_Menu_Children_Add() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);
		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);
		menuItem.setLabel("File");
		menu.getChildren().add(menuItem);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(0, menu.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(1, menu.getChildren().size());
		assertNotNull(menu.getChildren().get(0));
		assertEquals("File", menu.getChildren().get(0).getLabel());
	}

	@Test
	public void testWindow_Menu_Children_Remove() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);
		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		MMenuItem menuItem = ems.createModelElement(MDirectMenuItem.class);
		menuItem.setLabel("File");
		menu.getChildren().add(menuItem);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.getChildren().remove(0);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(1, menu.getChildren().size());
		assertNotNull(menu.getChildren().get(0));
		assertEquals("File", menu.getChildren().get(0).getLabel());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());

		assertEquals(0, menu.getChildren().size());
	}

	@Test
	public void testWindow_Menu_Visible_TrueTrue() {
		testWindow_Menu_Visible(true, true);
	}

	@Test
	public void testWindow_Menu_Visible_TrueFalse() {
		testWindow_Menu_Visible(true, false);
	}

	@Test
	public void testWindow_Menu_Visible_FalseTrue() {
		testWindow_Menu_Visible(false, true);
	}

	@Test
	public void testWindow_Menu_Visible_FalseFalse() {
		testWindow_Menu_Visible(false, false);
	}

	/**
	 * Tests that a window's main menu can change and also additional menus
	 * added to the main menu will be persisted correctly.
	 */
	@Test
	public void testWindow_NestedMenu() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);
		MMenu menu = ems.createModelElement(MMenu.class);
		window.setMainMenu(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.setLabel("menuLabel");

		MMenu item = ems.createModelElement(MMenu.class);
		item.setLabel("itemLabel");
		menu.getChildren().add(item);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		menu = window.getMainMenu();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());
		assertNull(menu.getLabel());
		assertEquals(0, menu.getChildren().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(menu, window.getMainMenu());
		assertEquals("menuLabel", menu.getLabel());
		assertEquals(1, menu.getChildren().size());
		assertEquals("itemLabel", menu.getChildren().get(0).getLabel());
	}

	@Test
	public void testWindow_SharedElements_Add() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(0, window.getSharedElements().size());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertTrue(window.getSharedElements().get(0) instanceof MPart);
	}

	@Test
	public void testWindow_SharedElements_Remove() {
		MApplication application = createApplication();
		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		window.getSharedElements().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.getSharedElements().remove(part);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getSharedElements().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(0, window.getSharedElements().size());
	}
}
