package org.schumiwildeprojects.kck1.cli.states;

import com.googlecode.lanterna.gui2.BasicWindow;
import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.IRCTerminal;

import java.io.IOException;

public abstract class State {
    IRCTerminal terminal;
    public State() throws IOException {
        terminal = IRCTerminal.getInstance();
    }

    public abstract BasicLateInitWindow getWindow() throws IOException;
    public abstract void onClose() throws IOException;
    public abstract void onSubmit() throws IOException;
    public void onRetry() throws IOException {}
}
