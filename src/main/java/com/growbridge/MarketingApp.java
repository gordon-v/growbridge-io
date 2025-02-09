package com.growbridge;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Scanner;

public class MarketingApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static Connection connection;
    private static int loggedInUserId = -1; // -1 indicates no user is logged in

    public static void main(String[] args) {

        connection =  SSHPostgresConnection.initiateConnection();

        while (true) {
            // Show different options based on whether the user is logged in
            if (loggedInUserId == -1) {
                // If not logged in, show only Register and Login options
                System.out.println("\n=== Marketing App ===");
                System.out.println("1. Register User");
                System.out.println("2. Login User");
                System.out.println("8. Exit");
                System.out.print("Choose an option: ");
            } else {
                // If logged in, show full set of options
                System.out.println("\n=== Marketing App ===");
                System.out.println("3. Link Social Media Account");
                System.out.println("4. Create Profile Marketing Request");
                System.out.println("5. Create Post Marketing Request");
                System.out.println("6. View and Track Marketing Requests");
                System.out.println("7. Logout");
                System.out.println("8. Exit");
                System.out.print("Choose an option: ");
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> linkSocialMedia();
                case 4 -> createProfileMarketingRequest();
                case 5 -> createPostMarketingRequest();
                case 6 -> viewMarketingRequests();
                case 7 -> logoutUser();
                case 8 -> {
                    closeConnection();
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    private static void logoutUser() {
        loggedInUserId = -1;
    }

    private static void closeConnection() {
        SSHPostgresConnection.closeConnections();
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

        String sql = "SELECT userid FROM app_user WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                loggedInUserId = rs.getInt("userid");
                System.out.println("Login successful. User ID: " + loggedInUserId);
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void linkSocialMedia() {
        if (loggedInUserId == -1) {
            System.out.println("You must be logged in to link social media accounts.");
            return;
        }

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
            pstmt.setInt(5, loggedInUserId);
            pstmt.executeUpdate();
            System.out.println("Social media account linked successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    private static void createProfileMarketingRequest() {
        if (loggedInUserId == -1) {
            System.out.println("You must be logged in to create a profile marketing request.");
            return;
        }

        int providerId = 1;  // Profile marketing request always has provider ID 1
        // Query to get the list of linked social media profiles for the logged-in user
        String sql = "SELECT profileid, platform, user_name FROM SocialMediaProfile WHERE userid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            // Check if the user has linked any social media profiles
            if (!rs.next()) {
                System.out.println("You don't have any linked social media profiles.");
                return;
            }

            // Move the cursor back to the beginning of the ResultSet
            rs.beforeFirst();

            // Display the list of linked profiles
            System.out.println("Select a social media profile to associate with the marketing request:");
            int counter = 1;
            while (rs.next()) {
                int profileId = rs.getInt("profileid");
                String platform = rs.getString("platform");
                String userName = rs.getString("user_name");
                System.out.println(counter + ". " + platform + ": " + userName + " (Profile ID: " + profileId + ")");
                counter++;
            }

            // Ask the user to select a profile
            System.out.print("Enter the number of the profile you want to select: ");
            int selectedOption = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            // Validate the selected option
            if (selectedOption < 1 || selectedOption >= counter) {
                System.out.println("Invalid selection.");
                return;
            }

            // Move back to the beginning of the ResultSet to retrieve the chosen profile ID
            rs.beforeFirst();
            int selectedProfileId = -1;
            for (int i = 1; i <= selectedOption; i++) {
                if (rs.next()) {
                    if (i == selectedOption) {
                        selectedProfileId = rs.getInt("profileid");
                    }
                }
            }

            // Now that we have the profile ID, proceed with the marketing request creation
            System.out.print("Enter target followers: ");
            int targetFollowers = scanner.nextInt();
            scanner.nextLine(); // Consume the newline
            LocalDateTime ldt = LocalDateTime.now().plusWeeks(1);
            java.sql.Date timeline = java.sql.Date.valueOf(ldt.toLocalDate());

            // Insert the profile marketing request into the database
            String insertSql = "INSERT INTO ProfileMarketingRequest (target_followers, timeline, profileid, date_created, status, userid, providerid) " +
                    "VALUES (?, ?, ?, NOW(), 'pending', ?, ?)";

            try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, targetFollowers);
                insertPstmt.setDate(2, timeline);
                insertPstmt.setInt(3, selectedProfileId); // Use the selected profile ID
                insertPstmt.setInt(4, loggedInUserId);
                insertPstmt.setInt(5, providerId);
                insertPstmt.executeUpdate();
                System.out.println("Profile marketing request created successfully.");
            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Error fetching social media profiles: " + e.getMessage());
        }
    }




    private static void createPostMarketingRequest() {
        if (loggedInUserId == -1) {
            System.out.println("You must be logged in to create a post marketing request.");
            return;
        }

        // Automatically set provider ID for post marketing request
        int providerId = 0;  // Post marketing request always has provider ID 0

        System.out.print("Enter target likes: ");
        int targetLikes = scanner.nextInt();
        System.out.print("Enter target comments: ");
        int targetComments = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        System.out.print("Enter timeline (YYYY-MM-DD HH:MM:SS): ");
        String timeline = scanner.nextLine();
        System.out.print("Enter post ID: ");
        int postId = scanner.nextInt();

        String sql = "INSERT INTO PostMarketingRequest (target_likes, target_comments, timeline, postid, date_created, status, userid, providerid) " +
                "VALUES (?, ?, ?, ?, NOW(), 'pending', ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, targetLikes);
            pstmt.setInt(2, targetComments);
            pstmt.setString(3, timeline);
            pstmt.setInt(4, postId);
            pstmt.setInt(5, loggedInUserId);
            pstmt.setInt(6, providerId); // Use providerId as 0
            pstmt.executeUpdate();
            System.out.println("Post marketing request created successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }



    private static void viewMarketingRequests() {
        if (loggedInUserId == -1) {
            System.out.println("You must be logged in to view marketing requests.");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            // Viewing Profile Marketing Requests
            ResultSet rs = stmt.executeQuery("SELECT * FROM ProfileMarketingRequest WHERE userid = " + loggedInUserId);
            System.out.println("Profile Marketing Requests:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("requestid") + ", Target Followers: " + rs.getInt("target_followers") +
                        ", Timeline: " + rs.getString("timeline") + ", Status: " + rs.getString("status"));
            }

            // Viewing Post Marketing Requests
            rs = stmt.executeQuery("SELECT * FROM PostMarketingRequest WHERE userid = " + loggedInUserId);
            System.out.println("Post Marketing Requests:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("requestid") + ", Target Likes: " + rs.getInt("target_likes") +
                        ", Target Comments: " + rs.getInt("target_comments") + ", Timeline: " + rs.getString("timeline") +
                        ", Status: " + rs.getString("status"));
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
