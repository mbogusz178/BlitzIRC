package org.schumiwildeprojects.kck1.cli.states;

import org.schumiwildeprojects.kck1.cli.BasicLateInitWindow;
import org.schumiwildeprojects.kck1.cli.IRCTerminal;
import org.schumiwildeprojects.kck1.cli.LoginWindow;

import java.io.IOException;

// Logowanie
public class LoginState extends State {
    private LoginWindow window;

    public LoginState() throws IOException {
        super();
    }

    @Override
    public BasicLateInitWindow getWindow() {
        window = new LoginWindow("Logowanie");
        return window;
    }

    @Override
    public void onClose() throws IOException {
        terminal.changeState(new ExitState());
    }

    @Override
    public void onSubmit() throws IOException {
        String serverName = window.getServerName();
        String nick = window.getNickname();
        String login = window.getLogin();
        String fullName = window.getFullName();
        String channel = window.getChannel();
        String password = window.getPassword();
        IRCTerminal.currentChannel = channel;
        terminal.initializeConnectionThread(serverName, nick, login, fullName, channel, password);
        terminal.changeState(new ConnectingProgressState());
    }
}
