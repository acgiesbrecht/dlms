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
 * This interface is used to listen to editCommit event of a BaseColumn. Add an 
 * implementation of this interface to {@link BaseColumn#addEditCommitListener()}
 * to listen to any value change of that column.
 * 
 * @author amrullah
 * @see BaseColumn#addEditCommitListener(com.panemu.tiwulfx.table.EditCommitListener) 
 */
public interface EditCommitListener<R, C> {
    void editCommited(BaseColumn<R,C> column, R record, C oldValue, C newValue);
}
