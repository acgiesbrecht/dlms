/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.sidemenu;


/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class SideMenuItem {
    private String imageStyle;
    private String label;
    private String actionName;
    
    private boolean isPane;
    private String frmClass;
    
    public SideMenuItem(String imageStyle, String label, String actionName) {
        this.imageStyle = imageStyle;
        this.label = label;
        this.actionName = actionName;
    }

    public String getImageStyle() {
        return imageStyle;
    }

    public void setImageStyle(String imageStyle) {
        this.imageStyle = imageStyle;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

	public boolean isPane() {
		return isPane;
	}

	public void setPane(boolean isPane) {
		this.isPane = isPane;
	}

	public String getFrmClass() {
		return frmClass;
	}

	public void setFrmClass(String frmClass) {
		this.frmClass = frmClass;
	}

}
