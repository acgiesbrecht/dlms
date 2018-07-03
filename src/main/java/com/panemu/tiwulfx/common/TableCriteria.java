/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.common;

import java.io.Serializable;

/**
 *
 * @author amrullah
 */
public class TableCriteria<T> implements Serializable {

    public static enum Operator {

        in("in"), 
        not_in("not.in"), 
        eq("="),
        ne("<>"), 
        le("<="), 
        lt("<"), 
        ge(">="), 
        gt(">"), 
        is_null("null"),
        is_not_null("not.null"), 
        like_begin("start.with"),
        like_end("end.with"),
        like_anywhere("contains"),
        ilike_begin("start.with"),
        ilike_end("end.with"),
        ilike_anywhere("contains");
        private String description;
        private Operator(String desc) {
            this.description = desc;
        }

        @Override
        public String toString() {
            return description;
        }
    }
    private String attributeName;
    private Operator operator;
    private T value;

    public TableCriteria() {
    }

    public TableCriteria(String attributeName, Operator operator, T value) {
        this.attributeName = attributeName;
        this.operator = operator;
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
