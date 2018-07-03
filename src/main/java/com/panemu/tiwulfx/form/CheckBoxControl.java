/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class CheckBoxControl extends BaseControl<Boolean, CheckBox> {

    private CheckBox checkBox;
    private StringProperty trueLabel = new SimpleStringProperty(TiwulFXUtil.getLiteral("label.true"));
    private StringProperty falseLabel = new SimpleStringProperty(TiwulFXUtil.getLiteral("label.false"));
    private StringProperty nullLabel = new SimpleStringProperty(TiwulFXUtil.DEFAULT_NULL_LABEL);
    /**
     * It holds the value of this control. If
     * {@link #setAllowIndeterminate(boolean)} is set to true, the value of this
     * control will be null when its state is indeterminate. This property is
     * instantiated in {@link #bindValuePropertyWithControl()}
     */
    private ObjectProperty<Boolean> checkboxValue;

    public CheckBoxControl() {
        this("");
    }

    public CheckBoxControl(String propertyName) {
        super(propertyName, new CheckBox());
        checkBox = this.getInputComponent();
        initiateListener();
    }

    private void initiateListener() {
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean newValue) {
                if (!checkBox.isIndeterminate()) {
                    checkboxValue.set(newValue);
                }
            }
        });

        /**
         * If checkbox is indeterminate, set value to null
         */
        checkBox.indeterminateProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (checkBox.isAllowIndeterminate()) {
                    checkboxValue.set(t1 ? null : checkBox.isSelected());
                }
            }
        });

        checkboxValue.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean newValue) {
                checkBox.setText(conf.toString(newValue));
            }
        });
        final InvalidationListener labelInvalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                checkBox.setText(conf.toString(getValue()));
            }
        };

        trueLabel.addListener(labelInvalidationListener);
        falseLabel.addListener(labelInvalidationListener);
        nullLabel.addListener(labelInvalidationListener);
    }

    @Override
    public void setValue(Boolean value) {
        if (checkBox.isAllowIndeterminate()) {
            checkBox.setIndeterminate(value == null);
        }
        if (value == null) {
            checkBox.setSelected(false);
        } else {
            checkBox.setSelected(value);
        }
    }

    @Override
    protected void bindValuePropertyWithControl(CheckBox inputControl) {
        checkboxValue = new SimpleObjectProperty<>();
        value.bind(checkboxValue);
    }

    /**
     * See {@link CheckBox#setIndeterminate(boolean)}. To allow checkbox
     * to have indeterminate state, call {@link #setRequired(false)}.
     * @param bln 
     */
    public final void setIndeterminate(boolean bln) {
        checkBox.setIndeterminate(bln);
    }

    public final boolean isIndeterminate() {
        return checkBox.isIndeterminate();
    }

    /**
     * If not required, the checkbox will support 3 states: true, false, null
     * otherwise it will support only true and false.
     * @param required 
     */
    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
        checkBox.setAllowIndeterminate(!required);
    }

    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    public void setTrueLabel(String trueLabel) {
        this.trueLabel.set(trueLabel);
    }

    public void setFalseLabel(String falseLabel) {
        this.falseLabel.set(falseLabel);
    }

    public void setNullLabel(String nullLabel) {
        this.nullLabel.set(nullLabel);
    }

    public void setLabel(String trueLabel, String falseLabel, String nullLabel) {
        this.trueLabel.set(trueLabel);
        this.falseLabel.set(falseLabel);
        this.nullLabel.set(nullLabel);
    }
    private StringConverter<Boolean> conf = new StringConverter<Boolean>() {
        @Override
        public String toString(Boolean value) {
            if (value == null) {
                return nullLabel.get();
            }
            if (value) {
                return trueLabel.get();
            } else {
                return falseLabel.get();
            }
        }

        @Override
        public Boolean fromString(String string) {
            if (string.equals(nullLabel.get())) {
                return null;
            } else if (string.equals(trueLabel.get())) {
                return true;
            } else {
                return false;

            }
        }
    };

	public final StringProperty textProperty() {
		return checkBox.textProperty();
	}

	public final void setText(String string) {
		checkBox.setText(string);
	}

	public final String getText() {
		return checkBox.getText();
	}

	public final void setMnemonicParsing(boolean bln) {
		checkBox.setMnemonicParsing(bln);
	}

	public final boolean isMnemonicParsing() {
		return checkBox.isMnemonicParsing();
	}

	public final BooleanProperty mnemonicParsingProperty() {
		return checkBox.mnemonicParsingProperty();
	}
	
	
}
