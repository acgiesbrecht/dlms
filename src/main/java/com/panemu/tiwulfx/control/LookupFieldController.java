/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableData;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.table.BaseColumn;
import com.panemu.tiwulfx.table.CheckBoxColumn;
import com.panemu.tiwulfx.table.LocalDateColumn;
import com.panemu.tiwulfx.table.NumberColumn;
import com.panemu.tiwulfx.table.TableControl;
import com.panemu.tiwulfx.table.TableControl.Component;
import com.panemu.tiwulfx.table.TableController;
import com.panemu.tiwulfx.table.TextColumn;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Class that serves data loading for LookupField.
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public abstract class LookupFieldController<T> {

    private LookupWindow lookupWindow;
    private Stage dialogStage;
    private Class<T> recordClass;

    /**
     *
     * @param recordClass The class of record/object/POJO that will be displayed
     * in table's row
     */
    public LookupFieldController(Class<T> recordClass) {
        this.recordClass = recordClass;
    }

    public abstract String[] getColumns();

    /**
     * This method is published to serve data displayed on Lookup's suggestion
     * list. Eventually it will call
     * {@link #loadData(int, java.util.List, java.util.List, java.util.List, int)}
     *
     * @param propertyName
     * @param key
     * @return
     */
    public List<T> loadDataForPopup(String propertyName, String key) {
        return loadDataForPopup(propertyName, key, TableCriteria.Operator.ilike_anywhere);
    }

    /**
     * This method is published to serve data displayed on Lookup's suggestion
     * list. Eventually it will call
     * {@link #loadData(int, java.util.List, java.util.List, java.util.List, int)}
     *
     * @param propertyName
     * @param key
     * @param operator
     * @return
     */
    public List<T> loadDataForPopup(String propertyName, String key, TableCriteria.Operator operator) {
        List<TableCriteria> lstCriteria = new ArrayList<>();
        if (key != null && !key.isEmpty()) {
            lstCriteria.add(new TableCriteria(propertyName, operator, key));
        }
        TableData data = loadData(0, lstCriteria, Arrays.asList(propertyName), Arrays.asList(SortType.ASCENDING), TiwulFXUtil.DEFAULT_LOOKUP_SUGGESTION_ITEMS);
        return (List<T>) data.getRows();
    }

    /**
     *
     * @return
     */
    protected String getWindowTitle() {
        return TiwulFXUtil.getLiteral("lookup.title");
    }

    public T show(Window stage, T initialValue, String propertyName) {
        return show(stage, initialValue, propertyName, null);
    }

    /**
     * Show lookup dialog.
     *
     * @param stage parent
     * @param initialValue this value will be returned if user clik the close
     * button instead of double clicking a row or click Select button
     * @param propertyName propertyName corresponds to searchCriteria
     * @param searchCriteria searchCriteria (nullable)
     * @return selected object or the initialValue
     */
    public T show(final Window stage, T initialValue, String propertyName, String searchCriteria) {
        if (dialogStage == null) {
            PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(recordClass);
            lookupWindow = new LookupWindow();
            for (String clm : getColumns()) {
                for (PropertyDescriptor prop : props) {
                    if (prop.getName().equals(clm)) {
                        Class type = prop.getPropertyType();
                        if (type.equals(Boolean.class)) {
                            lookupWindow.table.addColumn(new CheckBoxColumn<T>(clm));
                        } else if (type.equals(String.class)) {
                            lookupWindow.table.addColumn(new TextColumn<T>(clm));
                        } else if (type.equals(Date.class)) {
                            lookupWindow.table.addColumn(new LocalDateColumn<T>(clm));
                        } else if (Number.class.isAssignableFrom(type)) {

                            if (Long.class.isAssignableFrom(type)) {
                                lookupWindow.table.addColumn(new NumberColumn<T, Long>(clm, type));
                            } else {
                                lookupWindow.table.addColumn(new NumberColumn<T, Double>(clm, type));
                            }
                        } else {
                            TableColumn column = new TableColumn();
                            column.setCellValueFactory(new PropertyValueFactory(clm));
                            lookupWindow.table.addColumn(column);
                        }
                        break;
                    }
                }

            }
            dialogStage = new Stage();
            if (stage instanceof Stage) {
                dialogStage.initOwner(stage);
                dialogStage.initModality(Modality.WINDOW_MODAL);
            } else {
                dialogStage.initOwner(null);
                dialogStage.initModality(Modality.APPLICATION_MODAL);
            }
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(true);
            dialogStage.setScene(new Scene(lookupWindow));
            dialogStage.getIcons().add(new Image(LookupFieldController.class.getResourceAsStream("/com/panemu/tiwulfx/res/image/lookup.png")));
            dialogStage.setTitle(getWindowTitle());
            dialogStage.getScene().getStylesheets().add(getClass().getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm());
            initCallback(lookupWindow, lookupWindow.table);
        }

        for (TableColumn column : lookupWindow.table.getTableView().getColumns()) {
            if (column instanceof BaseColumn && ((BaseColumn) column).getPropertyName().equals(propertyName)) {
                if (searchCriteria != null && !searchCriteria.isEmpty()) {
                    TableCriteria tc = new TableCriteria(propertyName, TableCriteria.Operator.ilike_anywhere, searchCriteria);
                    ((BaseColumn) column).setTableCriteria(tc);
                } else {
                    ((BaseColumn) column).setTableCriteria(null);
                }

                break;
            }
        }
        selectedValue = initialValue;
        beforeShowCallback(lookupWindow.table);
        lookupWindow.table.reloadFirstPage();

        if (stage != null) {
            /**
             * Since we support multiple monitors, ensure that the stage is
             * located in the center of parent stage. But we don't know the
             * dimension of the stage for the calculation, so we defer the
             * relocation after the stage is actually displayed.
             */
            Runnable runnable = new Runnable() {
                public void run() {
                    dialogStage.setX(stage.getX() + stage.getWidth() / 2 - dialogStage.getWidth() / 2);
                    dialogStage.setY(stage.getY() + stage.getHeight() / 2 - dialogStage.getHeight() / 2);

                    //set the opacity back to fully opaque
                    dialogStage.setOpacity(1);
                }
            };

            Platform.runLater(runnable);

            //set the opacity to 0 to minimize flicker effect
            dialogStage.setOpacity(0);
        }

        dialogStage.showAndWait();
        return selectedValue;
    }

    /**
     * Override this method to get reference on Lookup container and
     * TableControl object. This is useful to set preferred size of lookup
     * window and set particular column's width. This callback is executed after
     * Lookup Stage along with TableControl are ready to be shown. It is only
     * executed one time. The next lookup dialog show will not be called.
     *
     * @param container
     * @param table
     * @see #beforeShowCallback(com.panemu.tiwulfx.table.TableControl)
     */
    protected void initCallback(VBox container, TableControl<T> table) {
    }

    /**
     * Call back that is called every time the lookup dialog is about to be
     * shown.
     *
     * @param table
     * @see #initCallback(javafx.scene.layout.VBox,
     * com.panemu.tiwulfx.table.TableControl)
     */
    protected void beforeShowCallback(TableControl<T> table) {

    }

    private T selectedValue;

    private void select() {
        selectedValue = lookupWindow.table.getSelectedItem();
        close();

    }

    private void close() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dialogStage.hide();
            }
        });
    }

    protected abstract TableData loadData(int startIndex, List<TableCriteria> filteredColumns, List<String> sortedColumns, List<SortType> sortingTypes, int maxResult);

    private class LookupTableController extends TableController<T> {

        @Override
        public TableData loadData(int startIndex, List<TableCriteria> filteredColumns, List<String> sortedColumns, List<SortType> sortingTypes, int maxResult) {
            return LookupFieldController.this.loadData(startIndex, filteredColumns, sortedColumns, sortingTypes, maxResult);
        }

        @Override
        public void doubleClick(T record) {
            select();
        }
    }

    private class LookupWindow extends VBox {

        TableControl<T> table = new TableControl<>();
        private Button button = new Button(TiwulFXUtil.getLiteral("lookup.select"));

        public LookupWindow() {
            addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        close();
                    }
                }
            });
            HBox pnlButton = new HBox();
            pnlButton.setAlignment(Pos.CENTER);
            pnlButton.setPadding(new Insets(10));
            pnlButton.getChildren().add(button);

            getChildren().addAll(table, pnlButton);
            table.setController(new LookupTableController());
            table.setVisibleComponents(false, Component.BUTTON_DELETE,
                    Component.BUTTON_EDIT,
                    Component.BUTTON_EXPORT,
                    Component.BUTTON_INSERT,
                    Component.BUTTON_SAVE);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    select();
                }
            });
        }
    }
}
