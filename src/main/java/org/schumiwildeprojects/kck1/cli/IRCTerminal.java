package org.schumiwildeprojects.kck1.cli;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import org.schumiwildeprojects.kck1.backend.ConnectionState;
import org.schumiwildeprojects.kck1.backend.Main;
import org.schumiwildeprojects.kck1.cli.states.LoginState;
import org.schumiwildeprojects.kck1.cli.states.State;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

// singleton (tylko jedno okno główne na aplikację)
public class IRCTerminal {
    private static IRCTerminal instance;
    public static String serverName;
    public static String nickname;
    public static String currentLogin;
    public static String currentFullName;
    public static String currentPassword;
    private WindowBasedTextGUI textGUI;
    private final Terminal terminal;
    private final Screen screen;
    public static String currentChannel;
    public static BufferedReader reader;
    public static BufferedWriter writer;

    private Thread serverThread;
    private ConnectionThread connectionRunnable;
    private Thread connectionThread, resultThread;
    private Socket socket;
    private State state;
    public static volatile boolean appIsOpen = false;

    class ConnectionThread implements Runnable {

        private final String serverName;
        private final String nick;
        private final String login;
        private final String fullName;
        private final String channel;
        private final String pass;
        private ConnectionState state;

        public ConnectionThread(String serverName, String nick, String login, String fullName, String channel, String pass) {
            this.serverName = serverName;
            this.nick = nick;
            this.login = login;
            this.fullName = fullName;
            this.channel = channel;
            this.pass = pass;
        }
        @Override
        public void run() {
            if (!serverName.equals("") && !nick.equals("") && !login.equals("") && !fullName.equals("") && !channel.equals("")) {
                try {
                    state = connect(serverName, nick, login, fullName, channel, pass);
                } catch (IOException e) {
                    Logger.getLogger(LoginWindow.class.getName()).log(Level.SEVERE, null, e);
                    System.exit(-1);
                }
            } else {
                state = ConnectionState.FIELDS_NOT_FILLED;
            }
        }

        public ConnectionState getResult() {
            return state;
        }
    }

    public ConnectionState connect(String serverName, String nick, String login, String name, String channel, String pass) throws IOException {

        // Łączenie z serwerem
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(serverName, Main.PORT), 10000);
        } catch (SocketTimeoutException e) {
            return ConnectionState.SERVER_TIMEOUT;
        } catch (SocketException e) {
            return ConnectionState.SERVER_NAME_EMPTY;
        }
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        // Logowanie się do serwera
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + " 8 * : " + name + "\r\n");
        writer.write("msg nickserv identify " + nick + " " + pass + "\r\n");
        writer.flush();

        // Read lines from the server until it tells us we have connected.
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("004")) { // Kod oznaczający połączenie
                // Jesteśmy zalogowani
                break;
            }
            else if( line.contains("432")) {
                return ConnectionState.INVALID_NICKNAME;
            }
            else if (line.contains("433")) { // Nazwa użytkownika jest już zajęta
                System.out.println("Nickname is already in use.");
                return ConnectionState.USERNAME_EXISTS;
            }
        }

        // Wyślij wiadomość do serwera o dołączenie
        writer.write("JOIN " + channel + "\r\n");
        writer.flush( );

        currentChannel = channel;
        nickname = nick;
        currentLogin = login;
        currentFullName = name;
        currentPassword = pass;
        appIsOpen = true;
        return ConnectionState.SUCCESSFUL;
    }

    public static IRCTerminal getInstance() throws IOException {
        if(instance == null)
            instance = new IRCTerminal();
        return instance;
    }

    private IRCTerminal() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        factory.setInitialTerminalSize(new TerminalSize(200, 50));
        factory.setTerminalEmulatorTitle("BlitzIRC");
        terminal = factory.createTerminalEmulator();
        terminal.enterPrivateMode();
        terminal.setCursorVisible(false);
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
    }

    private class TimeoutTask extends TimerTask {

        @Override
        public void run() {
            closeConnection();
            System.exit(0);
        }
    }

    public void start() throws IOException {
        changeState(new LoginState());
        while(state.getWindow() != null) {
            show();
        }
        close();
        appIsOpen = false;
        Timer timer = new Timer();
        timer.schedule(new TimeoutTask(), 5000);
        try {
            if(connectionThread != null)
                connectionThread.join();
            if(serverThread != null)
                serverThread.join();
            if(resultThread != null)
                resultThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        timer.cancel();

        closeConnection();
    }

    public void changeState(State state) {
        this.state = state;
    }

    void show() throws IOException {
        textGUI = new MultiWindowTextGUI(screen);
        BasicLateInitWindow window = state.getWindow();
        textGUI.addWindow(window);
        window.start();
        textGUI.waitForWindowToClose(window);
        // zwraca jedyne okno w TextGUI
        int retVal = window.returnValue();
        switch (retVal) {
            case 0 -> state.onClose();
            case 1 -> state.onSubmit();
            case 2 -> state.onRetry();
            default -> throw new IllegalStateException("Wrong window return value");
        }
    }

    public Thread getServerThread() { return serverThread; }

    public Thread getResultThread() {
        return resultThread;
    }

    public ConnectionThread getConnectionRunnable() {
        return connectionRunnable;
    }

    public Thread getConnectionThread() {
        return connectionThread;
    }

    public void initializeConnectionThread(String serverName, String nick, String login, String fullName, String channel, String pass) {
        currentChannel = channel;
        connectionRunnable = new ConnectionThread(serverName, nick, login, fullName, channel, pass);
        connectionThread = new Thread(connectionRunnable);
        connectionThread.start();
    }

    public void initializeResultThread(Runnable func) {
        resultThread = new Thread(func);
        resultThread.start();
    }

    public void initializeServerThread(Runnable func) {
        serverThread = new Thread(func);
        serverThread.start();
    }

    public void close() throws IOException {
        terminal.exitPrivateMode();
        terminal.close();
    }

    public void closeConnection() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not close socket");
                System.exit(-1);
            }
        }
    }
}
