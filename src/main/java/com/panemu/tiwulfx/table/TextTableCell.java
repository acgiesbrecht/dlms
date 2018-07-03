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
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TextInputConstraint;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TextTableCell<R> extends BaseCell<R, String> {

	private TextField textField;
	private TextColumn<R> column;

	public TextTableCell(TextColumn<R> column) {

		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(String value) {
		textField.setText(value);
	}

	@Override
	protected String getValueFromEditor() {
		if (textField.getText() == null
				  || (column.isEmptyStringAsNull() && textField.getText().trim().isEmpty())) {
			return null;
		}
		return textField.getText();
	}

	@Override
	protected Control getEditor() {
		if (textField == null) {
			textField = new TextField();
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			if (column.isCapitalize() || column.getMaxLength() > 0) {
				textField.textProperty().addListener(new TextInputConstraint(textField, column.getMaxLength(), column.isCapitalize()));
			}
			textField.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> ov, String t, String newValue) {
					for (CellEditorListener<R, String> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});

		}
		return textField;
	}
}
