package com.project.artconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to manage JDBC connections.
 * TODO: Students must implementation the getConnection logic.
 */
public class ConnectionManager {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/artconnect";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Clementine1Mysql!";

    /**
     * Provides a connection to the MySQL database.
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        // TODO: Students should implement this using DatabaseConfig properties
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        //throw new UnsupportedOperationException("Database connection logic not yet implemented.");
    }
}
