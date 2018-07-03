/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupField<T> extends Control {

    private LookupFieldController<T> controller;
    private ObjectProperty<T> value = new SimpleObjectProperty<T>() {
        private boolean skipValidation = false;
        @Override
        public T get() {
            if (!skipValidation && markInvalid.get()) {
                //it will trigger validation in LookupFieldSkin
                markInvalid.set(false);
            }
            return super.get();
        }

        @Override
        public void set(T v) {
            skipValidation = true;
            super.set(v);
            skipValidation = false;
        }
        
    };
    private StringProperty propertyName = new SimpleStringProperty();
    private int waitTime = TiwulFXUtil.DEFAULT_LOOKUP_SUGGESTION_WAIT_TIMES;
    private BooleanProperty markInvalid = new SimpleBooleanProperty(false);
    private BooleanProperty disableManualInput = new SimpleBooleanProperty(false);

    public LookupField() {
        getStyleClass().add("lookup-field");
        setFocusTraversable(false);
    }
    
    /**
     * This property is intended for communication with LookupField's skin.
     * markInvalidProperty() is set to true if user changes textfield's text. When 
     * {@link #valueProperty() valueProperty().get()}  is called and markInvalidProperty is true, validation
     * will be executed to ensure user's input is valid.
     * 
     * Developer should not need to use this property.
     * @return 
     */
    public BooleanProperty markInvalidProperty() {
        return markInvalid;
    }

    @Override
    public String getUserAgentStylesheet() {
        return LookupField.class.getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm();
    }

    public LookupFieldController<T> getController() {
        return controller;
    }

    public void setController(LookupFieldController<T> controller) {
        this.controller = controller;
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    public ObjectProperty<T> valueProperty() {
        return this.value;
    }

    public String getPropertyName() {
        return propertyName.get();
    }

    public void setPropertyName(String propertyName) {
        this.propertyName.set(propertyName);
    }

    public StringProperty propertNameProperty() {
        return propertyName;
    }
   
	/**
	 * Set it TRUE to restrict input only from lookupWindow and disable user
	 * manual input.
	 *
	 * @param disableManualInput
	 */
	public void setDisableManualInput(boolean disableManualInput) {
		this.disableManualInput.set(disableManualInput);
	}

	public boolean getDisableManualInput() {
		return disableManualInput.get();
	}
    
    public BooleanProperty propertyDisableProperty() {
            return disableManualInput;
    }

    /**
     * Set wait time in millisecond for showing suggestion list. Set it to -1 to
     * disable suggestion list feature.
     *
     * @param waitTime Default is 500 millisecond
     */
    public void setShowSuggestionWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getShowSuggestionWaitTime() {
        return this.waitTime;
    }

    /////////////////////////////////////////////////////
    // Manage to display lookup dialog
    ////////////////////////////////////////////////////
    private ReadOnlyBooleanWrapper showingLokupDialog;
    /**
     * 
     */
    public void showLookupDialog() {
        setShowingLookupDialog(true);
        setShowingLookupDialog(false);
    }
    
    private ReadOnlyBooleanWrapper showingLookupDialogPropertyImpl() {
        if (showingLokupDialog == null) {
            showingLokupDialog = new ReadOnlyBooleanWrapper(false);
        }
        return showingLokupDialog;
    }

    public ReadOnlyBooleanProperty showingLookupDialogProperty() {
        return showingLookupDialogPropertyImpl().getReadOnlyProperty();
    }

    private void setShowingLookupDialog(boolean value) {
        showingLookupDialogPropertyImpl().set(value);
    }
    public final boolean isShowingLookupDialog() {
        return showingLookupDialogPropertyImpl().get();
    }
    
    
    //////////////////////////////////////////////////////
    // Reset Display Text
    //////////////////////////////////////////////////////
    private ReadOnlyBooleanWrapper resettingDisplayText;
    private ReadOnlyBooleanWrapper resettingDisplayTextPropertyImpl() {
        if (resettingDisplayText == null) {
            resettingDisplayText = new ReadOnlyBooleanWrapper();
        }
        return resettingDisplayText;
    }
    public ReadOnlyBooleanProperty resettingDisplayTextProperty() {
        return resettingDisplayTextPropertyImpl().getReadOnlyProperty();
    }
    private void setResettingDisplayText(boolean value) {
        resettingDisplayTextPropertyImpl().set(true);
    }
    public boolean isResettingDisplayText() {
        return resettingDisplayTextPropertyImpl().get();
    }
    
    /**
     * Reset display text to match with lookup field's value.
     * This call will bring original textfield's value as long as the underlying
     * validation method has not been called before due to:
     * 1. call to valueProperty().get()
     * 2. call to getValue()
     * 3. lost focus
     */
    public void resetDisplayText() {
        setResettingDisplayText(true);
        setResettingDisplayText(false);
    }

    ///////////////////////////////////////////////
    // Manage to display suggestion popup list
    ///////////////////////////////////////////////

    private ReadOnlyBooleanWrapper showingSuggestionPropertyImpl() {
        if (showing == null) {
            showing = new ReadOnlyBooleanWrapper(false);
        }
        return showing;
    }

    public ReadOnlyBooleanProperty showingSuggestionProperty() {
        return showingSuggestionPropertyImpl().getReadOnlyProperty();
    }

    private void setShowingSuggestion(boolean value) {
        showingSuggestionPropertyImpl().set(value);
    }
    public final boolean isShowingSuggestion() {
        return showingSuggestionPropertyImpl().get();
    }
    
    private ReadOnlyBooleanWrapper showing;

    public void showSuggestion() {
        setShowingSuggestion(true);
    }

    public void hideSuggestion() {
        setShowingSuggestion(false);
    }
	
	private StringProperty promptText = new SimpleStringProperty("");
	
	public String getPromptText() {
		return promptText.get();
	}
	
	public void setPromptText(String promptText) {
		this.promptText.set(promptText);
	}
	
	public StringProperty promptTextProperty() {
		return promptText;
	}
}
