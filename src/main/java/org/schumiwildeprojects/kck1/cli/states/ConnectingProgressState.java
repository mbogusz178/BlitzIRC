package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.ConnectingProgressWindow;

import java.io.IOException;

public class ConnectingProgressState extends State {

    public ConnectingProgressState() throws IOException {
        super();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        return new ConnectingProgressWindow("Czekaj");
    }

    @Override
    public void onClose() throws IOException {
        terminal.changeState(new LoginState());
    }

    @Override
    public void onSubmit() throws IOException {
        terminal.changeState(new MainCommState());
    }
}
