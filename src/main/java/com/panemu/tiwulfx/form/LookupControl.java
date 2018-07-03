/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import com.panemu.tiwulfx.control.LookupField;
import com.panemu.tiwulfx.control.LookupFieldController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupControl<R> extends BaseControl<R, LookupField<R>> {
    private LookupField<R> lookupField;
    private StringProperty nestedPropertyName = new SimpleStringProperty();
    
    public LookupControl() {
        super(new LookupField());
        lookupField = getInputComponent();
        lookupField.propertNameProperty().bind(nestedPropertyName);
    }

    public LookupFieldController<R> getController() {            
        return lookupField.getController();
    }

    public void setController(LookupFieldController<R> controller) {
        lookupField.setController(controller);
    }
    

    @Override
    public void setValue(R value) {
        lookupField.setValue(value);
    }

    @Override
    protected void bindValuePropertyWithControl(LookupField<R> inputControl) {
        value.bind(inputControl.valueProperty());
    }

    public String getLookupPropertyName() {
        return nestedPropertyName.get();
    }

    public void setLookupPropertyName(String nestedPropertyName) {
        this.nestedPropertyName.set(nestedPropertyName);
    }

	public String getPromptText() {
		return lookupField.getPromptText();
	}

	public void setPromptText(String promptText) {
		lookupField.setPromptText(promptText);
	}

	public StringProperty promptTextProperty() {
		return lookupField.promptTextProperty();
	}
        
        public void setDisableLookupManualInput(boolean disableLookupManualInput){
                lookupField.setDisableManualInput(disableLookupManualInput);
        }
        
        public boolean getDisableLookupManualInput(){
                return lookupField.getDisableManualInput();
        }
}