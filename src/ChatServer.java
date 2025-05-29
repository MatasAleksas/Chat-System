import java.io.*;
import java.net.*;
import java.util.*;

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
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static int clientCount = 0;

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
                clientCount++;
                new ClientHandler(s, "Client-" + clientCount).start();
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
        private String clientName;
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        /**
         * Constructs a new ClientHandler instance and initializes it with the specified
         * client socket. This class is responsible for handling communication with the
         * connected client.
         *
         * @param socket the socket associated with the connected client
         */
        public ClientHandler(Socket socket, String clientName) {
            this.socket = socket;
            this.clientName = clientName;
        }

        /**
         * Manages communication with a connected client. This method is responsible for
         * reading messages from the client, broadcasting them to all connected clients,
         * and managing necessary cleanup in case of errors or disconnections.
         *
         * The method:
         * - Reads input streams from the client socket to fetch messages.
         * - Writes messages to the output streams of all connected clients.
         * - Handles exceptions related to input/output operations.
         * - Cleans up resources, such as removing the client's writer from the global
         *   collection and closing the client socket when the connection is terminated.
         *
         * This method runs in an infinite loop until the client disconnects or an error occurs.
         *
         * Implementation details include:
         * - Using a synchronized set to store and manage client output streams.
         * - Handling IOException to ensure graceful failure scenarios.
         * - Closing the client socket and removing the associated writer from the
         *   global collection during error handling or cleanup.
         */
        public void run() {
            try {
                // Get input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Add the client's writer to the global collection to broadcast messages to all clients
                clientWriters.add(out);
                System.out.println(clientName + " connected.");

                // Read messages from the client and broadcast them to all clients
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(clientName + " said: " + message);
                    for (PrintWriter writer : clientWriters) {
                        writer.println(clientName + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: " + e);
            } finally {
                // Cleanup resources and close the client socket
                try {
                    if (out != null) {
                        clientWriters.remove(out);
                    }

                    socket.close();
                    System.out.println(clientName + " disconnected.");
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                }
            }
        }
    }
}


