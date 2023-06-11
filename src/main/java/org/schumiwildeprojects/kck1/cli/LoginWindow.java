package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;

// Ekran logowania
public class LoginWindow extends BasicLateInitWindow {
    private TextBox serverNameBox;
    private TextBox nicknameBox;
    private TextBox loginBox;
    private TextBox fullNameBox;
    private TextBox channelBox;
    private TextBox passwordBox;

    public LoginWindow(String title) {
        super(title);
        setFixedSize(new TerminalSize(50, 8));
    }

    private void addLoginOption(Panel optionPanel, Label optionLabel, String textBoxName, Panel mainPanel) {
        int horizontalSpan = 1;
        optionPanel.addComponent(optionLabel);
        switch (textBoxName) {
            case "serverNameBox" -> {
                serverNameBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                optionPanel.addComponent(serverNameBox);
            }
            case "nicknameBox" -> {
                nicknameBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                optionPanel.addComponent(nicknameBox);
            }
            case "loginBox" ->  {
                loginBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                optionPanel.addComponent(loginBox);
            }
            case "fullNameBox" -> {
                fullNameBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                optionPanel.addComponent(fullNameBox);
            }
            case "channelBox" -> {
                channelBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                optionPanel.addComponent(channelBox);
            }
            case "passwordBox" -> {
                passwordBox = new TextBox(new TerminalSize((int) ((float) getSize().getColumns() / 2.5), 1));
                passwordBox.setMask('*');
                optionPanel.addComponent(passwordBox.setLayoutData(GridLayout.createHorizontallyFilledLayoutData()));
                horizontalSpan = 2;
                optionLabel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(horizontalSpan));
            }
        }
        mainPanel.addComponent(optionPanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(horizontalSpan)));
    }

    @Override
    public void start() {
        TerminalSize termSize = getTextGUI().getScreen().getTerminalSize();
        setPosition(new TerminalPosition((termSize.getColumns() - getSize().getColumns()) / 2, (termSize.getRows() - getSize().getRows()) / 2));
        Panel contentPanel = new Panel(new GridLayout(2));

        Panel serverNamePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label serverNameLabel = new Label("Nazwa serwera:");
        addLoginOption(serverNamePanel, serverNameLabel, "serverNameBox", contentPanel);

        Panel nicknamePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label nicknameLabel = new Label("Nick:");
        addLoginOption(nicknamePanel, nicknameLabel, "nicknameBox", contentPanel);

        Panel loginPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label loginLabel = new Label("Login:");
        addLoginOption(loginPanel, loginLabel, "loginBox", contentPanel);

        Panel fullNamePanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label fullNameLabel = new Label("Pełne imię:");
        addLoginOption(fullNamePanel, fullNameLabel, "fullNameBox", contentPanel);

        Panel channelPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label channelLabel = new Label("Kanał:");
        addLoginOption(channelPanel, channelLabel, "channelBox", contentPanel);

        Panel passwordPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Label passwordLabel = new Label("Hasło:");
        addLoginOption(passwordPanel, passwordLabel, "passwordBox", contentPanel);


        contentPanel.addComponent(new EmptySpace().setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button exitButton = new Button("Wyjście", this::close);
        buttonPanel.addComponent(exitButton);
        Button submitButton = new Button("Połącz", () -> {
            returnVal = 1;
            close();
        });
        buttonPanel.addComponent(submitButton);
        contentPanel.addComponent(buttonPanel
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER,
                        true, false,
                        2, 1)));

        setComponent(contentPanel);

        returnVal = 0;
    }

    public String getServerName() {
        return serverNameBox.getText();
    }

    public String getNickname() {
        return nicknameBox.getText();
    }

    public String getLogin() {
        return loginBox.getText();
    }

    public String getFullName() {
        return fullNameBox.getText();
    }

    public String getChannel() {
        return channelBox.getText();
    }

    public String getPassword() { return passwordBox.getText(); }
}
