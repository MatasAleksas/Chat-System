package com.chatapp;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The ChatUI class represents a client-side chat application implemented with a graphical
 * user interface (GUI). This class is responsible for setting up the user interface, managing
 * the connection to the chat server, and handling message transmission and reception.
 *
 * The main features of the class include:
 * - A graphical interface for displaying and sending chat messages.
 * - A connection to the server to facilitate real-time communication.
 * - Handling of user input and server messages as part of the chat functionality.
 *
 * The class contains methods for initializing the interface, connecting to the server, and
 * processing user inputs.
 *
 * @author Matas Aleksas
 * @version 1.0.0
 */
public class ChatUI {
    public static final String server_address = "3.142.249.27"; //3.142.249.27
    public Map<String, Style> userStyles = new HashMap<>();
    public static final int server_port = 5000;
    public String name;
    private Style specialMessageStyle;

    private JFrame frame;
    private JTextPane chatArea;
    private StyledDocument doc;
    private Map<String, Color> userColors = new HashMap<>();
    private Random random = new Random();
    private JTextField textField;
    private PrintWriter out;

    /**
     * The main method serves as the entry point of the ChatUI application. It initializes and
     * displays the user interface for the chat client by creating an instance of ChatUI and
     * invoking its UI setup functionality.
     *
     * @param args command-line arguments, not utilized in this implementation
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatUI().createUI());
    }

    private Color getUserColor(String user) {
        return userColors.computeIfAbsent(user, k -> new Color(random.nextInt(256),
                random.nextInt(256), random.nextInt(256)));
    }

    private void appendMessageWithColor(String message) {
        try {
            if (message.startsWith("[Whisper") || message.startsWith("SERVER:")) {
                doc.insertString(doc.getLength(), message + "\n", specialMessageStyle);
                return; // We've handled it, so we can exit the method.
            }

            int timestampEnd = message.indexOf(']');
            int userMessageSep =  message.indexOf(':', timestampEnd);

            int sep = message.indexOf(':');
            if (userMessageSep != -1 && timestampEnd != -1) {
                String timestampPart = message.substring(0, timestampEnd + 2);
                String usernamePart = message.substring(timestampEnd + 2, userMessageSep);
                String messagePart = message.substring(userMessageSep);

                doc.insertString(doc.getLength(), timestampPart, null);

                Style userStyle = chatArea.addStyle(usernamePart, null);
                StyleConstants.setForeground(userStyle, getUserColor(usernamePart));
                doc.insertString(doc.getLength(), usernamePart, userStyle);

                doc.insertString(doc.getLength(), messagePart + "\n", null);
            } else {
                doc.insertString(doc.getLength(), message + "\n", null);
            }
        } catch (BadLocationException e) {
            System.out.println("Error: " + e);
        }
    }

    private void appendMessage(String message) {
        try {
            doc.insertString(doc.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Initializes and constructs the user interface for the chat client application.
     * This method sets up the main components of the user interface, including the
     * chat display area, text input field, and frame layout. The method also attaches
     * listeners to handle user inputs and connects the client to the server.
     *
     * The method performs the following tasks:
     * - Creates and configures the main application window (JFrame).
     * - Sets up a non-editable JTextArea for displaying chat messages, which is
     *   scrollable using a JScrollPane.
     * - Adds a JTextField for user input and registers an ActionListener to handle
     *   sending messages when the user presses the Enter key.
     * - Arranges components in the frame using a BorderLayout.
     * - Packs and displays the frame.
     * - Initiates a connection to the server by invoking the connectToServer method.
     *
     * This method is invoked to initialize and display the graphical components of
     * the client-side chat application.
     */
    private void createUI() {
        this.name = JOptionPane.showInputDialog(null, "Enter your desired name:", "Username",
                JOptionPane.PLAIN_MESSAGE);
        if (this.name == null || this.name.trim().isEmpty()) {
            this.name = "Anonymous" + random.nextInt(1000); // Make anonymous more unique
        }

        frame = new JFrame("Chat Client - " + this.name);
        frame.setLocationRelativeTo(null);
        chatArea = new JTextPane();
        textField = new JTextField();

        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();

        specialMessageStyle = chatArea.addStyle("special", null);
        StyleConstants.setForeground(specialMessageStyle, Color.GRAY);
        StyleConstants.setItalic(specialMessageStyle, true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        textField.addActionListener(e -> sendMessage());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        connectToServer();
    }

    /**
     * Establishes a connection to the chat server using the configured server address and port,
     * and initializes the input and output streams for communication.
     *
     * The method performs the following operations:
     * - Creates a socket connection to the server using the specified address and port.
     * - Initializes a PrintWriter for sending messages to the server.
     * - Initializes a BufferedReader for reading messages from the server.
     * - Starts a new thread to continuously listen for incoming messages from the server and
     *   appends these messages to the chat display area.
     *
     * If an error occurs during the connection or stream initialization, an error message
     * is printed to the console.
     *
     * This method is intended to be invoked as part of the setup process for the chat client
     * to enable real-time communication with the server.
     */
    private void connectToServer() {
        try {
            Socket socket = new Socket(server_address, server_port);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // --- HANDSHAKE LOGIC on the CLIENT ---
            while (true) {
                String serverMessage = in.readLine();
                if (serverMessage.equals("SUBMITNAME")) {
                    this.name = this.name.strip(); // Get rid of leading and trailing whitespace
                    out.println(this.name); // Send our desired name
                } else if (serverMessage.equals("NAMEACCEPTED")) {
                    this.frame.setTitle("Chat Client - " + this.name); // Name is good, set title
                    textField.setEditable(true); // Allow user to type now
                    break; // Exit handshake loop
                } else if (serverMessage.equals("NAMETAKEN")) {
                    this.name = JOptionPane.showInputDialog(frame, "Name '" + this.name +
                            "' is taken. Please choose another:", "Username Taken", JOptionPane.ERROR_MESSAGE);
                    if (this.name == null || this.name.trim().isEmpty()) {
                        this.name = "Anonymous" + random.nextInt(1000);
                    }
                } else if (serverMessage.equals("INVALIDNAME")) {
                    this.name = JOptionPane.showInputDialog(frame, "Name '" + this.name +
                            "' is invalid. Please choose another:", "Username Invalid", JOptionPane.ERROR_MESSAGE);
                    if (this.name == null || this.name.trim().isEmpty()) {
                        this.name = "Anonymous" + random.nextInt(1000);
                    }
                }
            }

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        String finalMessage = message;
                        SwingUtilities.invokeLater(() -> appendMessageWithColor(finalMessage));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> appendMessage("Error: " + e));
                }
            }).start();

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> appendMessage("Error: " + e));
        }
    }

    /**
     * Sends a message typed by the user in the text input field to the server.
     *
     * This method performs the following actions:
     * - Retrieves the text from the input field and trims any leading or trailing whitespace.
     * - Checks if the input is not empty and if the output stream to the server is initialized.
     * - Sends the message to the server using the output stream.
     * - Clears the text input field after the message has been sent.
     *
     * The method assumes that the `textField` variable is a reference to a valid JTextField
     * component where the user enters the message, and `out` is an initialized PrintWriter
     * used for sending messages to the server.
     *
     * This method does not return any value and is expected to be invoked when the user
     * submits a message, such as by pressing the Enter key.
     */
    private void sendMessage() {
        String message = textField.getText().trim();

        if (!message.isEmpty() && out != null) {
            out.println(message);
            textField.setText("");
        }
    }
}
