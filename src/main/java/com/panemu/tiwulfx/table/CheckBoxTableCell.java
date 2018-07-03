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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class CheckBoxTableCell<R> extends BaseCell<R, Boolean> {

	private CheckBox checkbox;
	private CheckBoxColumn<R> column;

	public CheckBoxTableCell(CheckBoxColumn<R> column) {
		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(Boolean value) {
		if (value == null && !column.isRequired()) {
			checkbox.setIndeterminate(true);
		} else {
			if (value == null) {
				value = false;
			}
			checkbox.setIndeterminate(false);
			checkbox.setSelected(value);
		}
		checkbox.setText(column.getStringConverter().toString(value));
	}

	@Override
	protected Boolean getValueFromEditor() {
		if (!column.isRequired() && checkbox.isIndeterminate()) {
			return null;
		} else {
			return checkbox.isSelected();
		}
	}

	@Override
	protected Control getEditor() {
		if (checkbox == null) {
			checkbox = new CheckBox();
			checkbox.setAllowIndeterminate(!column.isRequired());
			final ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (checkbox.isIndeterminate()) {
						checkbox.setText(column.getStringConverter().toString(null));
					} else {
						checkbox.setText(column.getStringConverter().toString(checkbox.isSelected()));
					}
				}
			};

			checkbox.indeterminateProperty().addListener(changeListener);
			checkbox.selectedProperty().addListener(changeListener);

			checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean newValue) {
					for (CellEditorListener<R, Boolean> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});

			/**
			 * Disable traversing focus using LEFT and RIGHT.
			 */
			checkbox.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if ((event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT)
							  && isEditing()) {
						event.consume();
					} else if (event.getCode() == KeyCode.DOWN) {
						if (checkbox.isAllowIndeterminate()) {
							if (checkbox.isIndeterminate()) {
								checkbox.setIndeterminate(false);
								checkbox.setSelected(true);
							} else if (checkbox.isSelected()) {
								checkbox.setIndeterminate(false);
								checkbox.setSelected(false);
							} else {
								checkbox.setIndeterminate(true);
							}
						} else {
							checkbox.setSelected(!checkbox.isSelected());
						}
						event.consume();
					} else if (event.getCode() == KeyCode.UP) {
						if (checkbox.isAllowIndeterminate()) {
							if (checkbox.isIndeterminate()) {
								checkbox.setIndeterminate(false);
								checkbox.setSelected(false);
							} else if (checkbox.isSelected()) {
								checkbox.setIndeterminate(true);
							} else {
								checkbox.setIndeterminate(false);
								checkbox.setSelected(true);
							}
						} else {
							checkbox.setSelected(!checkbox.isSelected());
						}
						event.consume();
					}
				}
			});
		}
		return checkbox;
	}

}
