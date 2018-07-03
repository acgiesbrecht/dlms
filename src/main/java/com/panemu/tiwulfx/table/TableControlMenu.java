/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.LabelSeparatorMenuItem;
import com.panemu.tiwulfx.control.NumberField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
class TableControlMenu extends MenuButton {

    private TableControl tableControl;

    public TableControlMenu(final TableControl tableControl) {
        this.tableControl = tableControl;
        getStyleClass().add("table-menu");
        setGraphic(TiwulFXUtil.getGraphicFactory().createConfigGraphic());
        final ToggleGroup toggleGroup = new ToggleGroup();
        final RadioMenuItem normalEditing = new RadioMenuItem(TiwulFXUtil.getLiteral("normal"));
        normalEditing.setUserData(Boolean.FALSE);
        normalEditing.setToggleGroup(toggleGroup);
        normalEditing.setSelected(!tableControl.isAgileEditing());
        final RadioMenuItem agileEditing = new RadioMenuItem(TiwulFXUtil.getLiteral("agile"));
        agileEditing.setUserData(Boolean.TRUE);
        agileEditing.setToggleGroup(toggleGroup);
        agileEditing.setSelected(tableControl.isAgileEditing());
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (toggleGroup.getSelectedToggle() != null) {
                    Boolean agile = (Boolean) toggleGroup.getSelectedToggle().getUserData();
                    tableControl.setAgileEditing(agile);
                }
            }
        });
        
        LabelSeparatorMenuItem labelEditing = new LabelSeparatorMenuItem(TiwulFXUtil.getLiteral("editing.mode"), false);
        this.getItems().addAll(labelEditing, normalEditing, agileEditing);
        
        tableControl.agileEditingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                agileEditing.setSelected(newValue);
                normalEditing.setSelected(!newValue);
            }
        });
        
        createMaxRecordMenuItem();
        createMiscMenu();
        setFocusTraversable(false);
        setPopupSide(Side.TOP);
    }

    private boolean ignore = false;
    private void createMaxRecordMenuItem() {
        ////////////////////////////////
        // Max Row
        ////////////////////////////////
        
        final NumberField<Integer> txtMaxRow = new NumberField<>(Integer.class);
        txtMaxRow.setValue(tableControl.getMaxRecord());
        txtMaxRow.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                ignore = true;
                int max = newValue == null || newValue.intValue() == 0 ? 1 : newValue;
                tableControl.setMaxRecord(max);
                ignore = false;
            }
        });
        tableControl.maxRecordProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (ignore) {
                    return;
                }
                txtMaxRow.setValue((Integer) newValue);
            }
        });
        txtMaxRow.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                tableControl.reloadFirstPage();
            }
        });
		  HBox hbox = new HBox();
		  hbox .getChildren().add(txtMaxRow);
		  hbox.setPadding(Insets.EMPTY);
        CustomMenuItem miMaxRecord = new CustomMenuItem(hbox, false);
        LabelSeparatorMenuItem labelMaxRecord = new LabelSeparatorMenuItem(TiwulFXUtil.getLiteral("max.record"));
        this.getItems().addAll(labelMaxRecord, miMaxRecord);
    }
    
    private void createMiscMenu() {
        LabelSeparatorMenuItem labelMaxRecord = new LabelSeparatorMenuItem(TiwulFXUtil.getLiteral("misc"));
        MenuItem miClearTableCriteria = new MenuItem(TiwulFXUtil.getLiteral("remove.all.filters"));
        miClearTableCriteria.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                tableControl.clearTableCriteria();
            }
        });
        this.getItems().addAll(labelMaxRecord, miClearTableCriteria);
    }
}
