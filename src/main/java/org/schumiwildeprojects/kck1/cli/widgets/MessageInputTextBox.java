package org.schumiwildeprojects.kck1.cli.widgets;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.ArrayList;
import java.util.List;

public class MessageInputTextBox extends TextBox {

    private final List<SubmitListener> listeners;

    public interface SubmitListener {
        void onAction();
    }

    public void addSubmitListener(SubmitListener listener) {
        listeners.add(listener);
    }

    public MessageInputTextBox(TerminalSize size) {
        super(size);
        listeners = new ArrayList<>();
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        if(keyStroke.getKeyType() == KeyType.Enter) {
            for(SubmitListener l: listeners) {
                l.onAction();
            }
            return Result.HANDLED;
        }
        return super.handleKeyStroke(keyStroke);
    }
}
