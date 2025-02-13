package chat;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 5000;

    public static void main(String[] args) {
        int attempt = 0;

        // Try to establish a connection with retry logic
        while (attempt < MAX_RETRIES) {
            try (
                    Socket socket = new Socket(args[0], 5000);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Scanner scanner = new Scanner(System.in)
            ) {

                System.out.println("Connected to the chat server!\n");

                // Start a thread to handle incoming messages
                Thread receiveThread = new Thread(() -> {
                    try {
                        String serverResponse;
                        while ((serverResponse = in.readLine()) != null) {
                            System.out.println(serverResponse);
                        }
                    } catch (IOException e) {
                        System.err.println("\nError receiving message from server: " + e.getMessage() + "\n");
                    }
                });

                receiveThread.setDaemon(true);
                receiveThread.start();

                // Read messages from the console and send to the server
                while (true) {
                    String userInput = scanner.nextLine();

                    if (userInput.equalsIgnoreCase("/exit")) {
                        System.out.println("\nDisconnecting from server...\n");
                        break;
                    }

                    out.println(userInput);
                }

                break; // Exit retry loop if successful

            } catch (IOException e) {
                attempt++;
                System.out.println("\nConnection attempt " + attempt + " failed. Retrying...\n");

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {}
            }
        }

        if (attempt == MAX_RETRIES) {
            System.err.println("\nFailed to connect after " + MAX_RETRIES + " attempts.\n");
        }
    }
}
