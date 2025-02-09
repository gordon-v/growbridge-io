package com.growbridge;

import com.jcraft.jsch.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SSHPostgresConnection {
    private static final String SSH_HOST = "your.ssh.server.com";  // SSH Server
    private static final int SSH_PORT = 22;                        // SSH Port
    private static final String SSH_USER = "your_ssh_username";    // SSH Username
    private static final String SSH_PRIVATE_KEY = "/path/to/private_key.pem";  // Private key for SSH

    private static final String DB_HOST = "127.0.0.1";  // Localhost (after SSH tunnel)
    private static final int DB_PORT = 5433;            // Forwarded port (e.g., 5433)
    private static final String DB_NAME = "your_database";
    private static final String DB_USER = "your_db_user";
    private static final String DB_PASSWORD = "your_db_password";

    private static Session sshSession;
    private static Connection connection;

    public static void main(String[] args) {
        try {
            establishSSHTunnel();
            connectToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    private static void establishSSHTunnel() throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(SSH_PRIVATE_KEY);  // Use private key authentication

        sshSession = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
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

    private static void closeConnections() {
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
