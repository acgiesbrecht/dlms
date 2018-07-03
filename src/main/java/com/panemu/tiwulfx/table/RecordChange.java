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
public class RecordChange<R, C> {

    private R record;
	private String propertyName;
    private C oldValue;
    private C newValue;

    public RecordChange() {
    }

    public RecordChange(R record, String propertyName, C oldValue, C newValue) {
        this.record = record;
		this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public C getOldValue() {
        return oldValue;
    }

    public void setOldValue(C oldValue) {
        this.oldValue = oldValue;
    }

    public C getNewValue() {
        return newValue;
    }

    public void setNewValue(C newValue) {
        this.newValue = newValue;
    }
    
    public R getRecord() {
        return this.record;
    }
	
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public String toString() {
		return "RecordChange{" + "record=" + record + ", propertyName=" + propertyName + ", oldValue=" + oldValue + ", newValue=" + newValue + '}';
	}
	
}
