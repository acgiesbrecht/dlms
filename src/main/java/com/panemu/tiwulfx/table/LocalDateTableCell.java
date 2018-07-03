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

import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LocalDateTableCell<R> extends BaseCell<R, LocalDate> {

	private DatePicker datePicker;
	private LocalDateColumn<R> column;

	public LocalDateTableCell(LocalDateColumn<R> column) {
		super(column);
		this.column = column;
	}

	@Override
	protected void setValueToEditor(LocalDate value) {
		/**
		 * If user type the date instead of using datepicker popup, the date is
		 * not committed to datePicker until user presses ENTER or the datepicker
		 * lost the focus. If user change the date with typing and then presses
		 * ESC to cancel, the text field is not automatically updated because
		 * datePicker doesn't detect any value change. So, let's set the textfield
		 * manually
		 */
		datePicker.getEditor().setText(datePicker.getConverter().toString(value));

		datePicker.setValue(value);
	}

	@Override
	protected LocalDate getValueFromEditor() {
		/**
		 * If user change the date by typing, the new value is committed to the
		 * DatePicker only if user press ENTER or move focus. Since this method
		 * (getValueFromEditor) might be called before then, let's ensure we get
		 * the date as displayed in the DatePicker's textfield
		 */
		StringConverter<LocalDate> c = datePicker.getConverter();
		String dateText = datePicker.getEditor().getText();//get the date as displayed in the textfield
		LocalDate value = datePicker.getValue();
		try {
			value = c.fromString(dateText);
		} catch (DateTimeParseException ex) {
			//no need to log the error. The value of datepicker will be reverted
		}

		datePicker.setValue(value);
		return value;
	}

	@Override
	protected Control getEditor() {
		if (datePicker == null) {
			datePicker = new DatePicker();
			
			TiwulFXUtil.attachShortcut(datePicker, column.getController());
			
			final Callback<DatePicker, DateCell> dayCellFactory
					= new Callback<DatePicker, DateCell>() {
						@Override
						public DateCell call(final DatePicker datePicker) {
							return new DateCell() {
								@Override
								public void updateItem(LocalDate item, boolean empty) {
									super.updateItem(item, empty);

									if (column.getController() != null && !column.getController().isEnabled(item)) {
										setDisable(true);
									}
								}
							};
						}
					};
			datePicker.setDayCellFactory(dayCellFactory);

			datePicker.valueProperty().addListener(new ChangeListener<LocalDate>() {

				@Override
				public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
					if (column.getController() != null && newValue != null && !column.getController().isEnabled(newValue)) {
						column.getController().onDisabledDateSelected(datePicker, oldValue);
					}
				}
			});
		}
		return datePicker;
	}

	@Override
	protected void attachEnterEscapeEventHandler() {
		/**
		 * Use event filter instead on onKeyPressed because Enter and Escape have
		 * been consumed by Combobox it self
		 */
		datePicker.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(getValueFromEditor());
					t.consume();
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
					/**
					 * Propagate ESCAPE key press to cell to go to Browsing mode on
					 * Agile editing only
					 */
					LocalDateTableCell.this.fireEvent(t);
				}
			}
		});
	}
}
