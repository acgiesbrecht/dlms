package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.control.NumberField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author hahach
 */
public class NumberControl<R extends Number> extends BaseControl<R, NumberField<R>> {

	private NumberField<R> numberField;

	public NumberControl() {
		this((Class<R>) Double.class);
	}

	public NumberControl(Class<R> clazz) {
		super(new NumberField(clazz));
		numberField = getInputComponent();
	}

	@Override
	protected void bindValuePropertyWithControl(NumberField inputControl) {
		value.bind(inputControl.valueProperty());
	}

	@Override
	public void setValue(R value) {
		numberField.setValue(value);
	}

	@Override
	protected void bindEditablePropertyWithControl(NumberField<R> inputControl) {
		inputControl.editableProperty().bind(editableProperty());
	}

	public final void setNumberType(Class<R> clazz) {
		numberField.setNumberType(clazz);
	}

	public Class<R> getNumberType() {
		return numberField.getNumberType();
	}

	public void setGrouping(boolean grouping) {
		numberField.setGrouping(grouping);
	}

	public boolean isGrouping() {
		return numberField.isGrouping();
	}

	public void setMaxLength(int maxLength) {
		numberField.setMaxLength(maxLength);
	}

	public int getMaxLength() {
		return numberField.getMaxLength();
	}

	public final String getText() {
		return numberField.getText();
	}

	public final ObjectProperty<Class<R>> numberTypeProperty() {
		return numberField.numberTypeProperty();
	}


	public void setDigitBehindDecimal(int digitBehindDecimal) {
		numberField.setDigitBehindDecimal(digitBehindDecimal);
	}

	public int getDigitBehindDecimal() {
		return numberField.getDigitBehindDecimal();
	}

	/**
	 * Allow negative number. When it is set to true, user can type - as first
	 * character. To change application wide value, see {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 * <p>
	 * @param negativeAllowed pass true to allow negative value. Default is
	 *                      {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 */
	public void setNegativeAllowed(boolean allowNegative) {
		numberField.setNegativeAllowed(allowNegative);
	}

	/**
	 * Check if this NumberField allow negative value. Default value is {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 * @return true if allow negative number.
	 */
	public boolean isNegativeAllowed() {
		return numberField.isNegativeAllowed();
	}

	public BooleanProperty allowNegativeProperty() {
		return numberField.negativeAllowedProperty();
	}

}
