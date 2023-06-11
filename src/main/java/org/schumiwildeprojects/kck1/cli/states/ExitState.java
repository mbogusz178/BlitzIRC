package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;

import java.io.IOException;

// Dummy state do zakończenia aplikacji
public class ExitState extends State {

    public ExitState() throws IOException {
        super();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        return null;
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onSubmit() {

    }
}
