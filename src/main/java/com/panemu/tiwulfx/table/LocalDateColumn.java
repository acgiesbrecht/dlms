/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.DateFieldController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author amrullah
 */
public class LocalDateColumn<R> extends BaseColumn<R, LocalDate> {

	private DatePicker searchInputControl = new DatePicker();
	private SearchMenuItemBase<LocalDate> searchMenuItem = new SearchMenuItemBase<LocalDate>(this) {

		@Override
		protected Node getInputControl() {
			return searchInputControl;
		}

		@Override
		protected List<Operator> getOperators() {
			List<Operator> lst = new ArrayList<>();
			lst.add(Operator.eq);
			lst.add(Operator.ne);
			lst.add(Operator.lt);
			lst.add(Operator.le);
			lst.add(Operator.gt);
			lst.add(Operator.ge);
			lst.add(Operator.is_null);
			lst.add(Operator.is_not_null);
			return lst;
		}

		@Override
		protected LocalDate getValue() {
			return searchInputControl.getValue();
		}
	};

	public LocalDateColumn() {
		this("");
	}

	public LocalDateColumn(String propertyName) {
		this(propertyName, 100);
	}

	public LocalDateColumn(String propertyName, double preferredWidth) {
		super(propertyName, preferredWidth);
		Callback<TableColumn<R, LocalDate>, TableCell<R, LocalDate>> cellFactory
				= new Callback<TableColumn<R, LocalDate>, TableCell<R, LocalDate>>() {
					@Override
					public TableCell call(TableColumn p) {
						return new LocalDateTableCell<R>(LocalDateColumn.this);
					}
				};
		setCellFactory(cellFactory);
		setStringConverter(stringConverter);
		TiwulFXUtil.attachShortcut(searchInputControl, getController());
	}

	@Override
	public MenuItem getSearchMenuItem() {
		searchInputControl.setValue(getDefaultSearchValue());
		return searchMenuItem;
	}

	private StringConverter<LocalDate> stringConverter = new StringConverter<LocalDate>() {

		@Override
		public String toString(LocalDate date) {
			if (date != null) {
				return dateFormat.get().format(date);
			}else{
				return getNullLabel();
			}
			
		}

		@Override
		public LocalDate fromString(String string) {
			if (string != null && !string.trim().isEmpty() && !string.equals(getNullLabel())) {
				return LocalDate.parse(string, dateFormat.get());
			} else {
				return null;
			}
			
		}
	};

	/**
	 * Gets the date format.
	 *
	 * @return The date format.
	 */
	public ObjectProperty<DateTimeFormatter> dateFormatProperty() {
		return dateFormat;
	}
	private ObjectProperty<DateTimeFormatter> dateFormat = new SimpleObjectProperty<DateTimeFormatter>(TiwulFXUtil.getDateFormatForLocalDate());

	public void setDateFormat(DateTimeFormatter dateFormat) {
		this.dateFormat.set(dateFormat);
	}

	public DateTimeFormatter getDateFormat() {
		return dateFormat.get();
	}
	
	private ObjectProperty<DateFieldController> controllerProperty = new SimpleObjectProperty<>();

	public ObjectProperty<DateFieldController> controllerProperty() {
		return controllerProperty;
	}

	public DateFieldController getController() {
		return controllerProperty.get();
	}

	/**
	 * This method will set a controller that will decide which dates are
	 * enabled. A disabled date is not selectable neither using calendar popup
	 * or shortcut (up/down arrow, Ctrl+up/down arrow). If user type-in a
	 * disable date, by default the controller will display an error message and
	 * revert the value back. To change this behavior, override {@link DateFieldController#onDisabledDateSelected(com.panemu.tiwulfx.control.DateField, java.util.Date, java.util.Date)
	 * DateFieldController.onDisabledDateSelected}
	 *
	 * @param dateFieldController
	 */
	public void setController(DateFieldController dateFieldController) {
		this.controllerProperty.set(dateFieldController);
	}

	
}
