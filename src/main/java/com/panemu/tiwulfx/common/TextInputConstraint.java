/*
 * Copyright (C) 2014 Panemu.
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

package com.panemu.tiwulfx.common;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

/**
 *	This class listens to a Text Input's text-change-event. It can limit maximum
 * character could be entered to a Text Input as well as convert entered text
 * to capital case.
 * 
 * <p>Example:
 * <pre><code>
 * TextField textField = new TextField();
 * textField.textProperty().addListener(new TextInputConstraint(textField, 10, true));
 * </code>
 * </pre>
 * @author Amrullah <amrullah@panemu.com>
 */
public class TextInputConstraint implements ChangeListener<String>{
	private int maxLength = 0;
	private boolean capitalize = false;
	private boolean turnOffListener = false;
	private TextInputControl txt;

	public TextInputConstraint(TextInputControl txt, int maxLength) {
		this(txt, maxLength, false);
	}
	
	/**
	 * 
	 * @param textInput instance of TextField or TextArea.
	 * @param maxLength maximum character length constraint
	 * @param capitalize if true then the text will always be converted to capital case
	 */
	public TextInputConstraint(TextInputControl textInput,int maxLength, boolean capitalize) {
		this.txt = textInput;
		this.maxLength = maxLength;
		this.capitalize = capitalize;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newText) {
		if (turnOffListener) {
			return;
		}
		turnOffListener = true;
		if (newText == null) {
			newText = "";
		}
		if (capitalize) {
			txt.setText(newText.toUpperCase());
		}
		
		if (maxLength > 0 && newText.length() > maxLength) {
			newText = newText.substring(0, maxLength);
			txt.setText(newText);
		}
		turnOffListener = false;
	}
	
}
