/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.skin;

import com.panemu.tiwulfx.control.TypeAheadField;
import com.panemu.tiwulfx.control.behavior.TypeAheadFieldBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TypeAheadFieldSkin<T> extends BehaviorSkinBase<TypeAheadField<T>, TypeAheadFieldBehavior<T>> {

    private static final String PROP_SHOWING_SUGGESTION = "SHOWING_SUGGESTION";
    private static final String PROP_RESETTING_DISPLAY_TEXT = "RESETTING_DISPLAY_TEXT";
    private TextField textField;
    private Button button;
    private PopupControl popup;
    private boolean detectTextChanged = true;
    private Timer waitTimer;
    private LoaderTimerTask loaderTimerTask;
    private TypeAheadField<T> typeAheadField;
    /**
     * flag to
     */
    public boolean needValidation = true;

    public TypeAheadFieldSkin(TypeAheadField<T> control) {
        super(control, new TypeAheadFieldBehavior<>(control));
        this.typeAheadField = control;
        // move focus in to the textfield
        typeAheadField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (hasFocus) {
                    // move focus in to the textfield if the comboBox is editable
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            textField.requestFocus();
                        }
                    });
                }
            }
        });

        initialize();

        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (!hasFocus) {
                    validate();
                }
            }
        });

        textField.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.DOWN) {
                    typeAheadField.fireEvent(ke);
                    //prevent moving caret position to the end
                    ke.consume();
                }
            }
        });
        typeAheadField.addEventFilter(InputEvent.ANY, new EventHandler<InputEvent>() {
            @Override
            public void handle(InputEvent t) {
                if (textField == null) {
                    return;
                }

                // When the user hits the enter or F4 keys, we respond before
                // ever giving the event to the TextField.
                if (t instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) t;
//					if (ke.getCode() == KeyCode.DOWN && ke.getEventType() == KeyEvent.KEY_RELEASED) {
//						if (!typeAheadField.isShowingSuggestion()) {
//							typeAheadField.showSuggestion();
//						}
//						t.consume();
//                        return;
//					} else
                    if ((ke.getCode() == KeyCode.F10 || ke.getCode() == KeyCode.ESCAPE || ke.getCode() == KeyCode.ENTER)
                            && !ke.isControlDown()) {

                        // RT-23275: The TextField fires F10 and ESCAPE key events
                        // up to the parent, which are then fired back at the
                        // TextField, and this ends up in an infinite loop until
                        // the stack overflows. So, here we consume these two
                        // events and stop them from going any further.
                        t.consume();
                        return;
                    }
                }
            }
        });

        textField.promptTextProperty().bind(typeAheadField.promptTextProperty());

        getSkinnable().requestLayout();

        registerChangeListener(control.showingSuggestionProperty(), PROP_SHOWING_SUGGESTION);
        registerChangeListener(control.resettingDisplayTextProperty(), PROP_RESETTING_DISPLAY_TEXT);
    }

    @Override
    protected void handleControlPropertyChanged(String string) {
        super.handleControlPropertyChanged(string);
        if (PROP_SHOWING_SUGGESTION.equals(string)) {
            if (typeAheadField.isShowingSuggestion()) {
                showSuggestion();
            } else {
                hideSuggestion();
            }
        } else if (PROP_RESETTING_DISPLAY_TEXT.equals(string)) {
            if (typeAheadField.isResettingDisplayText()) {
                updateTextField();
            }
        }
    }

    public void hideSuggestion() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }

    private PopupControl getPopup() {
        if (popup == null) {
            createPopup();
        }
        return popup;
    }

    private double getListViewPrefHeight() {
        double ph = 200;
        if (listView.getSkin() instanceof VirtualContainerBase) {
            try {
                int maxRows = 10;

                ListViewSkin<?> skinL = (ListViewSkin<?>) listView.getSkin();
                skinL.updateListViewItems();

                Method methodL = skinL.getClass().getSuperclass().getDeclaredMethod("updateRowCount", null);
                methodL.setAccessible(true);
                methodL.invoke(skinL, null);

                VirtualContainerBase<?, ?, ?> skin = (VirtualContainerBase<?, ?, ?>) listView.getSkin();

                Method method = skin.getClass().getSuperclass().getDeclaredMethod("getVirtualFlowPreferredHeight", Integer.TYPE);
                method.setAccessible(true);
                ph = (double) method.invoke(skin, maxRows);
            } catch (Exception ex) {
                Logger.getLogger(TypeAheadFieldSkin.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            double ch = typeAheadField.getItems().size() * 25;
            ph = Math.min(ch, 200);
        }
        return ph;
    }

    /**
     * Get the reference to the underlying textfield. This method is used by
     * TypeAheadTableCell.
     *
     * @return TextField
     */
    public TextField getTextField() {
        return textField;
    }

    private void createPopup() {
        popup = new PopupControl() {
            {
                setSkin(new Skin() {
                    @Override
                    public Skinnable getSkinnable() {
                        return TypeAheadFieldSkin.this.typeAheadField;
                    }

                    @Override
                    public Node getNode() {
                        return listView;
                    }

                    @Override
                    public void dispose() {
                    }
                });
            }
        };
        popup.setAutoHide(true);
        popup.setOnAutoHide(new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                typeAheadField.hideSuggestion();
            }
        });
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                typeAheadField.hideSuggestion();
            }
        });

        listView.setCellFactory(new Callback() {
            @Override
            public Object call(Object p) {
                return new PropertyListCell();
            }
        });

        /**
         * Taken from
         * {@link com.sun.javafx.scene.control.skin.ComboBoxListViewSkin}
         */
        listView.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                // RT-18672: Without checking if the user is clicking in the
                // scrollbar area of the ListView, the comboBox will hide. Therefore,
                // we add the check below to prevent this from happening.
                EventTarget target = t.getTarget();
                if (target instanceof Parent) {
                    List<String> s = ((Parent) target).getStyleClass();
                    if (s.contains("thumb")
                            || s.contains("track")
                            || s.contains("decrement-arrow")
                            || s.contains("increment-arrow")) {
                        return;
                    }
                }
                needValidation = false;
                typeAheadField.setValue(listView.getSelectionModel().getSelectedItem());
                typeAheadField.hideSuggestion();
            }
        });

        listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    needValidation = false;
                    if (listView.getSelectionModel().getSelectedItem() != null) {
                        /**
                         * By default, select the first item if none is selected
                         */
                        if (listView.getSelectionModel().getSelectedItem() == typeAheadField.getValue()) {
                            /**
                             * Update the textfield. User may have changed it.
                             */
                            updateTextField();
                        } else {
                            typeAheadField.setValue(listView.getSelectionModel().getSelectedItem());
                            /**
                             * The textfield will be updated by value change
                             * listener
                             */
                        }

                    } else if (!listView.getItems().isEmpty()) {
                        T defaultItem = listView.getItems().get(0);
                        if (defaultItem == typeAheadField.getValue()) {
                            /**
                             * Update the textfield. User may have changed it.
                             */
                            updateTextField();
                        } else {
                            typeAheadField.setValue(defaultItem);
                            /**
                             * The textfield will be updated by value change
                             * listener
                             */
                        }
                    }

                    typeAheadField.hideSuggestion();
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    typeAheadField.hideSuggestion();
                } else if (t.getCode() == KeyCode.RIGHT) {
                    textField.positionCaret(textField.getCaretPosition() + 1);
                    refreshList();
                    t.consume();
                } else if (t.getCode() == KeyCode.LEFT) {
                    textField.positionCaret(textField.getCaretPosition() - 1);
                    refreshList();
                    t.consume();
                } else if (t.getCode() == KeyCode.TAB) {
                    typeAheadField.hideSuggestion();
                    if (textField.getSkin() instanceof BehaviorSkinBase) {
                        /**
                         * Move to the next control. It will trigger action to
                         * select first matched item from the listview
                         */
                        BehaviorSkinBase bsb = (BehaviorSkinBase) textField.getSkin();
                        bsb.getBehavior().traverseNext();
                    }
                }
            }
        });

    }

    private void initialize() {
        textField = new TextField();
        textField.setFocusTraversable(true);

        button = new Button();
        button.setFocusTraversable(false);
        StackPane arrow = new StackPane();
        arrow.setFocusTraversable(false);
        arrow.getStyleClass().setAll("arrow");
        arrow.setMaxWidth(USE_PREF_SIZE);
        arrow.setMaxHeight(USE_PREF_SIZE);

        button.setGraphic(arrow);
        StackPane.setAlignment(textField, Pos.CENTER_LEFT);
        StackPane.setAlignment(button, Pos.CENTER_RIGHT);
        this.getChildren().addAll(textField, button);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (!typeAheadField.isFocused()) {
                    /**
                     * Need to make this control become focused. Otherwise
                     * changing value in LookupColumn while the LookuField cell
                     * editor is not focused before, won't trigger commitEdit()
                     */
                    typeAheadField.requestFocus();
                }
                typeAheadField.showSuggestion();
            }
        });
        updateTextField();
        typeAheadField.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                updateTextField();
            }
        });

        typeAheadField.markInvalidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue && !newValue && needValidation) {
                    validate();
                }
            }
        });

        textField.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                if (detectTextChanged) {
                    if (waitTimer != null) {
                        loaderTimerTask.setObsolete(true);
                        waitTimer.cancel();
                        waitTimer.purge();
                    }

                    typeAheadField.markInvalidProperty().set(true);
                    needValidation = true;

                    waitTimer = new Timer("lookupTimer");
                    loaderTimerTask = new LoaderTimerTask(waitTimer);
                    waitTimer.schedule(loaderTimerTask, 100);
                }
            }
        });

    }

    private void updateTextField() {
        detectTextChanged = false;
        needValidation = false;
        if (typeAheadField.getValue() == null) {
            textField.setText("");
            typeAheadField.markInvalidProperty().set(false);
            detectTextChanged = true;
            return;
        }
        T value = getSkinnable().getValue();
        if (value != null) {
            String string = getSkinnable().getConverter().toString(value);
            if (string == null) {
                textField.setText("");
            } else {
                textField.setText(string);
            }
        } else {
            textField.setText("");
        }
        typeAheadField.markInvalidProperty().set(false);
        detectTextChanged = true;
    }

    private Point2D getPrefPopupPosition() {
        Point2D p = getSkinnable().localToScene(0.0, 0.0);
        Point2D p2 = new Point2D(p.getX() + getSkinnable().getScene().getX() + getSkinnable().getScene().getWindow().getX(), p.getY() + getSkinnable().getScene().getY() + getSkinnable().getScene().getWindow().getY() + getSkinnable().getHeight());
        return p2;
    }

    private void positionAndShowPopup() {
        if (getPopup().getSkin() == null) {
            getSkinnable().getScene().getRoot().impl_processCSS(true);
        }

        Point2D p = getPrefPopupPosition();

        /**
         * In LookupColumn, sometimes the lookupfield disappears due to commit
         * editing before the popup appears. In this case,
         * lookupField.getScene() will be null.
         */
        Scene scene = typeAheadField.getScene();
        if (scene != null) {
            getPopup().show(scene.getWindow(), p.getX(), p.getY());
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h) {

        double obw = button.prefWidth(-1);

        double displayWidth = getSkinnable().getWidth()
                - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight() + obw);

        textField.resizeRelocate(x, y, w, h);
        button.resizeRelocate(x + displayWidth, y, obw, h);
    }

    private List<T> getData() {
        List<T> items = getSkinnable().getItems();
        StringConverter<T> converter = getSkinnable().getConverter();
        List<T> eligibleItems = new ArrayList<>();
        String text = textField.getText().substring(0, textField.getCaretPosition());
        if (!getSkinnable().isSorted() && (text == null || text.length() == 0)) {
            return items;
        }

        if (getSkinnable().isSorted()) {
            List<String> lstEligibleString = new ArrayList<>();
            for (T item : items) {
                String label = converter.toString(item);
                //if (label != null && label.toLowerCase().startsWith(text.toLowerCase())) {
                if (label != null && label.toLowerCase().contains(text.toLowerCase())) {
                    lstEligibleString.add(label);
                }
            }
            Collections.sort(lstEligibleString);
            for (String string : lstEligibleString) {
                eligibleItems.add(converter.fromString(string));
            }
        } else {
            for (T item : items) {
                String label = converter.toString(item);
                //if (label != null && label.toLowerCase().startsWith(text.toLowerCase())) {
                if (label != null && label.toLowerCase().contains(text.toLowerCase())) {
                    eligibleItems.add(item);
                }
            }
        }
        return eligibleItems;
    }

    private void validate() {
        if (needValidation) {
            if (!textField.getText().isEmpty()) {
                loaderTimerTask.setObsolete(true);
                List<T> data = getData();
                if (data.size() > 0) {
                    if (typeAheadField.getValue() == data.get(0)) {
                        updateTextField();
                    } else {
                        typeAheadField.setValue(data.get(0));
                    }
                } else if (typeAheadField.getValue() == null) {
                    //need to update text field since value change listener
                    //doesn't detect any change.
                    updateTextField();
                } else {
                    //the text field will be updated by value change listener
                    typeAheadField.setValue(null);
                }
            } else {
                typeAheadField.setValue(null);
            }
        }
    }

    private void showSuggestion() {
        List<T> data = getData();
        listView.getItems().clear();
        listView.getItems().addAll(data);
        boolean dummyBag = needValidation;
        needValidation = false;
        if (getSkinnable().getValue() == null) {
            listView.getSelectionModel().selectFirst();
        } else {
            listView.getSelectionModel().select(getSkinnable().getValue());
        }
        needValidation = dummyBag;
        positionAndShowPopup();
    }

    private void refreshList() {
        List<T> data = getData();
        listView.getItems().clear();
        listView.getItems().addAll(data);
    }

    private class LoaderTimerTask extends TimerTask {

        private boolean obsolete = false;
        private Timer timer;

        public LoaderTimerTask(Timer timer) {
            this.timer = timer;
        }

        public void setObsolete(boolean obsolete) {
            this.obsolete = obsolete;
        }

        @Override
        public void run() {
            if (!obsolete) {
                final List<T> data = getData();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!obsolete) {
                            listView.getItems().clear();
                            if (!data.isEmpty()) {
                                listView.getItems().addAll(data);
                                typeAheadField.showSuggestion();
                            }
                        }
                    }
                });
            }
            timer.cancel();
            timer.purge();
        }
    }
    private ListView<T> listView = new ListView<T>() {
        @Override
        protected double computeMinHeight(double width) {
            return 30;
        }

        @Override
        protected double computePrefWidth(double height) {
            double pw = 0;
            if (getSkin() instanceof ListViewSkin) {
                ListViewSkin<?> skin = (ListViewSkin<?>) getSkin();

                int rowsToMeasure = -1;

//				pw = Math.max(comboBox.getWidth(), skin.getMaxCellWidth(rowsToMeasure) + 30);
                Method method;
                try {
                    method = skin.getClass().getSuperclass().getDeclaredMethod("getMaxCellWidth", Integer.TYPE);

                    method.setAccessible(true);
                    double rowWidth = (double) method.invoke(skin, rowsToMeasure);
                    pw = Math.max(typeAheadField.getWidth(), rowWidth + 30);
                } catch (Exception ex) {
                    Logger.getLogger(TypeAheadFieldSkin.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                pw = Math.max(100, typeAheadField.getWidth());
            }

            // need to check the ListView pref height in the case that the
            // placeholder node is showing
            if (getItems().isEmpty() && getPlaceholder() != null) {
                pw = Math.max(super.computePrefWidth(height), pw);
            }

            return Math.max(50, pw);
        }

        @Override
        protected double computePrefHeight(double width) {

            return getListViewPrefHeight();
        }
    };

    private class PropertyListCell extends ListCell<T> {

        @Override
        protected void updateItem(T t, boolean bln) {
            super.updateItem(t, bln);

            if (t != null) {
                StringConverter<T> converter = getSkinnable().getConverter();
                String value = converter.toString(t);
                if (value != null) {
                    setText(value.toString());
                } else {
                    setText("");
                }
            }
        }
    }
}
