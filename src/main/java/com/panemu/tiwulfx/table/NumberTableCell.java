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

import com.panemu.tiwulfx.control.NumberField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class NumberTableCell<R, C extends Number> extends BaseCell<R, C> {

	private NumberField<C> textField;
	private NumberColumn<R, C> column;

	public NumberTableCell(NumberColumn<R, C> column) {
		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(C value) {
		textField.setValue(value);
	}

	@Override
	protected C getValueFromEditor() {
		return textField.getValue();
	}

	@Override
	protected Control getEditor() {
		if (textField == null) {
			textField = new NumberField<>(column.getNumberType());
			textField.setMaxLength(column.getMaxLength());
			textField.setDigitBehindDecimal(column.getDigitBehindDecimal());
			textField.setNegativeAllowed(column.isNegativeAllowed());
			textField.valueProperty().addListener(new ChangeListener<C>() {

				@Override
				public void changed(ObservableValue<? extends C> ov, C t, C newValue) {
					for (CellEditorListener<R, C> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});
		}
		return textField;
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
	}
}
