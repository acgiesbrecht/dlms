/*
 * Copyright (C) 2015 Panemu.
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

import javafx.stage.Window;

/**
 *
 * @author amrullah
 */
public interface ExceptionHandler {

	/**
	 * This method is called when there is uncatched error happens in TiwulFX
	 * components.
	 * <p>
	 * @param throwable the throwable
	 * @param window    the window on which a Message Dialog should put the
	 *                  modal. Usually it is where the error happens.
	 */
	void handleException(Throwable throwable, Window window);
}
