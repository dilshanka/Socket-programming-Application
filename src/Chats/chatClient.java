package Chats;
import java.io.*;
import java.net.*;

public class chatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // Read email from console
            System.out.print("Enter your email: ");
            String username = consoleReader.readLine();
            out.println(username);

            // Start a separate thread to handle incoming messages from the server
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                        if (serverMessage.equals("Server: Enter your message")) {
                            // Prompt the user for input only if server requests it
                            System.out.print("You: ");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Loop to send messages to the server
            while (true) {
                // Read user input
                String userInput = consoleReader.readLine();
                // Print the message in console
                System.out.println("You: " + userInput);
                // Send the user input to the server
                out.println(username + ": " + userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
