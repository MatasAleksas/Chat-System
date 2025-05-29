import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * The ChatClient class represents a client-side application that establishes a connection
 * to a server, sends a predefined message, and manages resource cleanup.
 *
 * The class demonstrates basic socket programming and is intended to pair with a corresponding
 * server to facilitate basic one-way communication.
 *
 * @author Matas Aleksas
 * @version 1.0.0
 */
public class ChatClient {
    /**
     * The main method initializes a client-side application that establishes a socket connection
     * to a server, listens for messages from the server, and allows the client to send messages
     * to the server.
     *
     * @param args command-line arguments, not utilized in this implementation
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter server host: ");
        String host = scan.nextLine();

        try {
            // Create a socket connection to the server
            // localhost is the local host machine, 5000 is the port number
            Socket s = new Socket(host, 5000);
            System.out.println("Connected to server");

            // Get input and output streams for communication with the server
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter serverOut = new PrintWriter(s.getOutputStream(), true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            // Start a thread to read messages from the server and display them to the console
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();

            // Send a message to the server
            String userInput;
            while ((userInput = userIn.readLine()) != null) {
                serverOut.println(userInput);
            }

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}
