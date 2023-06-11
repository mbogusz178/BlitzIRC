package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.IRCTerminal;
import org.schumiwildeprojects.kck1.cli.MainCommWindow;

import java.io.IOException;

public class MainCommState extends State {
    private MainCommWindow window;

    public MainCommState() throws IOException {
        super();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        window = new MainCommWindow("Komunikacja na " + IRCTerminal.currentChannel);
        return window;
    }

    @Override
    public void onClose() throws IOException {
        window.leaveChannel();
        terminal.changeState(new ExitState());
    }

    @Override
    public void onSubmit() throws IOException {
        window.leaveChannel();
        terminal.changeState(new LoginState());
    }

    @Override
    public void onRetry() throws IOException {
        terminal.initializeConnectionThread(IRCTerminal.serverName, IRCTerminal.nickname, IRCTerminal.currentLogin, IRCTerminal.currentFullName, IRCTerminal.currentChannel, IRCTerminal.currentPassword);
        terminal.changeState(new ConnectingProgressState());
    }
}
