package org.schumiwildeprojects.kck1.backend;

import org.schumiwildeprojects.kck1.cli.IRCTerminal;

import java.io.IOException;

public class Main {

    public static IRCTerminal ircTerminal;
    public static final String SERVER_URL = "irc.freenode.net";
    public static final int PORT = 6667;

    public static void main(String[] args) throws IOException {
        ircTerminal = IRCTerminal.getInstance();
        ircTerminal.start();
    }
}