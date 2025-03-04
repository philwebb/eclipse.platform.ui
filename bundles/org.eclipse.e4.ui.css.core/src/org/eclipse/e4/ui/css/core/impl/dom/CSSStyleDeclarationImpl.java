/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class CSSStyleDeclarationImpl extends AbstractCSSNode implements CSSStyleDeclaration {

	private boolean readOnly;
	private CSSRule parentRule;
	private List<CSSProperty> properties = new ArrayList<CSSProperty>();

	public CSSStyleDeclarationImpl(CSSRule parentRule) {
		this.parentRule = parentRule;
	}

	// W3C CSSStyleDeclaration API methods

	@Override
	public String getCssText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < properties.size(); i++) {
			CSSProperty property = properties.get(i);
			sb.append(property.getName()).append(": ");
			sb.append(property.getValue().getCssText());
			sb.append(";");
			if (i < properties.size() - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	public int getLength() {
		return properties.size();
	}

	@Override
	public CSSRule getParentRule() {
		return parentRule;
	}

	@Override
	public CSSValue getPropertyCSSValue(String propertyName) {
		CSSProperty property = findCSSProperty(propertyName);
		return (property == null)
			? null
			: property.getValue();
	}

	@Override
	public String getPropertyPriority(String propertyName) {
		CSSProperty property = findCSSProperty(propertyName);
		return (property != null && property.isImportant())
			? CSSPropertyImpl.IMPORTANT_IDENTIFIER
			: "";
	}

	@Override
	public String getPropertyValue(String propertyName) {
		CSSProperty property = findCSSProperty(propertyName);
		return (property == null)
			? ""
			: property.getValue().toString();
	}

	@Override
	public String item(int index) {
		return properties.get(index).getName();
	}

	@Override
	public String removeProperty(String propertyName) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		for (int i = 0; i < properties.size(); i++) {
			CSSProperty property = properties.get(i);
			if(CSSPropertyImpl.sameName(property, propertyName)) {
				properties.remove(i);
				return property.getValue().toString();
			}
		}
		return "";
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		// TODO throws SYNTAX_ERR if cssText is unparsable
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public void setProperty(String propertyName, String value, String priority) throws DOMException {
		if(readOnly)
			throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR, DOMExceptionImpl.NO_MODIFICATION_ALLOWED_ERROR);
		// TODO Auto-generated method stub
		// TODO throws SYNTAX_ERR if value is unparsable
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}


	// Additional

	public void addProperty(CSSProperty  property) {
		properties.add(property);
	}

	public CSSPropertyList getCSSPropertyList() {
		CSSPropertyListImpl propertyList = new CSSPropertyListImpl();
		for (CSSProperty property: properties) {
			propertyList.add(property);
		}
		return propertyList;
	}

	protected void setReadOnly(boolean readOnly) {
		//TODO ViewCSS.getComputedStyle() should provide a read only access to the computed values
		this.readOnly = readOnly;
	}

	private CSSProperty findCSSProperty(String propertyName) {
		for (CSSProperty property : properties) {
			if(CSSPropertyImpl.sameName(property, propertyName))
				return property;
		}
		return null;
	}
}
