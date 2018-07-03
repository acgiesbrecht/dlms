/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.form;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TextControl extends BaseControl<String, TextField> {

    private TextField textField;

    public TextControl() {
        this("");
    }

    public TextControl(String propertyName) {
        super(propertyName, new TextField());
        textField = getInputComponent();
        value.bind(textField.textProperty());
    }

    @Override
    public void setValue(String value) {
        textField.textProperty().set(value);
    }

    @Override
    protected void bindValuePropertyWithControl(TextField inputControl) {
        value.bind(inputControl.textProperty());
    }

    @Override
    protected void bindEditablePropertyWithControl(TextField inputControl) {
        inputControl.editableProperty().bind(editableProperty());
    }

    public void setTextFormatter(TextFormatter textFormatter) {
        textField.setTextFormatter(textFormatter);
    }

}
