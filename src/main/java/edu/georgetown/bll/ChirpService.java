/**
 * ChirpService - Business Logic Layer for managing chirps
 * 
 * @author Chirpy Team
 */

package edu.georgetown.bll;

import edu.georgetown.dao.Chirp;
import edu.georgetown.dao.Chirper;
import edu.georgetown.bll.user.UserService;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service class that handles all business logic related to chirps.
 * Manages chirp creation, storage, retrieval, and search functionality.
 */
public class ChirpService {
    
    private static Logger logger;
    private UserService userService;
    
    /** Storage for all chirps in the system */
    private List<Chirp> allChirps;
    
    /**
     * Constructor for ChirpService.
     * 
     * @param logger the logger instance for logging operations
     * @param userService the user service for user-related operations
     */
    public ChirpService(Logger logger, UserService userService) {
        ChirpService.logger = logger;
        this.userService = userService;
        this.allChirps = new ArrayList<>();
        logger.info("ChirpService started");
    }

    /**
     * Creates and stores a new chirp.
     * 
     * @param authorUsername the username of the chirp author
     * @param text the content of the chirp
     * @return the created Chirp object, or null if creation failed
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Chirp createChirp(String authorUsername, String text) {
        if (authorUsername == null || authorUsername.trim().isEmpty()) {
            logger.warning("Attempted to create chirp with null/empty author");
            throw new IllegalArgumentException("Author username cannot be null or empty");
        }
        
        if (text == null || text.trim().isEmpty()) {
            logger.warning("Attempted to create chirp with null/empty text");
            throw new IllegalArgumentException("Chirp text cannot be null or empty");
        }
        
        // Verify that the user exists
        Chirper author = userService.getUserByUsername(authorUsername);
        if (author == null) {
            logger.warning("Attempted to create chirp for non-existent user: " + authorUsername);
            throw new IllegalArgumentException("User does not exist: " + authorUsername);
        }
        
        try {
            Chirp newChirp = new Chirp(authorUsername, text.trim());
            allChirps.add(newChirp);
            
            logger.info("Created new chirp by " + authorUsername + " with " + 
                       newChirp.extractHashtags().size() + " hashtags");
            
            return newChirp;
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to create chirp: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all chirps posted by a specific user.
     * 
     * @param username the username to search for
     * @return list of chirps by the user, sorted by most recent first
     */
    public List<Chirp> getChirpsByUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return allChirps.stream()
                .filter(chirp -> chirp.getAuthor().equals(username))
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all chirps containing a specific hashtag.
     * 
     * @param hashtag the hashtag to search for (with or without # symbol)
     * @return list of chirps containing the hashtag, sorted by most recent first
     */
    public List<Chirp> getChirpsByHashtag(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return allChirps.stream()
                .filter(chirp -> chirp.containsHashtag(hashtag))
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }

    /**
     * Generates a home timeline for a user showing chirps from users they follow.
     * 
     * @param username the username whose timeline to generate
     * @return list of chirps from followed users, sorted by most recent first
     */
    public List<Chirp> getHomeTimeline(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Chirper user = userService.getUserByUsername(username);
        if (user == null) {
            logger.warning("Attempted to get timeline for non-existent user: " + username);
            return new ArrayList<>();
        }
        
        // Get list of users this user follows
        Vector<Chirper> following = user.getFollowing(); // Now using the correct method
        Set<String> followingUsernames = new HashSet<>();
        
        for (Chirper followedUser : following) {
            followingUsernames.add(followedUser.getUsername());
        }
        
        // Also include the user's own chirps in their timeline
        followingUsernames.add(username);
        
        return allChirps.stream()
                .filter(chirp -> followingUsernames.contains(chirp.getAuthor()))
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }

    /**
     * Gets all chirps in the system.
     * 
     * @return list of all chirps, sorted by most recent first
     */
    public List<Chirp> getAllChirps() {
        return allChirps.stream()
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }

    /**
     * Gets the total number of chirps in the system.
     * 
     * @return the total chirp count
     */
    public int getChirpCount() {
        return allChirps.size();
    }

    /**
     * Gets the number of chirps posted by a specific user.
     * 
     * @param username the username to count chirps for
     * @return the number of chirps by the user
     */
    public int getChirpCountByUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        return (int) allChirps.stream()
                .filter(chirp -> chirp.getAuthor().equals(username))
                .count();
    }

    /**
     * Searches for chirps containing specific text (case-insensitive).
     * 
     * @param searchText the text to search for
     * @return list of chirps containing the search text, sorted by most recent first
     */
    public List<Chirp> searchChirps(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowercaseSearch = searchText.toLowerCase();
        
        return allChirps.stream()
                .filter(chirp -> chirp.getText().toLowerCase().contains(lowercaseSearch))
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }

    /**
     * Gets all unique hashtags used in the system.
     * 
     * @return set of all hashtags (without # symbol)
     */
    public Set<String> getAllHashtags() {
        Set<String> allHashtags = new HashSet<>();
        
        for (Chirp chirp : allChirps) {
            allHashtags.addAll(chirp.extractHashtags());
        }
        
        return allHashtags;
    }

    /**
     * Gets the most popular hashtags (by usage count).
     * 
     * @param limit the maximum number of hashtags to return
     * @return list of hashtags sorted by usage count (descending)
     */
    public List<String> getPopularHashtags(int limit) {
        Map<String, Integer> hashtagCounts = new HashMap<>();
        
        // Count hashtag usage
        for (Chirp chirp : allChirps) {
            for (String hashtag : chirp.extractHashtags()) {
                hashtagCounts.put(hashtag, hashtagCounts.getOrDefault(hashtag, 0) + 1);
            }
        }
        
        // Sort by count and return top results
        return hashtagCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending order
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Deletes all chirps (for testing purposes).
     */
    public void clearAllChirps() {
        allChirps.clear();
        logger.info("All chirps cleared from system");
    }

    /**
     * Removes a specific chirp from the system.
     * 
     * @param chirp the chirp to remove
     * @return true if the chirp was removed, false if it wasn't found
     */
    public boolean removeChirp(Chirp chirp) {
        if (chirp == null) {
            return false;
        }
        
        boolean removed = allChirps.remove(chirp);
        if (removed) {
            logger.info("Removed chirp by " + chirp.getAuthor());
        }
        
        return removed;
    }
}