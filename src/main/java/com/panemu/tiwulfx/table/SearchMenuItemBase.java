/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public abstract class SearchMenuItemBase<C> extends SeparatorMenuItem {

    private final ChoiceBox<TableCriteria.Operator> operatorChoice = new ChoiceBox<>();
    private TableCriteria<C> searchCriteria;
    private BaseColumn column;
    private Button btnRemove = new Button(TiwulFXUtil.getLiteral("remove.filter"));

    protected abstract Node getInputControl();

    protected abstract List<Operator> getOperators();

    protected abstract C getValue();

    private EventHandler<ActionEvent> searchButtonListener = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Operator operator = operatorChoice.getSelectionModel().getSelectedItem();

            searchCriteria = column.createSearchCriteria(operator, getValue());

            SearchMenuItemBase.this.getParentPopup().hide();
            column.setTableCriteria(searchCriteria);
        }
    };
    private EventHandler<ActionEvent> removeFilterButtonListener = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            SearchMenuItemBase.this.getParentPopup().hide();
            column.setTableCriteria(null);
        }
    };

    public SearchMenuItemBase(final BaseColumn column) {
        this.column = column;
        setHideOnClick(false);
        operatorChoice.setConverter(operatorConverter);
        operatorChoice.getItems().addAll(getOperators());
        operatorChoice.getSelectionModel().select(0);
        HBox hbox = new HBox(5);
        Node filterImage = TiwulFXUtil.getGraphicFactory().createFilterGraphic();
        Button searchButton = new Button();
        searchButton.getStyleClass().add("table-menu");
        searchButton.setGraphic(filterImage);
        searchButton.setOnAction(searchButtonListener);
        btnRemove.setOnAction(removeFilterButtonListener);
        hbox.getChildren().addAll(operatorChoice, getInputControl(), searchButton);
        hbox.setPrefHeight(-1.0);
        hbox.setAlignment(Pos.CENTER);
        final VBox vbox = new VBox(5);
        vbox.getStylesheets().add(getClass().getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm());
        vbox.getStyleClass().add("search-menu-item");
        vbox.setOpacity(1);
        vbox.setAlignment(Pos.TOP_CENTER);
        Label lblTitle = new Label();

        lblTitle.textProperty().bind(new StringBinding() {
            {
                super.bind(column.textProperty());
            }

            @Override
            protected String computeValue() {
                return TiwulFXUtil.getLiteral("filter.param.column", column.getText());
            }
        });

        lblTitle.getStyleClass().add("searchMenuItemLabel");
        vbox.getChildren().add(lblTitle);
        vbox.getChildren().add(hbox);
        if (column.getTableCriteria() != null) {
            vbox.getChildren().add(btnRemove);
        }
        this.setContent(vbox);
        operatorChoice.getSelectionModel().selectedItemProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                ReadOnlyObjectProperty obj = (ReadOnlyObjectProperty) observable;
                Operator opt = (Operator) obj.get();
                getInputControl().setVisible(!opt.equals(Operator.is_null) && !opt.equals(Operator.is_not_null));
            }
        });

        column.tableCriteriaProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                TableCriteria crit = (TableCriteria) ((SimpleObjectProperty) observable).getValue();
                if (crit == null) {
                    vbox.getChildren().remove(btnRemove);
                } else if (!vbox.getChildren().contains(btnRemove)) {
                    vbox.getChildren().add(btnRemove);
                }
            }
        });

        /**
         * fix the popup that intermittently doesn't show up
         */
        operatorChoice.setFocusTraversable(false);
    }

    public void setSelectedOperator(Operator op) {
        operatorChoice.setValue(op);
    }

    public Operator getSelectedOperator() {
        return operatorChoice.getValue();
    }

    private StringConverter<Operator> operatorConverter = new StringConverter<Operator>() {
        Map<String, Operator> map = new HashMap<>();

        @Override
        public String toString(Operator object) {
            String literal = TiwulFXUtil.getLiteral(object.toString());
            map.put(literal, object);
            return literal;
        }

        @Override
        public Operator fromString(String string) {
            return map.get(string);
        }
    };
}
