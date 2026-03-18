package com.university;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main Server Class
 * Starts the server, listens for incoming client connections,
 * and creates a new thread for each client using ClientHandler.
 */
public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting server...");
        
        // Listen on a port for incoming connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Listening on port " + PORT);
            
            // Enter an infinite loop to accept client connections continually
            while (true) {
                // Blocks until a client connects
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // Spawn a new thread for each client so multiple clients can be served simultaneously
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
