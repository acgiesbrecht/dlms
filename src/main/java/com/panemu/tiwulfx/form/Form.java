/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class Form<R> extends VBox {

	private R valueObject;
	private List<BaseControl> lstInputControl = new ArrayList<>();
	private Map<BaseControl, Boolean> mapEditable = new HashMap<>();

	public static enum Mode {

		INSERT, EDIT, READ
	}

	public Form() {
		super();
		mode.addListener(new ChangeListener<Mode>() {
			@Override
			public void changed(ObservableValue<? extends Mode> ov, Mode t, Mode t1) {
				toggleControlEditable();
			}
		});
	}

	private void toggleControlEditable() {
		boolean editable = mode.get() != Mode.READ;
		changedByForm = true;
		for (BaseControl baseControl : lstInputControl) {
			baseControl.setEditable(editable && mapEditable.get(baseControl));
		}
		changedByForm = false;
	}

	/**
	 * Call this method to get record contained by this form. The form will get
	 * values from input controls inside it and set them to corresponding
	 * properties of the record.
	 *
	 * @return the record that its properties have been update with values taken
	 * from input controls
	 */
	public R getRecord() {
		/**
		 * take all values from input control and set them to valueObject
		 */
		for (BaseControl inputControl : lstInputControl) {
			if (!inputControl.getPropertyName().contains(".")) {
				inputControl.pullValue(valueObject);
			}
		}
		return valueObject;
	}

	public void setRecord(R valueObject) {
		if (lstInputControl.isEmpty()) {
			MessageDialogBuilder.error().message("No controls are bound. Ensure you have called form.bindChildren()").show(null);
		}
		this.valueObject = valueObject;
		/**
		 * take values from value object and display them in input controls
		 */
		for (BaseControl inputControl : lstInputControl) {
			if (inputControl.getPropertyName() == null || inputControl.getPropertyName().isEmpty()) {
				System.out.println("Warning: propertyName is not set for " + inputControl.getId());
			} else {
				inputControl.pushValue(valueObject);
				inputControl.setValid();
			}
		}
	}

	private void scanInputControls(Node node) {
		Pane parent = null;
		if (node instanceof Pane) {
			parent = (Pane) node;
		} else if (node instanceof TitledPane) {
			scanInputControls(((TitledPane)node).getContent());
			return;
		} else if (node instanceof ScrollPane) {
			scanInputControls(((ScrollPane) node).getContent());
			return;
		}
		if (parent == null) {
			return;
		}
		for (final Node component : parent.getChildren()) {
			if (!(component instanceof BaseControl)) {
				scanInputControls(component);
			} else if (component instanceof BaseControl) {
				BaseControl baseControl = (BaseControl) component;
				lstInputControl.add(baseControl);
				mapEditable.put(baseControl, baseControl.isEditable());
				baseControl.editableProperty().addListener(new EditableController(baseControl));
				if (component instanceof LookupControl) {
					((BaseControl) component).valueProperty().addListener(new ChangeListener() {
						@Override
						public void changed(ObservableValue ov, Object t, Object t1) {
							updateNestedObject(((BaseControl) component).getPropertyName(), t1);
						}
					});
				}
			}
		}
	}

	private void updateNestedObject(String joinPropertyName, Object newValue) {
		for (BaseControl control : lstInputControl) {
			if (control.getPropertyName().startsWith(joinPropertyName) && !(control.getPropertyName().equals(joinPropertyName))) {
				String childPropertyName = control.getPropertyName().substring(joinPropertyName.length() + 1, control.getPropertyName().length());
				if (newValue != null) {
					try {
						Object childValue = null;
						if (!childPropertyName.contains(".")) {
							childValue = PropertyUtils.getSimpleProperty(newValue, childPropertyName);
						} else {
							childValue = PropertyUtils.getNestedProperty(newValue, childPropertyName);
						}
						control.setValue(childValue);
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
						if (ex instanceof IllegalArgumentException) {
							/**
							 * The actual exception needed to be caught is
							 * org.apache.commons.beanutils.NestedNullException.
							 * But Scene Builder throw
							 * java.lang.ClassNotFoundException:
							 * org.apache.commons.beanutils.NestedNullException
							 * if NestedNullException is referenced in this
							 * class. So I catch its parent instead.
							 */
							control.setValue(null);
						} else {
							throw new RuntimeException(ex);
						}
					}
				} else {
					control.setValue(null);
				}
			}
		}
	}

	/**
	 * Scan input controls. Call this method after instantiating & initiating
	 * input controls, before calling {@link #setRecord(java.lang.Object)}
	 */
	public void bindChildren() {
		lstInputControl.clear();
		scanInputControls(this);
		toggleControlEditable();
	}

	public boolean validate() {
		boolean result = true;
		for (BaseControl control : lstInputControl) {
			boolean subResult = control.validate();
			result = result && subResult;
		}
		return result;
	}
	private boolean changedByForm = false;

	private class EditableController implements ChangeListener<Boolean> {

		private BaseControl control;

		public EditableController(BaseControl control) {
			this.control = control;

		}

		@Override
		public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean newValue) {
			if (!changedByForm) {
				mapEditable.put(control, newValue);
			}
		}
	}
	private ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.READ);

	/**
	 * Set form's mode. If it is {@link Mode#EDIT} then all input controls
	 * inside it will be not editable. Otherwise, editable input controls will
	 * be writable. Not editable input controls remain not editable.
	 *
	 * @param mode
	 */
	public void setMode(Mode mode) {
		this.mode.set(mode);
	}

	public Mode getMode() {
		return mode.get();
	}

	public ObjectProperty<Mode> modeProperty() {
		return mode;
	}
}
