/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class NumberField<T extends Number> extends TextField {

	private DecimalFormat formatter = TiwulFXUtil.getDecimalFormat();
	private String zeroDigit = "";
	private int digitBehindDecimal;
	private boolean ignore;
	private int maxLength = 10;
	private String regex = "";
	private String regexDecimal = "";
	private ObjectProperty<Class<T>> clazzProperty = new SimpleObjectProperty<>();
	private boolean grouping = false;
	private String MAX_VALUE_STRING = "";
	private static final Logger logger = Logger.getLogger(NumberField.class.getName());
	private BooleanProperty negativeAllowed = new SimpleBooleanProperty(TiwulFXUtil.DEFAULT_NEGATIVE_ALLOWED);

	public NumberField() {
		this((Class<T>) Double.class);
	}

	public NumberField(Class<T> clazz) {
		this.clazzProperty.set(clazz);
		digitBehindDecimal = TiwulFXUtil.DEFAULT_DIGIT_BEHIND_DECIMAL;
		for (int i = 0; i < digitBehindDecimal; i++) {
			zeroDigit = zeroDigit + "0";
		}
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String s, String s1) {
				if (ignore) {
					return;
				}
				ignore = true;
				boolean needMoveCaret = false;
				String[] parts = breakApart(s1);
				if (parts[0].length() > maxLength) {
					String validText = chopDecimalPlaces(s1);
					value.set(castToExpectedType(validText));
					setText(validText);
					needMoveCaret = true;
				}

				if (getRegex() != null && !getRegex().equals("") && !s1.matches(getRegex())) {
					if (s1.matches(regexDecimal)) {
						String validText = chopDecimalPlaces(s1);
						value.set(castToExpectedType(validText));
						setText(validText);
						needMoveCaret = true;
					} else {
						setText(s);
					}
				}
				s1 = getText();
				if (s1.isEmpty() || s1.equals(formatter.getDecimalFormatSymbols().getDecimalSeparator())) {
					setText("");
					value.set(null);
				} else if (clazzProperty.get() != BigDecimal.class && (s1.length() > MAX_VALUE_STRING.length()
						  || (s1.length() == MAX_VALUE_STRING.length() && s1.compareTo(MAX_VALUE_STRING) > 0))) {
					setText(s);
				} else {
					value.set(castToExpectedType(s1));
				}
				ignore = false;
				if (needMoveCaret) {
					Platform.runLater(() -> {
						int currentPos = getCaretPosition();
						char separator = TiwulFXUtil.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
						int increment = 1;
						if (currentPos > 0 && currentPos < getText().length() - 1 && getText().charAt(currentPos) == separator) {
							increment = 2;
						}
						int pos = Math.min(getText().length(), currentPos + increment);
						positionCaret(pos);
					});
				}
			}
		});
		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				formatter.applyPattern(getPattern(!newValue && grouping));
				ignore = true;
				if (value.get() != null) {
					setText(formatter.format(value.get()));
				} else {
					setText("");
				}
				ignore = false;
			}
		});
		if (clazz != null) {
			setupRegex();
		}

		value.addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				if (ignore) {
					return;
				}
				ignore = true;
				if (newValue != null) {
					formatter.applyPattern(getPattern(isGrouping()));
					setText(formatter.format(newValue));
				} else {
					setText("");
				}
				ignore = false;
			}
		});
		clazzProperty.addListener(new ChangeListener<Class<T>>() {

			@Override
			public void changed(ObservableValue<? extends Class<T>> ov, Class<T> t, Class<T> t1) {
				setupRegex();
				formatter.setParseBigDecimal(t1.equals(BigDecimal.class));
				resetMaxValueString();
			}
		});

		resetMaxValueString();
		negativeAllowed.addListener((observable, oldValue, newValue) -> setupRegex());
	}

	private String[] breakApart(String numberString) {
		String parts[] = null;
		char separator = TiwulFXUtil.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
		if (separator == '.') {
			parts = numberString.split("\\.");
		} else {
			parts = numberString.split(",");
		}
		return parts;
	}

	private String chopDecimalPlaces(String numberString) {
		if (clazzProperty.get() == Integer.class
				  || clazzProperty.get() == Long.class) {
			int end = Math.min(numberString.length(), maxLength);
			return numberString.substring(0, end);
		}
		String[] parts = breakApart(numberString);
		char separator = TiwulFXUtil.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
		if (parts[0].length() > maxLength) {
			if (parts.length == 1) {
				parts = Arrays.copyOf(parts, 2);
				parts[1] = "";
			}
			parts[1] = parts[0].substring(maxLength) + parts[1];
			parts[0] = parts[0].substring(0, maxLength);
			if (parts.length == 1) {
				numberString = parts[0];
			} else {
				numberString = parts[0] + separator + parts[1];
			}
		}

		if (parts.length > 1 && parts[1].length() > digitBehindDecimal) {
			parts[1] = parts[1].substring(0, digitBehindDecimal);
			numberString = parts[0] + separator + parts[1];
		}
		return numberString;
	}

	private void resetMaxValueString() {
		if (clazzProperty.get() == Long.class) {
			MAX_VALUE_STRING = String.valueOf(Long.MAX_VALUE);
		} else if (clazzProperty.get() == Integer.class) {
			MAX_VALUE_STRING = String.valueOf(Integer.MAX_VALUE);
		} else if (clazzProperty.get() == Double.class) {
			MAX_VALUE_STRING = String.valueOf(Double.MAX_VALUE);
		} else if (clazzProperty.get() == Float.class) {
			MAX_VALUE_STRING = String.valueOf(Float.MAX_VALUE);
		} else {
			MAX_VALUE_STRING = "";
		}
	}

	protected String getRegex() {
		return regex;
	}

	private void setupRegex() {
		String prefix = "";
		if (negativeAllowed.get()) {
			prefix = "-?";
		}
		if (clazzProperty.get().equals(Integer.class) || clazzProperty.get().equals(Long.class)) {
			regex = prefix + "\\d*";
			regexDecimal = regex;
			return;
		}
		char separator = TiwulFXUtil.getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
		if (separator == '.') {
			regex = prefix + "\\d*\\.?\\d{0," + digitBehindDecimal + "}";
			regexDecimal = prefix + "\\d*\\.?\\d*";
		} else {
			regex = prefix + "\\d*" + separator + "?\\d{0," + digitBehindDecimal + "}";
			regexDecimal = prefix + "\\d*" + separator + "?\\d*";
		}
	}

	public final void setNumberType(Class<T> clazz) {
		this.clazzProperty.set(clazz);
	}

	public Class<T> getNumberType() {
		return clazzProperty.get();
	}

	public final ObjectProperty<Class<T>> numberTypeProperty() {
		return this.clazzProperty;
	}

	/**
	 * Cast string to Number type that accepted by this NumberField. It depends
	 * on NumberType (see {@link #setNumberType}
	 *
	 * @param numberString
	 * @return
	 */
	public T castToExpectedType(String numberString) {
		if (numberString.length() == 1 && numberString.charAt(0) == formatter.getDecimalFormatSymbols().getDecimalSeparator()) {
			return null;
		}
		if ("-".equals(numberString)) {
			return null;
		}
		try {
			numberString = chopDecimalPlaces(numberString);
			Number number = formatter.parse(numberString);
			T val = (T) number;

			if (clazzProperty.get().equals(BigDecimal.class) && !(number instanceof BigDecimal)) {
				if (number instanceof Long || number instanceof Integer) {
					val = (T) BigDecimal.valueOf(number.longValue());
				} else {
					val = (T) BigDecimal.valueOf(number.doubleValue());
				}
			} else if (clazzProperty.get().equals(Double.class) && !(number instanceof Double)) {
				val = (T) new Double(number.doubleValue());
			} else if (clazzProperty.get().equals(Float.class) && !(number instanceof Float)) {
				val = (T) new Float(number.floatValue());
			} else if (clazzProperty.get().equals(Integer.class) && !(number instanceof Integer)) {
				val = (T) new Integer(number.intValue());
			}

			return val;
		} catch (ParseException ex) {
			logger.log(Level.SEVERE, "Unable to cast " + numberString + " to " + getNumberType().getName());
			return null;
		}
	}

	/**
	 * Set whether it will use thousand separator or not
	 *
	 * @param grouping set it to true to use thousand separator
	 */
	public void setGrouping(boolean grouping) {
		this.grouping = grouping;
		formatter.applyPattern(getPattern(grouping));
	}

	/**
	 *
	 * @return true if the NumberField display thousand separator
	 */
	public boolean isGrouping() {
		return grouping;
	}

	protected String getPattern(boolean grouping) {
		if (clazzProperty.get().equals(Integer.class) || clazzProperty.get().equals(Long.class)) {
			if (grouping) {
				formatter.setParseBigDecimal(clazzProperty.get().equals(BigDecimal.class));
				return "###,###";
			} else {
				return "###";
			}
		} else {
			if (grouping) {
				formatter.setParseBigDecimal(clazzProperty.get().equals(BigDecimal.class));
				return "###,##0." + zeroDigit;
			} else {
				return "##0." + zeroDigit;
			}
		}
	}

	public void setDigitBehindDecimal(int digitBehindDecimal) {
		this.digitBehindDecimal = digitBehindDecimal;
		if (digitBehindDecimal > 2) {
			zeroDigit = "";
			for (int i = 0; i < digitBehindDecimal; i++) {
				zeroDigit = zeroDigit + "0";
			}
		}
		setupRegex();
	}

	public int getDigitBehindDecimal() {
		return digitBehindDecimal;
	}

	public ObjectProperty<T> valueProperty() {
		return value;
	}

	public void setValue(T value) {
		this.value.set(value);
	}

	public T getValue() {
		return value.get();
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * Allow negative number. When it is set to true, user can type - as first
	 * character. To change application wide value, see {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 * <p>
	 * @param negativeAllowed pass true to allow negative value. Default is
	 *                      {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 */
	public void setNegativeAllowed(boolean negativeAllowed) {
		this.negativeAllowed.set(negativeAllowed);
	}

	/**
	 * Check if this NumberField allow negative value. Default value is {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
	 * @return true if allow negative number.
	 */
	public boolean isNegativeAllowed() {
		return negativeAllowed.get();
	}

	public BooleanProperty negativeAllowedProperty() {
		return negativeAllowed;
	}

	private ObjectProperty<T> value = new SimpleObjectProperty<T>() {

		@Override
		public T get() {
			if (getNumberType() == Double.class && super.get() != null && super.get().equals(-0d)) {
				//handle negative zero of Double. Replace it with positive zero
				super.set((T) new Double(0.0));
			}
			return super.get();
		}

	};
}
