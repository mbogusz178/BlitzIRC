package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.gui2.BasicWindow;

import java.io.IOException;

public abstract class BasicLateInitWindow extends BasicWindow {
    protected int returnVal;

    public BasicLateInitWindow(String title) {
        super(title);
    }

    public abstract void start() throws IOException;
    public final int returnValue() {
        return returnVal;
    }
}
