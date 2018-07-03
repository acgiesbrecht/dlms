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

import com.panemu.tiwulfx.control.LookupField;
import com.panemu.tiwulfx.control.skin.LookupFieldSkin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupTableCell<R, C> extends BaseCell<R, C> {

	private LookupField<C> lookupField;
	private LookupColumn<R, C> column;

	public LookupTableCell(LookupColumn<R, C> column) {
		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(C value) {
		lookupField.setValue(value);
	}

	@Override
	protected C getValueFromEditor() {
		return lookupField.getValue();
	}

	@Override
	protected Control getEditor() {
		if (lookupField == null) {
			lookupField = new LookupField<>();
			lookupField.setShowSuggestionWaitTime(column.getShowSuggestionWaitTime());
			lookupField.setValue(getItem());
			lookupField.setPropertyName(column.getLookupPropertyName());
			lookupField.setController(column.getLookupController());
			lookupField.setDisableManualInput(column.getDisableLookupManualInput());
    
			/**
			 * Disable traversing focus using LEFT, RIGHT, UP and DOWN.
			 */
			lookupField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if ((event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT
							  || event.getCode() == KeyCode.UP
							  || event.getCode() == KeyCode.DOWN)
							  && isEditing()) {
						event.consume();
					}
				}
			});

			lookupField.valueProperty().addListener(new ChangeListener<C>() {

				@Override
				public void changed(ObservableValue<? extends C> ov, C t, C newValue) {
					for (CellEditorListener<R, C> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});
		}
		return lookupField;
	}

	/**
	 *
	 * @return TextField of LookupField
	 */
	@Override
	protected Control getFocusableControl() {
		if (lookupField.getSkin() != null) {
			return ((LookupFieldSkin<C>) lookupField.getSkin()).getTextField();
		}
		return null;
	}

	@Override
	protected void attachEnterEscapeEventHandler() {
		/**
		 * Use event filter instead on onKeyPressed because Enter and Escape have
		 * been consumed by lookupField it self
		 */
		lookupField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER && !t.isControlDown()) {
					commitEdit(lookupField.getValue());
				} else if (t.getCode() == KeyCode.ESCAPE) {
					lookupField.resetDisplayText();
					cancelEdit();
					/**
					 * Propagate ESCAPE key press to cell
					 */
					LookupTableCell.this.fireEvent(t);
				}
			}
		});

	}

	@Override
	public void commitEdit(C t) {
		super.commitEdit(t);
		forceUpdateRow();
	}

	/**
	 * Force update cell values. It is needed in order to update the values of
	 * cell that display particular property of currently selected lookup object
	 */
	private void forceUpdateRow() {
		((TableRowControl) getTableRow()).refreshLookupSiblings(column.getPropertyName());
	}
}
