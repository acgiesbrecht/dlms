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

import com.panemu.tiwulfx.control.DateFieldController;
import java.time.LocalDate;
import javafx.event.EventHandler;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Attach shortcut to a {@link DatePicker}. The shortcut is
 * UP arrow to increase date, while DOWN arrow to decrease it.
 * Ctrl + UP arrow to increase month, Ctrl + DOWN arrow to decrease it.
 * Ctrl + Shift + UP arrow to increase year, Ctrl + Shift + DOWN arrow to decrease it.
 * @author Heru Tri Julianto <heru.trijulianto@panemu.com>
 */
public class DateEventHandler implements EventHandler<KeyEvent> {

	DatePicker datePicker;
	DateFieldController dateController;

	public DateEventHandler(DatePicker datePicker, DateFieldController dateController) {
		this.datePicker = datePicker;
		if (dateController == null) {
			this.dateController = new DateFieldController() {

				@Override
				public boolean isEnabled(LocalDate date) {
					return true;
				}
			};
		} else {
			this.dateController = dateController;
		}
		
	}

	@Override
	public void handle(KeyEvent event) {
		if (event.getTarget() != event.getSource()) {
			return;
		}
		LocalDate value = datePicker.getValue();
		if (value == null) {
			value = LocalDate.now();
		}
		if (event.getCode() == KeyCode.UP && event.isControlDown() && event.isShiftDown() && dateController.isEnabled(value.plusYears(1))) {
			datePicker.setValue(value.plusYears(1));
			event.consume();
		} else if (event.getCode() == KeyCode.UP && event.isControlDown() && dateController.isEnabled(value.plusMonths(1))) {
			datePicker.setValue(value.plusMonths(1));
			event.consume();
		} else if (event.getCode() == KeyCode.UP && dateController.isEnabled(value.plusDays(1))) {
			datePicker.setValue(value.plusDays(1));
			event.consume();
		} else if (event.getCode() == KeyCode.DOWN && event.isControlDown() && event.isShiftDown() && dateController.isEnabled(value.minusYears(1))) {
			datePicker.setValue(value.minusYears(1));
			event.consume();
		} else if (event.getCode() == KeyCode.DOWN && event.isControlDown() && dateController.isEnabled(value.minusMonths(1))) {
			datePicker.setValue(value.minusMonths(1));
			event.consume();
		} else if (event.getCode() == KeyCode.DOWN && dateController.isEnabled(value.minusDays(1))) {
			datePicker.setValue(value.minusDays(1));
			event.consume();
		}
	}

}
