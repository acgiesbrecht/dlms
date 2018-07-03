/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.sidemenu;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class SideMenuCategory extends SideMenuItem {
    private List<SideMenuItem> lstMenuItem = new ArrayList<SideMenuItem>();
    public SideMenuCategory(String imageStyle, String label) {
        super(imageStyle, label, null);
    }
    
    public void addMainMenuItem(SideMenuItem ... menuItem) {
        for (SideMenuItem item : menuItem) {
            lstMenuItem.add(item);
        }
    }

    public List<SideMenuItem> getMenuItems() {
        return lstMenuItem;
    }
}
