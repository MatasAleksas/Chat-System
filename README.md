# Java Chat App

A real-time, multithreaded chat application built with Java, featuring a robust client-server architecture and a graphical user interface created with Swing.

## Description

This project is a fully functional chat system where multiple clients can connect to a central server to communicate in real-time. 
The server is built to handle numerous concurrent connections efficiently and safely, using modern Java concurrency utilities. 
Clients interact with the application through a clean and intuitive GUI.

The system features public messaging, private "whisper" messages, and a server-enforced unique username policy to ensure a smooth and secure user experience.

## Key Features

- Multithreaded Server: Capable of handling multiple simultaneous client connections, with each client managed on its own dedicated thread.
- Thread-Safe Concurrency: Utilizes `java.util.concurrent` classes like `ConcurrentHashMap` and `AtomicInteger` to manage shared server state, preventing race conditions and ensuring data integrity.
- Client-Server Architecture: A classic TCP-based architecture where the server acts as the central hub and single source of truth.
- Graphical User Interface (GUI): A user-friendly and responsive client-side interface built with Java Swing.
- Username Handshake Protocol: A robust server-side validation process ensures every user has a unique and valid username before joining the chat.
- Private Messaging: Users can send private messages to specific individuals using the `/whisper <username> <message>` command.
- User List: Clients can request an up-to-date list of all connected users with the `/users` command.

## Technologies Used

- Language: Java
- Core Libraries: Java Sockets (for TCP networking), Java Swing (for the GUI)

## Getting Started

Follow these instructions to get a local copy of the project up and running on your machine.

### Prerequisites

You will need the Java Development Kit (JDK) version 8 or higher installed on your system.

### Installation & Running
1. **Clone the repository:**
   ```sh
    git clone https://github.com/MatasAleksas/Chat-System.git
    cd YOUR_REPOSITORY
    ```
2.  **Compile the source code:**
    Open a terminal in the project directory and run the Java compiler.
    ```sh
    javac ChatServer.java ChatUI.java
    ```

3.  **Run the Server:**
    In the same terminal, start the server. It will begin listening for connections on port 5000.
    ```sh
    java ChatServer
    ```
    You should see the message: `Server is listening on port 5000...`

4.  **Run the Client(s):**
    Open a **new, separate terminal window** for each client you want to run. In each new terminal, run the following command:
    ```sh
    java ChatUI
    ```
    A GUI window will pop up, prompting you for a username. Enter a unique name and start chatting! You can open multiple client windows to simulate a real chat room.

## Future Improvements

- **Chat Rooms:** Implement functionality for users to create and join different chat rooms or channels.
- **Persistent History:** Save chat messages to a file or a simple database so that history is not lost when the server restarts.
- **Enhanced UI:** Add features like a visual user list on the side of the chat window.
- **File Transfers:** Allow users to send files to one another.
- **Encryption:** Implement SSL/TLS to encrypt communication between the client and server.
