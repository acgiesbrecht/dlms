/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.LocalDateConverterWithDateFormat;
import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.DateFieldController;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
public class DateColumn<R> extends BaseColumn<R, Date> {

	private DatePicker searchInputControl;
	private SearchMenuItemBase<Date> searchMenuItem;

	public DateColumn() {
		this("");
	}

	public DateColumn(String propertyName) {
		this(propertyName, 100);
	}

	public DateColumn(String propertyName, double preferredWidth) {
		super(propertyName, preferredWidth);
		Callback<TableColumn<R, Date>, TableCell<R, Date>> cellFactory
				= new Callback<TableColumn<R, Date>, TableCell<R, Date>>() {
					@Override
					public TableCell call(TableColumn p) {
						return new DateTableCell<R>(DateColumn.this);
					}
				};
		setCellFactory(cellFactory);
		setStringConverter(stringConverter);
	}

	@Override
	public MenuItem getSearchMenuItem() {
		
		if (searchMenuItem == null) {
			searchInputControl = new DatePicker();
			searchInputControl.setConverter(new LocalDateConverterWithDateFormat(getDateFormat()));
			searchMenuItem = new SearchMenuItemBase<Date>(this) {

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
				protected Date getValue() {
					return TiwulFXUtil.toDate(searchInputControl.getValue());
				}
			};
		}

		searchInputControl.setValue(TiwulFXUtil.toLocalDate(getDefaultSearchValue()));
		
		TiwulFXUtil.attachShortcut(searchInputControl, getController());
		return searchMenuItem;
	}

	private StringConverter<Date> stringConverter = new StringConverter<Date>() {

		@Override
		public String toString(Date date) {
			if (date != null) {
				return dateFormat.get().format(date);
			}else{
				return getNullLabel();
			}
			
		}

		@Override
		public Date fromString(String string) {
			if (string != null && !string.trim().isEmpty() && !string.equals(getNullLabel())) {
				try {
					return dateFormat.get().parse(string);
				} catch (ParseException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				return null;
			}
			
		}
	};

	/**
	 * Gets the date format.
	 *
	 * @return The date format. By Default date format is taken from {@link TiwulFXUtil#getDateFormatForJavaUtilDate()}
	 */
	public ObjectProperty<DateFormat> dateFormatProperty() {
		return dateFormat;
	}
	
	private ObjectProperty<DateFormat> dateFormat = new SimpleObjectProperty<DateFormat>(TiwulFXUtil.getDateFormatForJavaUtilDate());

	/**
	 * Override the date format for this column only (not system wide).
	 * The default date format is taken from {@link TiwulFXUtil#getDateFormatForJavaUtilDate()}
	 * @param dateFormat 
	 */
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat.set(dateFormat);
	}

	public DateFormat getDateFormat() {
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
