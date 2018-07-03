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

package com.panemu.tiwulfx.control;

import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import java.time.LocalDate;
import javafx.scene.control.DatePicker;

/**
 *
 * @author Heru Tri Julianto <heru.trijulianto@panemu.com>
 */
public abstract class DateFieldController {
	
	/**
	 * This method is called if user type in disabled date then move the focus
	 * away from the DateField. It will display an error message and then revert
	 * the value back.
	 * @param dateField the datefield we are talking about :)
	 * @param oldDate date of dateField when it received focus
	 */
	public void onDisabledDateSelected(DatePicker dateField, LocalDate oldDate) {
		MessageDialogBuilder.error().message("date.disabled").show(dateField.getScene().getWindow());
		dateField.setValue(oldDate);
	}

	/**
	 * This abstract method will decide whether passed date is enabled.
	 * @param date 
	 * @return true to enable the date
	 */
	public abstract boolean isEnabled(LocalDate date);
}

