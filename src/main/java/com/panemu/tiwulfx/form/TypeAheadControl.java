/*
 * Copyright (C) 2013 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.control.TypeAheadField;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

/**
 *
 * @author amrullah
 */
public class TypeAheadControl<R> extends BaseControl<R, TypeAheadField<R>> {
	private TypeAheadField<R> typeAheadField;
	public TypeAheadControl() {
		this("");
	}

	public TypeAheadControl(String propertyName) {
		super(propertyName, new TypeAheadField<R>());
		typeAheadField = getInputComponent();
	}

	public TypeAheadControl(String propertyName, TypeAheadField control) {
		super(propertyName, control);
	}

	@Override
	protected void bindValuePropertyWithControl(TypeAheadField inputControl) {
		value.bind(inputControl.valueProperty());
	}

	@Override
	public void setValue(R value) {
		typeAheadField.setValue(value);
	}

	public final ObservableList<R> getItems() {
		return typeAheadField.getItems();
	}

	public void addItem(String Label, R value) {
		typeAheadField.addItem(Label, value);
	}

	public final StringConverter<R> getConverter() {
		return typeAheadField.getConverter();
	}

	public String getPromptText() {
		return typeAheadField.getPromptText();
	}

	public void setPromptText(String promptText) {
		typeAheadField.setPromptText(promptText);
	}

	public StringProperty promptTextProperty() {
		return typeAheadField.promptTextProperty();
	}

	/**
	 * Get Label of selected value
	 * @return  String label of selected value
	 * @see TypeAheadField#getText() 
	 */
	public String getText() {
		return typeAheadField.getText();
	}

	/**
	 * Get Label of passed value
	 * @param value
	 * @return String label of passed value
	 * @see TypeAheadField#getText(java.lang.Object) 
	 */
	public String getText(R value) {
		return typeAheadField.getText(value);
	}
	
	
	
}
