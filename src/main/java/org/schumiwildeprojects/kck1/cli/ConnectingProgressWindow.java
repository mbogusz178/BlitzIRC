package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import org.schumiwildeprojects.kck1.backend.ConnectionState;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectingProgressWindow extends BasicLateInitWindow {
    private static IRCTerminal terminal;

    public ConnectingProgressWindow(String title) {
        super(title);
        setFixedSize(new TerminalSize("Trwa łączenie z ...".length() + IRCTerminal.currentChannel.length(), 1));
    }

    @Override
    public void start() throws IOException {
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));

        Label connectingLabel = new Label("Trwa łączenie z " + IRCTerminal.currentChannel + "...");
        setComponent(connectingLabel);

        terminal = IRCTerminal.getInstance();

        returnVal = 0;

        terminal.initializeResultThread(() -> {
            try {
                terminal.getConnectionThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ConnectionState state = terminal.getConnectionRunnable().getResult();
            if(state == ConnectionState.SUCCESSFUL) {
                returnVal = 1;
            } else {
                Logger.getLogger(ConnectingProgressWindow.class.getName()).log(Level.WARNING, state.getMsg());
                MessageDialog.showMessageDialog(getTextGUI(), "Błąd", state.getMsg());
                returnVal = 0;
            }
            close();
        });
    }
}
