/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.common.Validator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * This is a parent class of input controls that designed to be used inside
 * {@link Form}. This class simply wraps the input control in order to add new
 * behavior i.e: required icon, invalid icon, invalid message popup.
 *
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public abstract class BaseControl<R, E extends Control> extends HBox {

	private String propertyName;
	private BooleanProperty required = new SimpleBooleanProperty(false);
	private BooleanProperty valid = new SimpleBooleanProperty(true);
	private StringProperty errorMessage;
	private static Image imgRequired = TiwulFXUtil.getGraphicFactory().getValidationRequiredImage();
	private static Image imginvalid = TiwulFXUtil.getGraphicFactory().getValidationRequiredImage();
	private static Image imgRequiredInvalid = TiwulFXUtil.getGraphicFactory().getValidationRequiredInvalidImage();
	private ImageView imagePlaceHolder = new ImageView();
	private E inputControl;
	protected ObjectProperty<R> value;
	private PopupControl popup;
	private Label errorLabel;
	private List<Validator<R>> lstValidator = new ArrayList<>();
	private InvalidationListener imageListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable o) {
			if (required.get() && !valid.get()) {
				imagePlaceHolder.setImage(imgRequiredInvalid);
			} else if (required.get()) {
				imagePlaceHolder.setImage(imgRequired);
			} else if (!valid.get()) {
				imagePlaceHolder.setImage(imginvalid);
			} else {
				imagePlaceHolder.setImage(null);
			}
		}
	};

	public BaseControl(E control) {
		this("", control);
	}

	public BaseControl(String propertyName, E control) {
		this.inputControl = control;
		this.propertyName = propertyName;
		HBox.setHgrow(control, Priority.ALWAYS);
		setAlignment(Pos.CENTER_LEFT);
		control.setMaxWidth(Double.MAX_VALUE);
		control.setMinHeight(USE_PREF_SIZE);
		getChildren().add(control);
		getChildren().add(imagePlaceHolder);

		required.addListener(imageListener);
		valid.addListener(imageListener);

		this.getStyleClass().add("form-control");
		value = new SimpleObjectProperty<>();
		bindValuePropertyWithControl(control);
		bindEditablePropertyWithControl(control);

		addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getEventType() == MouseEvent.MOUSE_MOVED
						&& !isValid()
						&& !getPopup().isShowing()) {
					Point2D p = BaseControl.this.localToScene(0.0, 0.0);
					getPopup().show(BaseControl.this,
							p.getX() + getScene().getX() + getScene().getWindow().getX(),
							p.getY() + getScene().getY() + getScene().getWindow().getY() + getInputComponent().getHeight() - 1);
				} else if (event.getEventType() == MouseEvent.MOUSE_EXITED && getPopup().isShowing()) {
					getPopup().hide();
				}
			}
		});
		getInputComponent().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				if (!isValid() && getPopup().isShowing()) {
					getPopup().hide();
				}
			}
		});
	}

	/**
	 * Delegate method. Request focus for underlying input component
	 */
	@Override
	public void requestFocus() {
		getInputComponent().requestFocus();
	}

	private StringProperty getErrorMessage() {
		if (errorMessage == null) {
			errorMessage = new SimpleStringProperty();
		}
		return errorMessage;
	}

	private PopupControl getPopup() {
		if (popup == null) {
			errorLabel = new Label();
			errorLabel.textProperty().bind(getErrorMessage());
			popup = new PopupControl();
			final HBox pnl = new HBox();
			pnl.getChildren().add(errorLabel);
			pnl.getStyleClass().add("error-popup");
			popup.setSkin(new Skin() {
				@Override
				public Skinnable getSkinnable() {
					return BaseControl.this.getInputComponent();
				}

				@Override
				public Node getNode() {
					return pnl;
				}

				@Override
				public void dispose() {
				}
			});
			popup.setHideOnEscape(true);
		}
		return popup;
	}

	/**
	 * Sets property name
	 *
	 * @return
	 */
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Set the field to required. A red star will be shown if this value is
	 * true. If the value for this field is empty and required is true, a
	 * validation error will appear on calling {@link Form#validate()}
	 *
	 * @param required
	 */
	public void setRequired(boolean required) {
		this.required.set(required);
	}

	public boolean isRequired() {
		return required.get();
	}

	public BooleanProperty requiredProperty() {
		return required;
	}

	/**
	 * Set the value contained by the control to valid. To set it to invalid
	 * call {@link #setInvalid(java.lang.String)}
	 */
	public void setValid() {
		this.valid.set(true);
	}

	/**
	 * Set the value contained by the control to invalid.
	 *
	 * @see #setValid()
	 * @param errorMessage
	 */
	public void setInvalid(String errorMessage) {
		this.valid.set(false);
		getErrorMessage().set(errorMessage);
	}

	public boolean isValid() {
		return this.valid.get();
	}

	/**
	 * Push value to display in input control
	 *
	 * @param object
	 */
	public final void pushValue(Object object) {
		R pushedValue = null;
		try {
			if (propertyName != null && !propertyName.trim().isEmpty()) {
				if (!getPropertyName().contains(".")) {
					pushedValue = (R) PropertyUtils.getSimpleProperty(object, propertyName);
				} else {
					pushedValue = (R) PropertyUtils.getNestedProperty(object, propertyName);
				}
				setValue(pushedValue);
			} else {
				System.out.println("Warning: propertyName is not set for " + getId());
			}

		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			if (ex instanceof IllegalArgumentException) {
				/**
				 * The actual exception needed to be cathect is
				 * org.apache.commons.beanutils.NestedNullException. But Scene
				 * Builder throw java.lang.ClassNotFoundException:
				 * org.apache.commons.beanutils.NestedNullException if
				 * NestedNullException is referenced in this class. So I catch
				 * its parent isntead.
				 */
				setValue(null);
			} else {
				throw new RuntimeException("Error when pushing value \""+pushedValue+"\" to \"" + propertyName + "\" propertyName. " + ex.getMessage(), ex);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error when pushing value \""+pushedValue+"\" to \"" + propertyName + "\" propertyName. " + ex.getMessage(), ex);
		}
	}

	/**
	 * Set value entered to this input control to the passed obj on
	 * corresponding property name.
	 *
	 * @param obj
	 */
	public void pullValue(Object obj) {
		try {
			if (propertyName != null && !propertyName.trim().isEmpty()) {
				PropertyUtils.setSimpleProperty(obj, propertyName, this.getValue());
			} else {
				System.out.println("Warning: propertyName is not set for " + getId());
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			RuntimeException ex2 = new RuntimeException("Error when pulling " + propertyName + ".", ex);
			throw ex2;
		}
	}

	/**
	 * Bind {@link #value} with control's specific value property. In case of
	 * TextControl it should be
	 * <pre>
	 * {@code value.bind(inputControl.textProperty())}
	 * </pre>
	 *
	 * @param inputControl underlying input control that is wrapped inside
	 * BaseControl
	 */
	protected abstract void bindValuePropertyWithControl(E inputControl);

	/**
	 * Default implementation is, editor control disableProperty is bound with {@link BaseControl}
	 * editable property. There are two different implementation.
	 * Example: 1.	TextField editable is bound with Control editable.
	 *			2.	ComboBox disabled property is bound with Control editable. ComboBox editable behaviour
	 *				is different with TextField editable behaviour. We took ComboBox disabled property to be bound
	 *				with Control's editable property
	 * @param inputControl 
	 */
	protected void bindEditablePropertyWithControl(E inputControl) {
		inputControl.disableProperty().bind(editableProperty().not());
	}

    public abstract void setValue(R value);

	public final R getValue() {
		return value.get();
	}

	public final ReadOnlyObjectProperty<R> valueProperty() {
		return value;
	}

	/**
	 * Gets the underlying input component
	 *
	 * @return
	 */
	public final E getInputComponent() {
		return inputControl;
	}

	/**
	 * Validate value contained in the input control. To make the input control
	 * mandatory, call {@link #setRequired(boolean true)}
	 *
	 * @return false if invalid. True otherwise
	 * @see #addValidator(com.panemu.tiwulfx.common.Validator) to add validator
	 */
	public boolean validate() {
		if (required.get()
				&& (value.get() == null
				|| (value.get() instanceof String && value.get().toString().trim().length() == 0))) {
			String msg = TiwulFXUtil.getLiteral("field.mandatory");
			setInvalid(msg);
			return false;
		}

		R val = value.get();
		//!!!do not trim
		if (value.get() instanceof String && value.get().toString().length() == 0) {
			val = null;
		}

		if (val != null) {
			for (Validator<R> validator : lstValidator) {
				String msg = validator.validate(getValue());
				if (msg != null && !"".equals(msg)) {
					setInvalid(msg);
					return false;
				}
			}
		}
		setValid();
		return true;
	}

	/**
	 * Add validator. An input control might have multiple validators. The
	 * validator will be called with the same sequence the validators are added
	 * to input controls
	 *
	 * @param validator
	 */
	public void addValidator(Validator<R> validator) {
		if (!lstValidator.contains(validator)) {
			lstValidator.add(validator);
		}
	}

	public void removeValidator(Validator<R> validator) {
		lstValidator.remove(validator);
	}
	/**
	 * set whether the input control is editable
	 */
	private BooleanProperty editable = new SimpleBooleanProperty(true);

	public void setEditable(boolean editable) {
		this.editable.set(editable);
	}

	public boolean isEditable() {
		return editable.get();
	}

	public BooleanProperty editableProperty() {
		return this.editable;
	}
	
}
