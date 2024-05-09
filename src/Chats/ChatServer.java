package Chats;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 9999;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            // Start a separate thread to handle sending messages from the console to clients
            Thread consoleSendThread = new Thread(() -> {
                try {
                    while (true) {
                        // Read message from console
                        System.out.print("Enter recipient's username (or type 'all' for broadcast): ");
                        String recipient = consoleReader.readLine();
                        System.out.print("Enter message: ");
                        String message = consoleReader.readLine();
                        
                        // Send message to the specified client or all clients
                        if (recipient.equalsIgnoreCase("all")) {
                            sendBroadcastMessage("Server: " + message);
                        } else {
                            sendMessage(recipient, "Server: " + message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            consoleSendThread.start();

            // Accept client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Read username from the client
                this.username = in.readLine();
                clients.put(username, out);

                // Start a separate thread to handle client's input
                Thread inputThread = new Thread(() -> {
                    try {
                        String clientMessage;
                        while ((clientMessage = in.readLine()) != null) {
                            System.out.println("Received from  " + ": " + clientMessage);
                            // No need to process client's message
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                inputThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void sendMessage(String recipient, String message) {
        PrintWriter out = clients.get(recipient);
        if (out != null) {
            out.println(message);
        } else {
            System.out.println("Recipient not found: " + recipient);
        }
    }
    
    static void sendBroadcastMessage(String message) {
        for (PrintWriter out : clients.values()) {
            out.println(message);
        }
    }
}
