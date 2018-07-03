/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class LabelSeparator extends StackPane {

    private Label lblText;

    public LabelSeparator(String label) {
        this(label, true);
    }

    public LabelSeparator(String label, boolean topPading) {
        HBox line = new HBox();
		 line.getStyleClass().add("line");
		 line.setMinHeight(2);
		 line.setPrefHeight(2);
		 line.setPrefWidth(USE_PREF_SIZE);
		 line.setMaxHeight(USE_PREF_SIZE);
		 
        if (topPading) {
            setPadding(new Insets(10, 0, 0, 0));
        }
        lblText = new Label(label);
        this.getChildren().addAll(line, lblText);
        this.getStyleClass().add("label-separator");

    }

    public void setText(String label) {
        textProperty().set(label);
    }

    public String getText() {
        return textProperty().get();
    }

    public StringProperty textProperty() {
        return lblText.textProperty();
    }
}
