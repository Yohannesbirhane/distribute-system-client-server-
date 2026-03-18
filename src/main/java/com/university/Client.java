package com.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main Client Class
 * Connects to the server, reads keyboard input, sends to server,
 * and prints server responses clearly in the console.
 */
public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("--- Student Database Client ---");
        System.out.println("Connecting to server at " + SERVER_HOST + ":" + SERVER_PORT);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server successfully!");
            printHelpMenu();

            while (true) {
                System.out.print("\nEnter command > ");
                String userInput = scanner.nextLine().trim();

                if (userInput.equalsIgnoreCase("EXIT")) {
                    System.out.println("Exiting client...");
                    break;
                }
                
                if (userInput.equalsIgnoreCase("HELP")) {
                    printHelpMenu();
                    continue;
                }

                if (userInput.isEmpty()) {
                    continue;
                }

                // Send request to server
                out.println(userInput);

                // Wait for the JSON response
                String jsonResponse = in.readLine();
                
                // Server closed connection or sent null
                if (jsonResponse == null) {
                    System.out.println("Server disconnected.");
                    break;
                }

                formatAndPrintResponse(jsonResponse);
            }

        } catch (IOException e) {
            System.err.println("Client Error: Could not connect to server or communication lost.");
            System.err.println(e.getMessage());
        }
    }

    private static void printHelpMenu() {
        System.out.println("\n--- Available Commands ---");
        System.out.println(" GET_ALL                                - Retrieve all students");
        System.out.println(" ADD <Name>;<Sex>;<Age>;<Department>    - Insert new student (delimiter is ';')");
        System.out.println("                                          Example: ADD Alice Smith;Female;20;Computer Science");
        System.out.println(" DELETE <ID>                            - Delete student by id");
        System.out.println(" SEARCH <Field> <Value>                 - Search student by field (name, sex, age, department)");
        System.out.println("                                          Example: SEARCH age 22  OR  SEARCH department Physics");
        System.out.println(" EXIT                                   - Disconnect and close client");
        System.out.println(" HELP                                   - Show this menu again");
        System.out.println("--------------------------");
    }

    /**
     * Parses the JSON response from the server and prints it cleanly.
     * @param jsonString The raw JSON string from server.
     */
    private static void formatAndPrintResponse(String jsonString) {
        try {
            JSONObject responseObj = new JSONObject(jsonString);
            String status = responseObj.getString("status");
            String message = responseObj.getString("message");
            JSONArray data = responseObj.getJSONArray("data");

            if ("SUCCESS".equals(status)) {
                System.out.println("[SUCCESS] " + message);
                
                if (data != null && data.length() > 0) {
                    System.out.printf("%-5s | %-20s | %-8s | %-4s | %-25s%n", "ID", "Name", "Sex", "Age", "Department");
                    System.out.println("-------------------------------------------------------------------------");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject student = data.getJSONObject(i);
                        System.out.printf("%-5d | %-20s | %-8s | %-4d | %-25s%n", 
                                student.getInt("id"), 
                                student.getString("name"), 
                                student.getString("sex"), 
                                student.getInt("age"), 
                                student.getString("department"));
                    }
                    System.out.println("-------------------------------------------------------------------------");
                }
            } else {
                System.out.println("[ERROR] " + message);
            }

        } catch (Exception e) {
            // Fallback for when the server does not send strictly valid JSON
            // (e.g. connection error dumps)
            System.out.println("[SERVER RAW] " + jsonString);
        }
    }
}
