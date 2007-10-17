/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved
 */

package com.sun.sgs.germwar.client.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * A GermWar client GUI component that provides an instant-messenger-type
 * interface for sending and receiving chat messages to and from other players.
 */
public class ChatPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** All of the actions that can be generated by buttons in this panel. */
    public static final String CHAT_ACTION = "chat";
    
    /** Time/date format for the chat history. */
    private static final SimpleDateFormat tsFormat =
        new SimpleDateFormat("hh:mm a");

    /** The input field for the recipient's name. */
    private JTextField nameField;

    /** The input field for messages. */
    private JTextField messageField;

    /** The output area for channel messages. */
    private final JList chatHistory;
    private final DefaultListModel chatHistoryModel;

    // Constructor

    /**
     * Creates a new {@code ChatPanel}.
     */
    public ChatPanel() {
        super();
        
        nameField = new JTextField();
        messageField = new JTextField();
        chatHistoryModel = new DefaultListModel();

        chatHistory = new JList(chatHistoryModel);
        chatHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatHistory.setLayoutOrientation(JList.VERTICAL);
        chatHistory.setVisibleRowCount(8);

        setupLayout();
    }

    /** 
     * Sets up the layout of this panel (assumes all components have been
     * created already).
     */
    private void setupLayout() {
        // bottom panel (everything but the chatHistory widget)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        int textFieldHeight = messageField.getPreferredSize().height;

        bottomPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        bottomPanel.add(new JLabel("To:"));
        bottomPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomPanel.add(nameField);
        bottomPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        bottomPanel.add(new JLabel("Message:"));
        bottomPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        bottomPanel.add(messageField);
        bottomPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        nameField.setPreferredSize(new Dimension(125, textFieldHeight));
        nameField.setMaximumSize(new Dimension(125, textFieldHeight));

        messageField.setMaximumSize(new Dimension(9999, textFieldHeight));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JScrollPane(chatHistory));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(bottomPanel);
    }

    /**
     * Adds an {@code ActionListener} to the message field.
     */
    public void addActionListener(final ActionListener l) {
        messageField.addActionListener(new ActionListener() {
                /**
                 * Replace the normal event command (which is the text of the
                 * text field) with {@code CHAT_ACTION}.
                 */
                public void actionPerformed(ActionEvent e) {
                    l.actionPerformed(new ActionEvent(e.getSource(),
                                          e.getID(),
                                          CHAT_ACTION,
                                          e.getWhen(),
                                          e.getModifiers()));
                }});
    }

    /**
     * Adds a new chat record to the chat history.
     */
    public void addRecord(String msg, String sender) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(tsFormat.format(new Date()));
        
        if (sender != null) {
            sb.append("  ").append(sender);
        }
        
        sb.append(": ").append(msg).append("\n");

        chatHistoryModel.addElement(sb.toString());

        /**
         * Due to timing issues, chatHistoryModel.getSize() returns the wrong
         * value if called right now; have to delay calling it until the GUI has
         * updated to reflect the new (just added) list item.
         */
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    chatHistory.ensureIndexIsVisible(chatHistoryModel.getSize() - 1);
                }
            });
    }

    /** Returns the current value of the message field. */
    public String getMessage() {
        return messageField.getText();
    }

    /** Returns the current value of the name (recipient) field. */
    public String getRecipient() {
        return nameField.getText();
    }

    /**
     * Removes an {@code ActionListener} from the message field.
     */
    public void removeActionListener(ActionListener l) {
        messageField.removeActionListener(l);
    }

    /**
     * {@inheritDoc}
     * <p>
     * En/disable all buttons on this panel when its en/disabled.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        nameField.setEnabled(enabled);
        messageField.setEnabled(enabled);
        chatHistory.setEnabled(enabled);

        if (!enabled) chatHistoryModel.clear();
    }

    /**
     * Sets value of the message field.
     */
    public void setMessage(String value) {
        messageField.setText(value);
    }

    /**
     * Sets value of the name (recipient) field.
     */
    public void setRecipient(String value) {
        nameField.setText(value);
    }
}
