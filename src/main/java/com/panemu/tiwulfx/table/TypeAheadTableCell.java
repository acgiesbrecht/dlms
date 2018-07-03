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

import com.panemu.tiwulfx.control.TypeAheadField;
import com.panemu.tiwulfx.control.skin.TypeAheadFieldSkin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TypeAheadTableCell<R, C> extends BaseCell<R, C> {

	private TypeAheadField<C> typeAheadField;
	private TypeAheadColumn<R, C> column;

	public TypeAheadTableCell(TypeAheadColumn<R, C> column) {
		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(C value) {
		typeAheadField.setValue(value);
	}

	@Override
	protected C getValueFromEditor() {
		return typeAheadField.getValue();
	}

	/**
	 *
	 * @return the textfield in TypeAhead
	 */
	@Override
	protected Control getFocusableControl() {
		if (typeAheadField.getSkin() != null) {
			return ((TypeAheadFieldSkin<C>) typeAheadField.getSkin()).getTextField();
		}
		return null;
	}

	@Override
	protected Control getEditor() {
		if (typeAheadField == null) {
			typeAheadField = new TypeAheadField<>();

			typeAheadField.setSorted(column.isSorted());
			if (!column.isRequired()) {
				typeAheadField.getItems().add(null);
			}

			typeAheadField.valueProperty().addListener(new ChangeListener<C>() {

				@Override
				public void changed(ObservableValue<? extends C> ov, C t, C newValue) {
					for (CellEditorListener<R, C> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});

			column.requiredProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
					if (t1) {
						typeAheadField.getItems().remove(null);
					} else {
						typeAheadField.getItems().add(0, null);
					}
				}
			});
			typeAheadField.getItems().addAll(column.getItemMap().values());
			column.getItemMap().addListener(new MapChangeListener<String, C>() {
				//TODO this listener is subject to memory leak
				@Override
				public void onChanged(MapChangeListener.Change<? extends String, ? extends C> change) {
					if (change.wasAdded()) {
						typeAheadField.getItems().add(change.getValueAdded());
					} else {
						typeAheadField.getItems().remove(change.getValueRemoved());
					}
				}
			});

			typeAheadField.setConverter(column.getStringConverter());

		}
		return typeAheadField;
	}

	@Override
	protected void attachEnterEscapeEventHandler() {
		/**
		 * Use event filter instead on onKeyPressed because Enter and Escape have
		 * been consumed by TypeAhead it self
		 */
		typeAheadField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(typeAheadField.getValue());
					t.consume();
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
					/**
					 * Propagate ESCAPE key press to cell
					 */
					TypeAheadTableCell.this.fireEvent(t);
				}
			}
		});
	}
}
