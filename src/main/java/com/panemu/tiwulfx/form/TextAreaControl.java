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

import javafx.scene.control.TextArea;

/**
 *
 * @author amrullah
 */
public class TextAreaControl extends BaseControl<String, TextArea>{

	private TextArea textArea;

	public TextAreaControl() {
		this("");
	}

	public TextAreaControl(String propertyName) {
		super(propertyName, new TextArea());
		textArea = getInputComponent();
	}
	
	
	@Override
	protected void bindValuePropertyWithControl(TextArea inputControl) {
		value.bind(inputControl.textProperty());
	}

	@Override
	public void setValue(String value) {
		textArea.setText(value);
	}
	
	@Override
    protected void bindEditablePropertyWithControl(TextArea inputControl) {
        inputControl.editableProperty().bind(editableProperty());
    }
	
}
