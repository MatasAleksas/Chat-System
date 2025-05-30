import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

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
    public static final String server_address = "localhost";
    public static final int server_port = 5000;
    public String name;

    private JFrame frame;
    private JTextArea chatArea;
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
        name = JOptionPane.showInputDialog(null, "Enter your name:", "Username", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            name = "Anonymous";
        }

        frame = new JFrame("Chat Client - " + name);
        chatArea = new JTextArea(20, 40);
        textField = new JTextField();

        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

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

            out.println(name);

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Error: " + e + "\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Error: " + e + "\n");
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
