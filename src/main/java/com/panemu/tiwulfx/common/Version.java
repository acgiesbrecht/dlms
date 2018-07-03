/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.common;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class Version {
    public static void main(String[] args) {
        System.out.println(Version.class.getPackage().getImplementationTitle() + " " + Version.class.getPackage().getImplementationVersion());
    }
}
