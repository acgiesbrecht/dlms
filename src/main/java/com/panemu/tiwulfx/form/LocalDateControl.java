/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.DateFieldController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LocalDateControl extends BaseControl<LocalDate, DatePicker> {
    private DatePicker dateField;
	private ObjectProperty<DateTimeFormatter> dateTimeProperty = new SimpleObjectProperty<>(TiwulFXUtil.getDateFormatForLocalDate());
    public LocalDateControl() {
        this("");
    }

    public LocalDateControl(String propertyName) {
        super(propertyName, new DatePicker());
        dateField = getInputComponent();
		dateField.setConverter(dateStringConverter);
		
		final Callback<DatePicker, DateCell> dayCellFactory
				= new Callback<DatePicker, DateCell>() {
					@Override
					public DateCell call(final DatePicker datePicker) {
						return new DateCell() {
							@Override
							public void updateItem(LocalDate item, boolean empty) {
								super.updateItem(item, empty);

								if (getController() != null && !getController().isEnabled(item)) {
									setDisable(true);
								}
							}
						};
					}
				};
		dateField.setDayCellFactory(dayCellFactory);
		
		dateField.valueProperty().addListener(new ChangeListener<LocalDate>() {

			@Override
			public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
				if(getController() != null && newValue != null && !getController().isEnabled(newValue)){
					getController().onDisabledDateSelected(dateField, oldValue);
				}
			}
		});
    }

    @Override
    protected void bindValuePropertyWithControl(DatePicker inputControl) {
        value.bind(inputControl.valueProperty());
    }

    @Override
    public void setValue(LocalDate value) {
        dateField.setValue(value);
    }

    public void setPromptText(String promptText) {
        dateField.setPromptText(promptText);
    }
	
    public ObjectProperty<DateTimeFormatter> dateFormatProperty() {
        return dateTimeProperty;
    }

	/**
	 * set Date Format. This method name doesn't conform Java Bean naming because
	 * there is a bug in SceneBuilder. This kind of naming will make this property
	 * read only in SceneBuilder. The problem doesn't exist if there is no default
	 * value for dateFormat property. However, since we want to make defaultFormat follows
	 * what is defined in TiwulFXUtils, we need to make SceneBuilder thinks that
	 * this property is read only.
	 * @param dateFormat 
	 */
    public void setDateFormat_(DateTimeFormatter dateFormat) {
        dateFormatProperty().set(dateFormat);
    }

    public DateTimeFormatter getDateFormat() {
        return dateFormatProperty().get();
    }

    public void showCalendar() {
        dateField.show();
    }

	private StringConverter<LocalDate> dateStringConverter = new StringConverter<LocalDate>() {
			DateTimeFormatter df = dateTimeProperty.get();

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return df.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, df);
				} else {
					return null;
				}
			}
		};
	
	private ObjectProperty<DateFieldController> controllerProperty = new SimpleObjectProperty<>();
	public ObjectProperty<DateFieldController> controllerProperty() {
		return controllerProperty;
	}
	public DateFieldController getController() {
		return controllerProperty.get();
	}
	
	/**
	 * This method will set a controller that will decide which dates are enabled.
	 * A disabled date is not selectable neither using calendar popup or shortcut
	 * (up/down arrow, Ctrl+up/down arrow). If user type-in a disable date, by default
	 * the controller will display an error message and revert the value back. To change
	 * this behavior, override {@link DateFieldController#onDisabledDateSelected(com.panemu.tiwulfx.control.DateField, java.util.Date, java.util.Date) 
	 * DateFieldController.onDisabledDateSelected}
	 * @param dateFieldController 
	 */
	public void setController(DateFieldController dateFieldController) {
		this.controllerProperty.set(dateFieldController);
	}
}
