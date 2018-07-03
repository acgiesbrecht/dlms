/*
 * Copyright (C) 2013 Panemu.
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

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public interface ButtonColumnController<R> {

	/**
	 * This method is called only once, after the button is initiated. Use this method to set property i.e: action
	 * handler, that won't change through out button's lifecycle
	 *
	 * @param button
	 */
	void initButton(Button button, TableCell<R, ?> row);

	/**
	 * This method is called for every {@link TableCell#updateItem()} call. Use this method to set different property
	 * i.e: style, text and perhaps actionHandler dynamically
	 *
	 * @param button
	 * @param record
	 */
	void redrawButton(Button button, R record, String text);
}
