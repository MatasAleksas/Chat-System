import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ChatServer class implements a simple server that listens for incoming client connections,
 * receives messages from the client, and displays the received messages to the console.
 *
 * This class uses Java's networking package to establish a ServerSocket on a specific port
 * and facilitates communication with a single client via a socket connection.
 *
 * @author Matas Aleksas
 * @version 1.0.0
 */
public class ChatServer {
    private static Map<String, PrintWriter> userWriters = new ConcurrentHashMap<>();
    private static AtomicInteger clientCount = new AtomicInteger(0);

    /**
     * The main method serves as the entry point of the application and initiates a server that listens
     * for a client connection, receives a message from the client, and displays it.
     *
     * @param args command-line arguments, not utilized in this implementation
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Server is Starting...");

        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server is listening on port 5000...");

            while (true) {
                // Wait for a client to connect
                Socket s = serverSocket.accept();
                clientCount.incrementAndGet();
                new ClientHandler(s).start();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

    }

    /**
     * The ClientHandler class is a thread implementation responsible for managing
     * communication between the server and an individual client.
     *
     * Each connected client is handled by a separate instance of this class, which
     * facilitates reading messages from the client and broadcasting them to all
     * connected clients.
     *
     * @author Matas Aleksas
     * @version 1.0.0
     */
    private static class ClientHandler extends Thread {
        // The client socket and output streams
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        String userName;


        /**
         * Constructs a new ClientHandler instance and initializes it with the specified
         * client socket. This class is responsible for handling communication with the
         * connected client.
         *
         * @param socket the socket associated with the connected client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Manages the connection for a single client from start to finish.
         * <p>
         * First, it handles a username handshake, repeatedly asking the client for a name
         * until a unique one is provided.
         * <p>
         * Once registered, it enters a loop to read messages. It broadcasts regular
         * messages to everyone and handles special commands like /whisper and /users.
         * <p>
         * If the client disconnects, a finally block ensures their information is cleaned
         * up and the connection is closed.
         */
        public void run() {
            try {
                // Get input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // --- HANDSHAKE LOGIC on the SERVER ---
                while (true) {
                    out.println("SUBMITNAME"); // Command: "Server is ready for a name"
                    this.userName = in.readLine();
                    if (this.userName == null || this.userName.trim().isEmpty() ||
                            this.userName.toLowerCase().startsWith("/whisper") ||
                            this.userName.toLowerCase().startsWith("/users")) {
                        // Reject invalid names right away
                        out.println("INVALIDNAME");
                        continue;
                    }

                    // Atomically check if name exists and add it if it doesn't.
                    // putIfAbsent returns null if the key is new.
                    if (userWriters.putIfAbsent(this.userName, this.out) == null) {
                        out.println("NAMEACCEPTED"); // Command: "Name is good"
                        break; // Exit the handshake loop
                    } else {
                        out.println("NAMETAKEN"); // Command: "Name is rejected"
                    }
                }

                // Add the client's writer to the global collection to broadcast messages to all clients
                System.out.println(userName + " has joined.");
                broadcast("SERVER: " + userName + " has joined the chat.");

                // Read messages from the client and broadcast them to all clients
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/whisper")) {
                        handleWhisper(message);
                    } else if (message.startsWith("/users")) {
                        listUsers();
                    } else {
                        broadcast(this.userName + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("SERVER: Error with client " + this.userName + ": " + e);
            } finally {
                // Cleanup resources and close the client socket
                cleanup();
            }
        }

        /**
         * Parses a {@code /whisper} command to send a message to a single target user.
         * The expected format is "/whisper <username> <message>".
         * @param message The full command string received from the client.
         */
        private void handleWhisper(String message) {
            String[] parts = message.split(" ", 3);

            // Invalid command setup
            if (parts.length < 3) {
                out.println("SERVER: Invalid Command. Use: '/whisper <user> <message>'");
                return;
            }

            String targetUser = parts[1];
            String privateMessage = parts[2];

            // Checks if you sent a whisper to yourself
            if (targetUser.equalsIgnoreCase(this.userName)) {
                out.println("SERVER: You cant send a message to yourself.");
                return;
            }

            PrintWriter targetWriter = userWriters.get(targetUser);

            if (targetWriter != null) {
                targetWriter.println("[Whisper from: " + this.userName + " ]: " + privateMessage);

                // Confirmation back to user
                out.println("[Whisper to " + targetUser + "]: " + privateMessage);
            } else {
                out.println("SERVER: Specified user '" + targetUser + "' was not found.");
            }
        }

        /**
         * Retrieves the list of all currently connected usernames and sends it back
         * to the client who issued the {@code /users} command.
         */
        private void listUsers() {
            // The keySet() of our ConcurrentHashMap is a thread-safe view of all keys (usernames).
            String userList = String.join(", ", userWriters.keySet());

            out.println("SERVER: Active users (" + userWriters.size() + "): " + userList);
        }

        /**
         * Sends a given message to every client currently connected to the server.
         * This is used for public chat messages and server-wide announcements.
         * @param message The message to be broadcast to all users.
         */
        private void broadcast(String message) {
            for (PrintWriter writer : userWriters.values()) {
                writer.println(message);
            }
        }

        /**
         * Handles the complete deregistration of a client when they disconnect.
         * It removes the user from the active user list, notifies other clients of
         * their departure, and closes the socket connection to free up resources.
         */
        private void cleanup() {
            if (this.userName != null) {
                System.out.println(userName + " is disconnecting.");

                if (userWriters.remove(this.userName) != null) {
                    clientCount.decrementAndGet();
                    broadcast("SERVER: " + userName + " has left the chat.");
                }
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
        }
    }
}


