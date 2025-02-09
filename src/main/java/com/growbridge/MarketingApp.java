package com.growbridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class MarketingApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static String DB_URL, DB_USER, DB_PASSWORD;
    private static Connection connection;

    public static void main(String[] args) {
        loadDatabaseConfig();

        // Open database connection at application start
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=== Marketing App ===");
            System.out.println("1. Register User");
            System.out.println("2. Login User");
            System.out.println("3. Link Social Media Account");
            System.out.println("4. Create Profile Marketing Request");
            System.out.println("5. Create Post Marketing Request");
            System.out.println("6. View and Track Marketing Requests");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> linkSocialMedia();
                case 4 -> createProfileMarketingRequest();
                case 5 -> createPostMarketingRequest();
                case 6 -> viewMarketingRequests();
                case 7 -> {
                    closeConnection();
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    private static void loadDatabaseConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config/db.config")) {
            properties.load(fis);
            DB_URL = properties.getProperty("DB_URL");
            DB_USER = properties.getProperty("DB_USER");
            DB_PASSWORD = properties.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            System.exit(1); // Exit if config file is missing
        }
    }

    private static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter contact details: ");
        String contactDetails = scanner.nextLine();

        String sql = "INSERT INTO app_user (username, email, password, contact_details) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, contactDetails);
            pstmt.executeUpdate();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "SELECT id FROM app_user WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful. User ID: " + rs.getInt("id"));
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void linkSocialMedia() {
        System.out.print("Enter user ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter platform (e.g., Twitter, Facebook): ");
        String platform = scanner.nextLine();
        System.out.print("Enter username on platform: ");
        String userName = scanner.nextLine();
        System.out.print("Enter account type (public/private): ");
        String accountType = scanner.nextLine();
        System.out.print("Enter follower count: ");
        int followers = scanner.nextInt();

        String sql = "INSERT INTO SocialMediaProfile (platform, user_name, account_type, followers_count, userid) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, platform);
            pstmt.setString(2, userName);
            pstmt.setString(3, accountType);
            pstmt.setInt(4, followers);
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
            System.out.println("Social media account linked successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void createProfileMarketingRequest() {
        System.out.print("Enter user ID: ");
        int userId = scanner.nextInt();
        System.out.print("Enter provider ID: ");
        int providerId = scanner.nextInt();
        System.out.print("Enter target followers: ");
        int targetFollowers = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter timeline (YYYY-MM-DD HH:MM:SS): ");
        String timeline = scanner.nextLine();
        System.out.print("Enter profile ID: ");
        int profileId = scanner.nextInt();

        String sql = "INSERT INTO ProfileMarketingRequest (target_followers, timeline, profileid, date_created, status, userid, providerid) VALUES (?, ?, ?, NOW(), 'pending', ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, targetFollowers);
            pstmt.setString(2, timeline);
            pstmt.setInt(3, profileId);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, providerId);
            pstmt.executeUpdate();
            System.out.println("Profile marketing request created successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void createPostMarketingRequest() { //TODO: Implement
        System.out.print("Enter user ID: ");
        int userId = scanner.nextInt();
        System.out.print("Enter provider ID: ");
        int providerId = scanner.nextInt();
        System.out.print("Enter target followers: ");
        int targetFollowers = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter timeline (YYYY-MM-DD HH:MM:SS): ");
        String timeline = scanner.nextLine();
        System.out.print("Enter profile ID: ");
        int profileId = scanner.nextInt();

        String sql = "INSERT INTO ProfileMarketingRequest (target_followers, timeline, profileid, date_created, status, userid, providerid) VALUES (?, ?, ?, NOW(), 'pending', ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, targetFollowers);
            pstmt.setString(2, timeline);
            pstmt.setInt(3, profileId);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, providerId);
            pstmt.executeUpdate();
            System.out.println("Profile marketing request created successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void viewMarketingRequests() {
        System.out.print("Enter user ID: ");
        int userId = scanner.nextInt();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM ProfileMarketingRequest WHERE userid = " + userId);
            while (rs.next()) {
                System.out.println("Profile Request ID: " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
