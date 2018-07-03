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
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author astri
 */
public class TextAreaTableCell<R> extends BaseCell<R, String> {

        private TextArea textArea;
	private TextAreaColumn<R> column;

	public TextAreaTableCell(TextAreaColumn<R> column) {

		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(String value) {
		textArea.setText(value);
	}

	@Override
	protected String getValueFromEditor() {
		if (textArea.getText() == null
				  || (column.isEmptyStringAsNull() && textArea.getText().trim().isEmpty())) {
			return null;
		}
		return textArea.getText();
	}

	@Override
	protected Control getEditor() {
		if (textArea == null) {
			textArea = new TextArea();
                        
			textArea.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                        textArea.setPrefRowCount(column.getRowCount());                        
                        textArea.setWrapText(true);
                        if (column.isCapitalize()) {
				textArea.textProperty().addListener(new TextInputConstraint(textArea, 0, column.isCapitalize()));
			}
			
			textArea.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> ov, String t, String newValue) {
					for (CellEditorListener<R, String> svl : column.getCellEditorListeners()) {
						svl.valueChanged(getTableRow().getIndex(), column.getPropertyName(), (R) getTableRow().getItem(), newValue);
					}
				}
			});
                        
                        textArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
                                private KeyCombination SHIFT_ENTER = new KeyCodeCombination(KeyCode.ENTER, KeyCodeCombination.SHIFT_ANY);
                                
                                /**
                                 * Set Shift+Enter as shortcut for new line
                                 * @param event 
                                 */
                                @Override
                                public void handle(KeyEvent event) {
                                        
                                        if (SHIFT_ENTER.match(event)) {
                                                textArea.deleteText(textArea.getSelection());
                                                textArea.insertText(textArea.getCaretPosition(), "\n");
                                        }
                                }
                        });
		}
		return textArea;
	}
}