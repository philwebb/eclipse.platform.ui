/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerTrimContributionTest extends
		ModelReconcilerTest {

	private void testTrimContribution_PositionInParent(String before,
			String after) {
		MApplication application = createApplication();

		MTrimContribution contribution = ems.createModelElement(MTrimContribution.class);
		application.getTrimContributions().add(contribution);
		contribution.setPositionInParent(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		contribution.setPositionInParent(after);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getTrimContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getTrimContributions().size());
		assertEquals(contribution, application.getTrimContributions().get(0));
		assertEquals(before, contribution.getPositionInParent());

		applyAll(deltas);

		assertEquals(1, application.getTrimContributions().size());
		assertEquals(contribution, application.getTrimContributions().get(0));
		assertEquals(after, contribution.getPositionInParent());
	}

	@Test
	public void testTrimContribution_PositionInParent_NullNull() {
		testTrimContribution_PositionInParent(null, null);
	}

	@Test
	public void testTrimContribution_PositionInParent_NullEmpty() {
		testTrimContribution_PositionInParent(null, "");
	}

	@Test
	public void testTrimContribution_PositionInParent_NullString() {
		testTrimContribution_PositionInParent(null, "id");
	}

	@Test
	public void testTrimContribution_PositionInParent_EmptyNull() {
		testTrimContribution_PositionInParent("", null);
	}

	@Test
	public void testTrimContribution_PositionInParent_EmptyEmpty() {
		testTrimContribution_PositionInParent("", "");
	}

	@Test
	public void testTrimContribution_PositionInParent_EmptyString() {
		testTrimContribution_PositionInParent("", "id");
	}

	@Test
	public void testTrimContribution_PositionInParent_StringNull() {
		testTrimContribution_PositionInParent("id", null);
	}

	@Test
	public void testTrimContribution_PositionInParent_StringEmpty() {
		testTrimContribution_PositionInParent("id", "");
	}

	@Test
	public void testTrimContribution_PositionInParent_StringStringUnchanged() {
		testTrimContribution_PositionInParent("id", "id");
	}

	@Test
	public void testTrimContribution_PositionInParent_StringStringChanged() {
		testTrimContribution_PositionInParent("id", "id2");
	}

	private void testTrimContribution_ParentId(String before, String after) {
		MApplication application = createApplication();

		MTrimContribution contribution = ems.createModelElement(MTrimContribution.class);
		application.getTrimContributions().add(contribution);
		contribution.setParentId(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		contribution.setParentId(after);

		Object state = reconciler.serialize();

		application = createApplication();
		contribution = application.getTrimContributions().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getTrimContributions().size());
		assertEquals(contribution, application.getTrimContributions().get(0));
		assertEquals(before, contribution.getParentId());

		applyAll(deltas);

		assertEquals(1, application.getTrimContributions().size());
		assertEquals(contribution, application.getTrimContributions().get(0));
		assertEquals(after, contribution.getParentId());
	}

	@Test
	public void testTrimContribution_ParentId_NullNull() {
		testTrimContribution_ParentId(null, null);
	}

	@Test
	public void testTrimContribution_ParentId_NullEmpty() {
		testTrimContribution_ParentId(null, "");
	}

	@Test
	public void testTrimContribution_ParentId_NullString() {
		testTrimContribution_ParentId(null, "id");
	}

	@Test
	public void testTrimContribution_ParentId_EmptyNull() {
		testTrimContribution_ParentId("", null);
	}

	@Test
	public void testTrimContribution_ParentId_EmptyEmpty() {
		testTrimContribution_ParentId("", "");
	}

	@Test
	public void testTrimContribution_ParentId_EmptyString() {
		testTrimContribution_ParentId("", "id");
	}

	@Test
	public void testTrimContribution_ParentId_StringNull() {
		testTrimContribution_ParentId("id", null);
	}

	@Test
	public void testTrimContribution_ParentId_StringEmpty() {
		testTrimContribution_ParentId("id", "");
	}

	@Test
	public void testTrimContribution_ParentId_StringStringUnchanged() {
		testTrimContribution_ParentId("id", "id");
	}

	@Test
	public void testTrimContribution_ParentId_StringStringChanged() {
		testTrimContribution_ParentId("id", "id2");
	}
}
