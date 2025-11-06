package edu.georgetown.bll.user;


import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import edu.georgetown.dao.*;
import edu.georgetown.bll.PersistenceService;
import java.util.HashMap;

public class UserService {

    private static Logger logger;

    private Map<String, Chirper> users; // = new HashMap<>(); 
    //just did here for easier view

    // Session management: token -> username
    private Map<String, String> sessionMap = new HashMap<>();
    
    // Persistence service for saving/loading data
    private PersistenceService persistenceService;

        /**
         * Generates a secure random session token
         */
        private String generateSessionToken() {
            return java.util.UUID.randomUUID().toString();
        }

        /**
         * Authenticates user and returns session token if successful, null otherwise
         */
        public String authenticateAndCreateSession(String username, String password) {
            if (!users.containsKey(username)) {
                return null;
            }
            Chirper user = users.get(username);
            if (user.checkPassword(password)) {
                String token = generateSessionToken();
                sessionMap.put(token, username);
                return token;
            }
            return null;
        }

        /**
         * Gets username for a given session token
         */
        public String getUsernameForSession(String token) {
            return sessionMap.get(token);
        }

        /**
         * Removes a session token from the session map (logout)
         */
        public boolean removeSession(String token) {
            if (token == null) {
                return false;
            }
            String username = sessionMap.remove(token);
            if (username != null) {
                logger.info("Removed session for user: " + username);
                return true;
            }
            return false;
        }

    public UserService(Logger log) {
        logger = log;
        logger.info("UserService started");
        this.persistenceService = new PersistenceService();
        this.users = loadUsersFromPersistence();
    }

    //params username and password
    //false if key / username already exists, true otherwise
    public boolean registerUser(String username, String password) {
       
        if (users.containsKey(username)) {
            return false;
        }
        Chirper created = new Chirper(username, password);
        users.put(username, created);
        
        // Save users to persistence
        saveUsersToPersistence();
        
        return true;

    }

    public boolean loginUser(String username, String password) {
        if (!users.containsKey(username)) { //just check if key exists
            return false;
        }

        Chirper user = users.get(username); //actually get object / value

        if (user.checkPassword(password)) {
            return true;
        }

        return false; 
    }

    //Chirper objects are the map values
    public Vector<Chirper> getUsers() {
        // not implemented; you'll need to change this
        return new Vector<>(users.values());
    }

    public Chirper getUserByUsername(String username) {
        return users.get(username); //returns null if not there
    }

    public Vector<String> getAllUsernames() {
        return new Vector<>(users.keySet()); //keys are just the usernames
    }
    
        /**
         * Authenticates a user by username and password.
         * Returns true if authentication is successful, false otherwise.
         */
        public boolean authenticateUser(String username, String password) {
            if (!users.containsKey(username)) {
                return false;
            }
            Chirper user = users.get(username);
            return user.checkPassword(password);
        }

        /**
         * Makes one user follow another user.
         * @param followerUsername the username of the user who wants to follow
         * @param followeeUsername the username of the user to be followed
         * @return true if follow was successful, false otherwise
         */
        public boolean followUser(String followerUsername, String followeeUsername) {
            if (followerUsername == null || followeeUsername == null) {
                return false;
            }
            
            if (followerUsername.equals(followeeUsername)) {
                logger.warning("User " + followerUsername + " attempted to follow themselves");
                return false;
            }
            
            Chirper follower = users.get(followerUsername);
            Chirper followee = users.get(followeeUsername);
            
            if (follower == null || followee == null) {
                logger.warning("Follow attempt failed - one or both users don't exist: " + followerUsername + " -> " + followeeUsername);
                return false;
            }
            
            boolean success = follower.followUser(followee);
            if (success) {
                logger.info("User " + followerUsername + " is now following " + followeeUsername);
                // Save users to persistence after follow relationship change
                saveUsersToPersistence();
            } else {
                logger.info("User " + followerUsername + " is already following " + followeeUsername);
            }
            
            return success;
        }

        /**
         * Makes one user unfollow another user.
         * @param followerUsername the username of the user who wants to unfollow
         * @param followeeUsername the username of the user to be unfollowed
         * @return true if unfollow was successful, false otherwise
         */
        public boolean unfollowUser(String followerUsername, String followeeUsername) {
            if (followerUsername == null || followeeUsername == null) {
                return false;
            }
            
            Chirper follower = users.get(followerUsername);
            Chirper followee = users.get(followeeUsername);
            
            if (follower == null || followee == null) {
                logger.warning("Unfollow attempt failed - one or both users don't exist: " + followerUsername + " -> " + followeeUsername);
                return false;
            }
            
            boolean success = follower.unfollowUser(followee);
            if (success) {
                logger.info("User " + followerUsername + " unfollowed " + followeeUsername);
                // Save users to persistence after unfollow relationship change
                saveUsersToPersistence();
            }
            
            return success;
        }

        /**
         * Checks if one user is following another.
         * @param followerUsername the username of the potential follower
         * @param followeeUsername the username of the potential followee
         * @return true if the first user is following the second, false otherwise
         */
        public boolean isUserFollowing(String followerUsername, String followeeUsername) {
            if (followerUsername == null || followeeUsername == null) {
                return false;
            }
            
            Chirper follower = users.get(followerUsername);
            Chirper followee = users.get(followeeUsername);
            
            if (follower == null || followee == null) {
                return false;
            }
            
            return follower.isFollowing(followee);
        }

        /**
         * Gets the list of usernames that a user is following.
         * @param username the username to get following list for
         * @return vector of usernames being followed
         */
        public Vector<String> getFollowing(String username) {
            Vector<String> followingUsernames = new Vector<>();
            
            Chirper user = users.get(username);
            if (user != null) {
                Vector<Chirper> following = user.getFollowing();
                for (Chirper followedUser : following) {
                    followingUsernames.add(followedUser.getUsername());
                }
            }
            
            return followingUsernames;
        }

        /**
         * Gets the list of usernames that are following a user.
         * @param username the username to get followers for
         * @return vector of follower usernames
         */
        public Vector<String> getFollowers(String username) {
            Vector<String> followerUsernames = new Vector<>();
            
            Chirper user = users.get(username);
            if (user != null) {
                Vector<Chirper> followers = user.getFollowers();
                for (Chirper follower : followers) {
                    followerUsernames.add(follower.getUsername());
                }
            }
            
            return followerUsernames;
        }
        
        /**
         * Load users from persistent storage.
         * @return HashMap of users, empty if file doesn't exist
         */
        private HashMap<String, Chirper> loadUsersFromPersistence() {
            HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
            logger.info("Loaded " + loadedUsers.size() + " users from persistence");
            return loadedUsers;
        }
        
        /**
         * Save current users to persistent storage.
         */
        private void saveUsersToPersistence() {
            try {
                boolean success = persistenceService.saveUsers((HashMap<String, Chirper>) users);
                if (success) {
                    logger.fine("Users saved to persistence successfully");
                } else {
                    logger.warning("Failed to save users to persistence");
                }
            } catch (Exception e) {
                logger.severe("Error saving users to persistence: " + e.getMessage());
            }
        }
    // methods you'll probably want to add:
    //   registerUser
    //   loginUser
    //   etc.

}
