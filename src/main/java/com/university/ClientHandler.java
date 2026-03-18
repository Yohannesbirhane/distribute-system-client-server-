package com.university;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        System.out.println("[SERVER] Handling new client on thread " + Thread.currentThread().getName());

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String clientMessage;
            
            // Read incoming requests from the client
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("[SERVER] Received from client: " + clientMessage);
                
                // Parse and handle the request safely handling exceptions
                String response = handleCommand(clientMessage.trim());
                
                // Send response back
                out.println(response);
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error with client connection: " + e.getMessage());
        } finally {
            try {
                // Ensure the socket is always closed upon exiting
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("[SERVER] Error closing socket: " + e.getMessage());
            }
            System.out.println("[SERVER] Client handler finished.");
        }
    }

    /**
     * Parses the command array or string and delegates to operations.
     * Expected commands: GET_ALL, ADD <name> <dept>, DELETE <id>, SEARCH <name>
     */
    private String handleCommand(String commandLine) {
        if (commandLine.isEmpty()) {
            return createErrorResponse("Empty command sent.");
        }

        String[] parts = commandLine.split("\\s+", 3);
        String action = parts[0].toUpperCase();

        try (Connection conn = DBConnection.getConnection()) {
            switch (action) {
                case "GET_ALL":
                    return getAllStudents(conn);
                case "ADD":
                    // Format: ADD Alice Smith;Female;20;Computer Science
                    String addArgs = commandLine.substring("ADD".length()).trim();
                    String[] addParts = addArgs.split(";", 4);
                    if (addParts.length < 4) return createErrorResponse("Usage: ADD <name>;<sex>;<age>;<department>");
                    
                    try {
                        int age = Integer.parseInt(addParts[2].trim());
                        return addStudent(conn, addParts[0].trim(), addParts[1].trim(), age, addParts[3].trim());
                    } catch (NumberFormatException e) {
                        return createErrorResponse("Age must be a valid number.");
                    }
                case "DELETE":
                    if (parts.length < 2) return createErrorResponse("Usage: DELETE <id>");
                    try {
                        int id = Integer.parseInt(parts[1]);
                        return deleteStudent(conn, id);
                    } catch (NumberFormatException e) {
                        return createErrorResponse("Invalid ID format.");
                    }
                case "SEARCH":
                    // Format: SEARCH <field> <value> 
                    // e.g., SEARCH name John | SEARCH age 20 | SEARCH department Physics
                    if (parts.length < 3) return createErrorResponse("Usage: SEARCH <field> <value> (fields: name, sex, age, department)");
                    String field = parts[1].toLowerCase();
                    String searchValue = commandLine.substring(commandLine.indexOf(parts[2])).trim();
                    return searchStudent(conn, field, searchValue);
                default:
                    return createErrorResponse("Unknown command: " + action);
            }
        } catch (SQLException e) {
            System.err.println("[SERVER] Database error: " + e.getMessage());
            return createErrorResponse("Database error occurred: " + e.getMessage());
        }
    }

    private String getAllStudents(Connection conn) throws SQLException {
        String query = "SELECT id, name, sex, age, department FROM students ORDER BY id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            JSONArray array = new JSONArray();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id"));
                obj.put("name", rs.getString("name"));
                obj.put("sex", rs.getString("sex"));
                obj.put("age", rs.getInt("age"));
                obj.put("department", rs.getString("department"));
                array.put(obj);
            }
            return createSuccessResponse("Students retrieved.", array);
        }
    }

    private String addStudent(Connection conn, String name, String sex, int age, String department) throws SQLException {
        String query = "INSERT INTO students (name, sex, age, department) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, sex);
            stmt.setInt(3, age);
            stmt.setString(4, department);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return createSuccessResponse("Student '" + name + "' added successfully.", null);
            } else {
                return createErrorResponse("Failed to add student.");
            }
        }
    }

    private String deleteStudent(Connection conn, int id) throws SQLException {
        String query = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return createSuccessResponse("Student with ID " + id + " has been deleted.", null);
            } else {
                return createErrorResponse("No student found with ID " + id + ".");
            }
        }
    }

    private String searchStudent(Connection conn, String field, String value) throws SQLException {
        String query;
        boolean isAgeSearch = field.equals("age");
        
        if (isAgeSearch) {
            query = "SELECT id, name, sex, age, department FROM students WHERE age = ?";
        } else if (field.equals("name") || field.equals("sex") || field.equals("department")) {
            query = "SELECT id, name, sex, age, department FROM students WHERE " + field + " ILIKE ?";
        } else {
            return createErrorResponse("Invalid search field. Use: name, sex, age, or department.");
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            if (isAgeSearch) {
                try {
                    stmt.setInt(1, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    return createErrorResponse("Age must be a valid number.");
                }
            } else {
                stmt.setString(1, "%" + value + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                JSONArray array = new JSONArray();
                while (rs.next()) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", rs.getInt("id"));
                    obj.put("name", rs.getString("name"));
                    obj.put("sex", rs.getString("sex"));
                    obj.put("age", rs.getInt("age"));
                    obj.put("department", rs.getString("department"));
                    array.put(obj);
                }
                return createSuccessResponse("Found " + array.length() + " match(es).", array);
            }
        }
    }

    /**
     * Helper to wrap a successful response in a structured JSON
     */
    private String createSuccessResponse(String message, JSONArray data) {
        JSONObject response = new JSONObject();
        response.put("status", "SUCCESS");
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        } else {
            response.put("data", new JSONArray());
        }
        return response.toString();
    }

    /**
     * Helper to wrap an error response in a structured JSON
     */
    private String createErrorResponse(String errorMessage) {
        JSONObject response = new JSONObject();
        response.put("status", "ERROR");
        response.put("message", errorMessage);
        response.put("data", new JSONArray());
        return response.toString();
    }
}
