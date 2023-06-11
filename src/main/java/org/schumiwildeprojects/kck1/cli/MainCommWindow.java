package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import org.schumiwildeprojects.kck1.cli.widgets.MessageInputTextBox;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainCommWindow extends BasicLateInitWindow {
    private boolean hasQuit;
    private IRCTerminal terminal;
    private TextBox messages;
    private MessageInputTextBox msgInput;
    private TextBox userList;

    private class Server implements Runnable {

        String line;

        public String readLines() throws Exception {
            return (IRCTerminal.reader.readLine());
        }

        public void run() {
            try {
                // Keep reading lines from the server.
                while (IRCTerminal.appIsOpen && (line = readLines()) != null) {
                    if (IRCTerminal.appIsOpen && line.toLowerCase().startsWith("ping ")) {
                        try {
                            // Odpowiedź na pingi ze strony serwera
                            IRCTerminal.writer.write("PONG " + line.substring(5) + "\r\n");
                            IRCTerminal.writer.write("PRIVMSG " + IRCTerminal.currentChannel + " :I got pinged!\r\n");
                            IRCTerminal.writer.flush();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if(IRCTerminal.appIsOpen && line.split(" ")[1].toLowerCase().equals("join")) {
                        messages.addLine("User " + line.substring(1, line.indexOf("!~")) + " joined the chat");
                        scrollMessagesToBottom();
                        IRCTerminal.writer.write("NAMES " + IRCTerminal.currentChannel + "\r\n");
                        IRCTerminal.writer.flush();
                    } else if(IRCTerminal.appIsOpen && line.split(" ")[1].toLowerCase().equals("353")) {
                        userList.setText("");
                        String[] splits  = line.split(" ");
                        for(int i = 5; i < splits.length; i++) {
                            int j = 0;
                            while(splits[i].substring(j, j+1).matches(".*[:@+].*")) {
                                j++;
                            }
                            userList.addLine(splits[i].substring(j));
                        }
                    } else if(IRCTerminal.appIsOpen && line.split(" ")[1].toLowerCase().equals("privmsg")){
                        String[] splitLine = line.split(" ");
                        String username = line.substring(1, line.indexOf("!~"));
                        StringBuilder lineToAdd = new StringBuilder(splitLine[3].substring(1));
                        for(int i = 4; i < splitLine.length; i++)
                            lineToAdd.append(" ").append(splitLine[i]);
                        messages.addLine(username + ": " + lineToAdd.toString());
                        scrollMessagesToBottom();
                    } else if(IRCTerminal.appIsOpen && line.split(" ")[1].toLowerCase().equals("quit")) {
                        String username = line.substring(1, line.indexOf("!~"));
                        messages.addLine("User " + username + " left the chat");
                        scrollMessagesToBottom();
                        IRCTerminal.writer.write("NAMES " + IRCTerminal.currentChannel + "\r\n");
                        IRCTerminal.writer.flush();
                        if(username.equals(IRCTerminal.nickname)) {
                            IRCTerminal.appIsOpen = false;
                            hasQuit = true;

                            terminal.getResultThread().join();
                            terminal.getConnectionThread().join();

                            terminal.closeConnection();
                            MessageDialogButton result = MessageDialog.showMessageDialog(getTextGUI(), "Utracono połączenie", "Utracono połaczenie z kanałem " + IRCTerminal.currentChannel + ".\n" +
                                    "Czy chcesz nawiązać kolejne połączenie?", MessageDialogButton.Yes, MessageDialogButton.Retry, MessageDialogButton.Abort);
                            switch (result) {
                                case Abort -> returnVal = 0;
                                case Yes -> returnVal = 1;
                                case Retry -> returnVal = 2;
                            }

                            close();
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public MainCommWindow(String title) {
        super(title);
        setFixedSize(new TerminalSize(180, 42));
    }

    private void scrollMessagesToBottom() {
        messages.getRenderer().setViewTopLeft(new TerminalPosition(0, messages.getLineCount()));
    }

    @Override
    public void start() throws IOException {
        hasQuit = false;
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));
        terminal = IRCTerminal.getInstance();
        Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        Panel mainClientPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel mainMessagePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        messages = new TextBox(new TerminalSize(160, 38));
        messages.setReadOnly(true);
        msgInput = new MessageInputTextBox(new TerminalSize(160, 1));
        msgInput.addSubmitListener(() -> {
            String line1 = msgInput.getText();
            if(line1.equals("")) return;
            if(line1.startsWith("/priv")) {
                List<String> commandTokens = Arrays.asList(line1.trim().replaceAll(" +", " ").split(" "));
                if(commandTokens.size() < 3 || commandTokens.get(1).startsWith("#")) {
                    messages.addLine("[BlitzIRC] Składnia komendy /priv: ");
                    messages.addLine("[BlitzIRC] /priv <nick użytkownika> <wiadomość>");
                } else {
                    StringBuilder message = new StringBuilder(commandTokens.get(2));
                    for(int i = 3; i < commandTokens.size(); i++) {
                        message.append(" ").append(commandTokens.get(i));
                    }
                    try {
                        IRCTerminal.writer.write("PRIVMSG " + commandTokens.get(1) + " :" + message.toString() + "\r\n");
                        IRCTerminal.writer.flush();
                        messages.addLine(IRCTerminal.nickname + " do " + commandTokens.get(1) + ": " + message.toString());
                        scrollMessagesToBottom();
                    } catch (IOException e) {
                        Logger.getLogger(MainCommWindow.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
            else if (line1.charAt(0) == '/') {
                try {
                    IRCTerminal.writer.write( line1.substring(1) + "\r\n");
                    IRCTerminal.writer.flush();
                } catch (IOException e) {
                    Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {
                try {
                    IRCTerminal.writer.write("PRIVMSG " + IRCTerminal.currentChannel +" :" + line1 + "\r\n");
                    IRCTerminal.writer.flush();
                    messages.addLine(IRCTerminal.nickname + ": " + line1);
                    scrollMessagesToBottom();
                } catch (IOException e) {
                    Logger.getLogger(IRCTerminal.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            msgInput.setText("");
        });

        mainMessagePanel.addComponent(messages.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        mainMessagePanel.addComponent(new EmptySpace());
        mainMessagePanel.addComponent(msgInput.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        mainClientPanel.addComponent(mainMessagePanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));

        userList = new TextBox(new TerminalSize(20, 40));
        userList.setReadOnly(true);
        mainClientPanel.addComponent(userList.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));

        contentPanel.addComponent(mainClientPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));
        contentPanel.addComponent(new EmptySpace());

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button buttonChangeChannel = new Button("Zmień kanał", () -> {
            try {
                IRCTerminal.appIsOpen = false;
                terminal.getServerThread().join();
                terminal.getResultThread().join();
                terminal.getConnectionThread().join();
                leaveChannel();
                terminal.closeConnection();
                returnVal = 1;
                close();
            } catch (Exception e) {
                Logger.getLogger(MainCommWindow.class.getName()).log(Level.SEVERE, null, e);
            }
        });
        buttonPanel.addComponent(buttonChangeChannel);
        Button buttonExit = new Button("Wyjście", this::close);
        buttonPanel.addComponent(buttonExit);
        contentPanel.addComponent(buttonPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));

        setComponent(contentPanel);

        messages.addLine("[BlitzIRC] Witaj na kanale " + IRCTerminal.currentChannel + ". Komenda \"/priv <nick użytkownika> <wiadomość>\"");
        messages.addLine("[BlitzIRC] pozwala wysłać wiadomość tylko do konkretnego użytkownika. Inne wiadomości zaczynające się od ukośnika");
        messages.addLine("[BlitzIRC] to komendy serwera IRC. Używaj ich tylko wtedy, kiedy wiesz, co robisz!");
        scrollMessagesToBottom();

        terminal.initializeServerThread(new Server());

        returnVal = 0;
    }

    public void leaveChannel() throws IOException {
        if(!hasQuit) {
            IRCTerminal.writer.write("QUIT :Left the channel\r\n");
            IRCTerminal.writer.flush();
            hasQuit = true;
        }
    }
}
