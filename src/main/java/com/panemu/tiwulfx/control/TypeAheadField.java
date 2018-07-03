/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TypeAheadField<T> extends Control {

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

    private BooleanProperty markInvalid = new SimpleBooleanProperty(false);
    private static final String DEFAULT_STYLE_CLASS = "type-ahead-field";

    public TypeAheadField() {
        this(FXCollections.<T>observableArrayList());
    }

    public TypeAheadField(ObservableList<T> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setItems(items);
        setFocusTraversable(false);
        getItems().addListener(new ListChangeListener<T>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends T> change) {
                while (change.next()) {
                    if (change.wasRemoved()) {
                        for (T t : change.getRemoved()) {
                            for (String key : itemMap.keySet()) {
                                if (t != null && t.equals(itemMap.get(key))) {
                                    itemMap.remove(key);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private ObjectProperty<ObservableList<T>> items = new SimpleObjectProperty<ObservableList<T>>(this, "items");

    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    public final ObservableList<T> getItems() {
        return items.get();
    }

    public ObjectProperty<ObservableList<T>> itemsProperty() {
        return items;
    }

    /**
     * This property is intended for communication with LookupField's skin.
     * markInvalidProperty() is set to true if user changes textfield's text.
     * When {@link #valueProperty()#get()} is called and markInvalidProperty is
     * true, validation will be executed to ensure user's input is valid.
     *
     * Developer should not need to use this property.
     *
     * @return
     */
    public BooleanProperty markInvalidProperty() {
        return markInvalid;
    }

    @Override
    public String getUserAgentStylesheet() {
        return TypeAheadField.class.getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm();
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    public void addItem(String Label, T value) {
        itemMap.put(Label, value);
        getItems().add(value);
    }

    public ObjectProperty<T> valueProperty() {
        return this.value;
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
     * Reset display text to match with typeahead field's value. This call will
     * bring original textfield's value as long as the underlying validation
     * method has not been called before due to: 1. call to
     * valueProperty().get() 2. call to getValue() 3. lost focus
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

    private HashMap<String, T> itemMap = new LinkedHashMap<>();

    /**
     * Converts the user-typed input (when the ComboBox is
     * {@link #editableProperty() editable}) to an object of type T, such that
     * the input may be retrieved via the {@link #valueProperty() value}
     * property.
     */
    public ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }
    private ObjectProperty<StringConverter<T>> converter
            = new SimpleObjectProperty<>(this, "converter", new LabelConverter());

    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    private BooleanProperty sortedProperty = new SimpleBooleanProperty(false);

    /**
     *
     * @return @see #setSorted(boolean)
     */
    public boolean isSorted() {
        return sortedProperty.get();
    }

    /**
     * Set whether items are sorted alphabetically. By Default is false which
     * means the order of the items is based on the order they are registered to
     * TypeAheadField.
     *
     * @param sorted set to true to sort the items. Default is false.
     */
    public void setSorted(boolean sorted) {
        sortedProperty.set(sorted);
    }

    /**
     * Property of sorted attribute
     *
     * @return
     * @see #setSorted(boolean)
     */
    public BooleanProperty sortedProperty() {
        return sortedProperty;
    }

    public class LabelConverter extends StringConverter<T> {

        @Override
        public String toString(Object value) {
            if (value == null) {
                return "";
            }
            Set<String> keys = itemMap.keySet();
            for (String label : keys) {
                T obj = (T) itemMap.get(label);
                if (obj.equals(value)) {
                    return label;
                }
            }
            return value.toString();
        }

        @Override
        public T fromString(String value) {
            return itemMap.get(value);
        }
    }

    /**
     * Get Label of selected value
     *
     * @return String label of selected value
     */
    public String getText() {
        return getConverter().toString(getValue());
    }

    /**
     * Get Label of passed value
     *
     * @param value
     * @return String label of passed value
     */
    public String getText(T value) {
        return getConverter().toString(value);

    }

}
