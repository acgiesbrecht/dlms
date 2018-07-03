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
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author astri
 */
public class TextAreaColumn<R extends Object> extends BaseColumn<R, String> {

        private TextField searchInputControl = new TextField();
	private boolean emptyStringAsNull = TiwulFXUtil.DEFAULT_EMPTY_STRING_AS_NULL;
	private int rowCount = 0;
	private boolean capitalize = false;
	
	private SearchMenuItemBase<String> searchMenuItem = new SearchMenuItemBase<String>(this) {
		@Override
		protected Node getInputControl() {
			searchInputControl.setPromptText("kata kunci");
			return searchInputControl;
		}

		@Override
		protected List<Operator> getOperators() {
			List<Operator> lst = new ArrayList<>();
			lst.add(Operator.eq);
			lst.add(Operator.ilike_begin);
			lst.add(Operator.ilike_anywhere);
			lst.add(Operator.ilike_end);
			lst.add(Operator.is_null);
			lst.add(Operator.is_not_null);
			return lst;
		}

		@Override
		protected String getValue() {
			return searchInputControl.getText();
		}
	};

	public TextAreaColumn() {
		this("");
	}

	public boolean isEmptyStringAsNull() {
		return emptyStringAsNull;
	}

	public void setEmptyStringAsNull(boolean emptyStringAsNull) {
		this.emptyStringAsNull = emptyStringAsNull;
	}

	public TextAreaColumn(String propertyName) {
		this(propertyName, 100);
	}

	public TextAreaColumn(String propertyName, double preferredWidth) {
		super(propertyName, preferredWidth);

		Callback<TableColumn<R, String>, TableCell<R, String>> cellFactory =
				new Callback<TableColumn<R, String>, TableCell<R, String>>() {
			@Override
			public TableCell call(TableColumn p) {
				return new TextAreaTableCell<R>(TextAreaColumn.this);
			}
		};

		setCellFactory(cellFactory);
		setStringConverter(stringConverter);
	}

	@Override
	MenuItem getSearchMenuItem() {
		searchInputControl.setText(getDefaultSearchValue());
		return searchMenuItem;
	}
        
        /**
         * 
         * @return rowCount Get preferred row count. If 0 then
         * default value from JavaFX will be used.
         */
        public int getRowCount() {
                return rowCount;
        }
        
        /**
         * 
         * @param rowCount set the preferred row count
         */
        public void setRowCount(int rowCount) {
                this.rowCount = rowCount;
        }

	/**
	 * 
	 * @return true if the cell editor automatically convert entered text to capital
	 * letters
	 */
	public boolean isCapitalize() {
		return capitalize;
	}

	/**
	 * Set whether text entered to a cell editor will be automatically converted to capital
	 * letters.
	 * @param capitalize set to true to make the text always capital.
	 */
	public void setCapitalize(boolean capitalize) {
		this.capitalize = capitalize;
	}
	
	private StringConverter<String> stringConverter = new StringConverter<String>() {
		@Override
		public String toString(String t) {
			if (t == null || (isEmptyStringAsNull() && t.trim().isEmpty())) {
				return getNullLabel();
			}
			return t;
		}

		@Override
		public String fromString(String string) {
			if (string == null || string.equals(getNullLabel())
					|| (isEmptyStringAsNull() && string.isEmpty())) {
				return null;
			}
			return string;
		}
	};
}