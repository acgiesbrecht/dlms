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

import com.panemu.tiwulfx.dialog.MessageDialog;
import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import javafx.stage.Window;

/**
 * Default implementation of {@link ExceptionHandlerFactory}. On exception, an
 * Error {@link MessageDialog} is shown with the error stacktrace.
 * @author amrullah
 */
public class DefaultExceptionHandlerFactory implements ExceptionHandlerFactory {
	public ExceptionHandler createExceptionHandler() {
		return new DefaultExceptionHandler();
	}
	
	private class DefaultExceptionHandler implements ExceptionHandler {

	@Override
	public void handleException(Throwable throwable, Window window) {
		String errMessage= TiwulFXUtil.getLiteral("error.occured") + "\n"+ throwable.getMessage();
		MessageDialogBuilder.error(throwable).message(errMessage).show(window);
	}
	
}
}
