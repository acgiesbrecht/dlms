/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control;

import javafx.scene.control.SeparatorMenuItem;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LabelSeparatorMenuItem extends SeparatorMenuItem {

    public LabelSeparatorMenuItem(String label) {
        this(label, true);
    }
    
    public LabelSeparatorMenuItem(String label, boolean topPading) {
        super();
        LabelSeparator content = new LabelSeparator(label, topPading);
        content.setPrefHeight(LabelSeparator.USE_COMPUTED_SIZE);
        content.setMinHeight(LabelSeparator.USE_PREF_SIZE);
        setContent(content);
        
    }
    
}
