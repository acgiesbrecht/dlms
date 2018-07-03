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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author amrullah
 */
public class ButtonColumn<R> extends BaseColumn<R, String> {

	public ButtonColumn() {
		this("");
	}
	public ButtonColumn(String propertyName) {
		this(propertyName, 100);
	}

	public ButtonColumn(String propertyName, double preferredWidth) {
		super(propertyName, preferredWidth);

		Callback<TableColumn<R, String>, TableCell<R, String>> cellFactory =
				new Callback<TableColumn<R, String>, TableCell<R, String>>() {
			@Override
			public TableCell call(TableColumn p) {
				return new ButtonCell();
			}
		};

		setCellFactory(cellFactory);
		setStringConverter(stringConverter);
	}

	private ButtonColumnController<R> helper;

	public ButtonColumnController<R> getHelper() {
		return helper;
	}

	public void setHelper(ButtonColumnController<R> helper) {
		this.helper = helper;
	}
	
	private class ButtonCell extends TableCell<R, String> {

		private Button button = new Button();

		public ButtonCell() {
			super();
			setGraphic(button);
			button.setAlignment(Pos.CENTER);
			setText(null);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			button.setMaxWidth(Double.MAX_VALUE);
			helper.initButton(button, this);
			contentDisplayProperty().addListener(new ChangeListener<ContentDisplay>() {
				private boolean suspendEvent = false;

				@Override
				public void changed(ObservableValue<? extends ContentDisplay> observable, ContentDisplay oldValue, ContentDisplay newValue) {
					if (suspendEvent) {
						return;
					}
					if (newValue != ContentDisplay.GRAPHIC_ONLY) {
						suspendEvent = true;
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						suspendEvent = false;
					}
				}
			});

		}

		@Override
		protected void updateItem(String item, boolean empty) {
			boolean emptyRow = getTableView().getItems().size() < getIndex() + 1;
			super.updateItem(item, empty && emptyRow);
			if (!emptyRow) {
				setGraphic(button);
				if (getTableRow() != null) {
					helper.redrawButton(button, (R) getTableRow().getItem(), item);
				}
			} else {
				setGraphic(null);
			}
		}
	}
	
	private StringConverter<String> stringConverter = new StringConverter<String>() {
		@Override
		public String toString(String t) {
			if (t == null) {
				return getNullLabel();
			}
			return t;
		}

		@Override
		public String fromString(String string) {
			if (string == null || string.equals(getNullLabel())) {
				return null;
			}
			return string;
		}
	};
}
