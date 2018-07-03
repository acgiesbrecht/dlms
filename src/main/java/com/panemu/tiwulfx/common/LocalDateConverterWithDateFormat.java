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

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

/**
 * This is a StringConverter class that convert String to/from LocalDate,
 * however the information available to format/parse is {@link DateFormat}
 * instead of {@link DateTimeFormatter}
 * 
 * @author amrullah <amrullah@panemu.com>
 */
public class LocalDateConverterWithDateFormat extends StringConverter<LocalDate> {

	private DateFormat dateFormat;

	public LocalDateConverterWithDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public String toString(LocalDate object) {
		if (object == null) {
			return null;
		}
		return dateFormat.format(TiwulFXUtil.toDate(object));
	}

	@Override
	public LocalDate fromString(String string) {
		if (string == null || string.trim().isEmpty()) {
			return null;
		}
		try {
			return TiwulFXUtil.toLocalDate(dateFormat.parse(string));
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

}
