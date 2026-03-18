package com.university;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials
    private static final String URL = "jdbc:postgresql://localhost:5432/university_da";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "kibru2130";

    /**
     * Establish a connection to the PostgreSQL database.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver is not found.");
            e.printStackTrace();
            throw new SQLException("Database driver not found", e);
        }
    }
}
