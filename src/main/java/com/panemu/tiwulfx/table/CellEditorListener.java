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

/**
 *
 * @author amrullah
 */
public interface CellEditorListener<R, C> {

	/**
	 * It will be called when selected value is change although the new value
	 * is not yet been committed to table cell.
	 * 
	 * @param rowIndex
	 * @param propertyName
	 * @param record
	 * @param value property value of the record hold by the cell
	 */
	void valueChanged(int rowIndex, String propertyName, R record, C value);
}
