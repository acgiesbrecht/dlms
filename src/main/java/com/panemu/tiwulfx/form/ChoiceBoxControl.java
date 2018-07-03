/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panemu.tiwulfx.form;

import java.util.LinkedHashMap;
import java.util.Set;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

/**
 *
 * @author sencaki
 */
public class ChoiceBoxControl<R> extends BaseControl<R, ChoiceBox<R>> {

    private LinkedHashMap<String, R> itemMap = new LinkedHashMap<>();
    private LabelConverter lblConverter = new LabelConverter();
    private ChoiceBox<R> choicebox;
    public ChoiceBoxControl() {
        super(new ChoiceBox<R>());
        choicebox = getInputComponent();
        choicebox.setConverter(lblConverter);
    }

    private String getLabel(R object) {
        Set<String> keys = itemMap.keySet();
        for (String label : keys) {
            R obj = (R) itemMap.get(label);
            if (obj.equals(object)) {
                return label;
            }
        }
        return null;
    }

    @Override
    public void setValue(R value) {
//        for (T item : choicebox.getItems()) {
//            if (item.equals(value)) {
//                choicebox.getSelectionModel().select(item);
//                break;
//            }
//        }
        choicebox.setValue(value);
    }

    @Override
    protected void bindValuePropertyWithControl(ChoiceBox<R> inputControl) {
        value.bind(inputControl.valueProperty());
    }

    public class LabelConverter extends StringConverter<R> {

        @Override
        public String toString(R value) {
            return getLabel((R) value);
        }

        @Override
        public R fromString(String value) {
            return itemMap.get(value);
        }
    }

    public void addItem(String label, R value) {
        itemMap.put(label, value);
        choicebox.getItems().add(value);
    }
}
