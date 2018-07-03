/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.NumberField;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author amrullah
 */
public class NumberColumn<R, C extends Number> extends BaseColumn<R, C> {

    private boolean grouping = true;
    private int maxLength = 10;
    private int digitBehindDecimal;
    private ObjectProperty<Class<C>> clazzProperty = new SimpleObjectProperty<>();
    private NumberField<C> searchInputControl = new NumberField<>(null);
    protected String pattern = "###,###";
    private String zeroDigit = "";
    private BooleanProperty negativeAllowed = new SimpleBooleanProperty(TiwulFXUtil.DEFAULT_NEGATIVE_ALLOWED);
    protected DecimalFormat formatter = TiwulFXUtil.getDecimalFormat();
    private SearchMenuItemBase<C> searchMenuItem = new SearchMenuItemBase<C>(this) {
        @Override
        protected Node getInputControl() {
            return searchInputControl;
        }

        @Override
        protected List<Operator> getOperators() {
            List<Operator> lst = new ArrayList<>();
            lst.add(Operator.eq);
            lst.add(Operator.ne);
            lst.add(Operator.lt);
            lst.add(Operator.le);
            lst.add(Operator.gt);
            lst.add(Operator.ge);
            lst.add(Operator.is_null);
            lst.add(Operator.is_not_null);
            return lst;
        }

        @Override
        protected C getValue() {
            return searchInputControl.getValue();
        }
    };

    public NumberColumn() {
        this("", (Class<C>) Double.class);
    }

    public NumberColumn(String propertyName, Class<C> clazz) {
        this(propertyName, clazz, 100);
    }

    public NumberColumn(String propertyName, Class<C> clazz, double prefWidth) {
        super(propertyName, prefWidth);
        setAlignment(Pos.BASELINE_RIGHT);
        this.clazzProperty.set(clazz);
        Callback<TableColumn<R, C>, TableCell<R, C>> cellFactory
                = new Callback<TableColumn<R, C>, TableCell<R, C>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new NumberTableCell<R, C>(NumberColumn.this);
            }
        };
        setCellFactory(cellFactory);
        formatter.setParseBigDecimal(clazz.equals(BigDecimal.class));
        formatter.applyPattern(getPattern(grouping));
        searchInputControl.numberTypeProperty().bind(this.clazzProperty);
        this.clazzProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                formatter.applyPattern(getPattern(grouping));
            }
        });
        setStringConverter(stringConverter);

        digitBehindDecimal = TiwulFXUtil.DEFAULT_DIGIT_BEHIND_DECIMAL;
        for (int i = 0; i < digitBehindDecimal; i++) {
            zeroDigit = zeroDigit + "0";
        }
        setGrouping(grouping);
    }

    /**
     *
     * @return true if the NumberField display thousand separator
     */
    public boolean isGrouping() {
        return grouping;
    }

    @Override
    MenuItem getSearchMenuItem() {
        if (getDefaultSearchValue() != null) {
            searchInputControl.setValue(getDefaultSearchValue());
        }
        return searchMenuItem;
    }

    /**
     * Set whether it will use thousand separator or not
     *
     * @param grouping set it to true to use thousand separator
     */
    public void setGrouping(boolean grouping) {
        this.grouping = grouping;
        pattern = getPattern(grouping);
        formatter.applyPattern(pattern);
    }
    private StringConverter<C> stringConverter = new StringConverter<C>() {
        @Override
        public String toString(C value) {
            return value == null ? getNullLabel() : formatter.format(value);
        }

        @Override
        public C fromString(String stringValue) {
            if (stringValue == null || stringValue.equals(getNullLabel())) {
                return null;
            }
            return searchInputControl.castToExpectedType(stringValue);
        }
    };

    /**
     * Set maximum character length is acceptable in input field
     *
     * @param maxLength
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    protected String getPattern(boolean grouping) {
        if (clazzProperty.get().equals(Integer.class) || clazzProperty.get().equals(Long.class)) {
            if (grouping) {
                formatter.setParseBigDecimal(clazzProperty.get().equals(BigDecimal.class));
                return "###,###";
            } else {
                return "###";
            }
        } else {
            if (grouping) {
                formatter.setParseBigDecimal(clazzProperty.get().equals(BigDecimal.class));
                return "###,##0." + zeroDigit;
            } else {
                return "##0." + zeroDigit;
            }
        }
    }

    public Class<C> getNumberType() {
        return clazzProperty.get();
    }

    public void setNumberType(Class<C> numberType) {
        this.clazzProperty.set(numberType);
    }

    public void setDigitBehindDecimal(int digitBehindDecimal) {
        this.digitBehindDecimal = digitBehindDecimal;
        if (digitBehindDecimal > 2) {
            zeroDigit = "";
            for (int i = 0; i < digitBehindDecimal; i++) {
                zeroDigit = zeroDigit + "0";
            }
        }
        setGrouping(grouping);
    }

    public int getDigitBehindDecimal() {
        return digitBehindDecimal;
    }

    /**
     * Allow negative number. When it is set to true, user can type - as first
     * character. To change application wide value, see
     * {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
     * <p>
     * @param allowNegative pass true to allow negative value. Default is
     * {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
     */
    public void setNegativeAllowed(boolean allowNegative) {
        this.negativeAllowed.set(allowNegative);
    }

    /**
     * Check if this NumberField allow negative value. Default value is
     * {@link TiwulFXUtil#DEFAULT_NEGATIVE_ALLOWED}
     *
     * @return true if allow negative number.
     */
    public boolean isNegativeAllowed() {
        return negativeAllowed.get();
    }
}
