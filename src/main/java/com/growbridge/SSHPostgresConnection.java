package com.growbridge;

import com.jcraft.jsch.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SSHPostgresConnection {
    private static String SSH_HOST = "your.ssh.server.com";  // SSH Server
    private static int SSH_PORT = 22;                        // SSH Port
    private static String SSH_USER = "your_ssh_username";    // SSH Username
    private static String SSH_PASSWORD = "/path/to/private_key.pem";  // Private key for SSH

    private static String DB_HOST = "127.0.0.1";  // Localhost (after SSH tunnel)
    private static int DB_PORT = 5433;            // Forwarded port (e.g., 5433)
    private static String DB_NAME = "your_database";
    private static String DB_USER = "your_db_user";
    private static String DB_PASSWORD = "your_db_password";

    private static Session sshSession;
    private static Connection connection;

    public static Connection initiateConnection() {
        try {
            loadDatabaseConfig();
            establishSSHTunnel();
            connectToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void loadDatabaseConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config/db.config")) {
            properties.load(fis);
            SSH_HOST = properties.getProperty("SSH_HOST");
            SSH_PORT = Integer.parseInt(properties.getProperty("SSH_PORT"));
            SSH_USER = properties.getProperty("SSH_USER");
            SSH_PASSWORD = properties.getProperty("SSH_PASSWORD");

            DB_HOST = properties.getProperty("DB_HOST");
            DB_PORT = Integer.parseInt(properties.getProperty("DB_PORT"));
            DB_NAME = properties.getProperty("DB_NAME");
            DB_USER = properties.getProperty("DB_USER");
            DB_PASSWORD = properties.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            System.exit(1); // Exit if config file is missing
        }
    }
    private static void establishSSHTunnel() throws JSchException {
        JSch jsch = new JSch();

        sshSession = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        sshSession.setPassword(SSH_PASSWORD);
        sshSession.setConfig("StrictHostKeyChecking", "no");  // Disable host key checking
        sshSession.connect();

        // Forward local port 5433 to remote port 5432
        int assignedPort = sshSession.setPortForwardingL(DB_PORT, "localhost", 5432);
        System.out.println("SSH Tunnel established. Forwarding localhost:" + assignedPort + " to remote database.");
    }

    private static void connectToDatabase() throws SQLException {
        String url = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        System.out.println("Connected to PostgreSQL database successfully!");
    }

    public static void closeConnections() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
            System.out.println("SSH session closed.");
        }
    }
}
