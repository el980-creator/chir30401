/**
 * Chirp - represents a single chirp (post) in the Chirpy system
 * 
 * @author Chirpy Team
 */

package edu.georgetown.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single chirp (post) with author, text content, and timestamp.
 * Supports hashtag extraction and validation.
 */
public class Chirp implements Serializable {
    
    /** Username of the chirp author */
    private String author;
    
    /** Content of the chirp (max 280 characters) */
    private String text;
    
    /** When the chirp was posted */
    private Date timestamp;
    
    /** Maximum allowed length for chirp text */
    public static final int MAX_CHIRP_LENGTH = 280;
    
    /** Regex pattern for hashtag detection */
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#\\w+");

    /**
     * Creates a new chirp with the specified author and text.
     * Timestamp is automatically set to current time.
     * 
     * @param author the username of the chirp author
     * @param text the content of the chirp
     * @throws IllegalArgumentException if author is null/empty or text exceeds max length
     */
    public Chirp(String author, String text) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (text.length() > MAX_CHIRP_LENGTH) {
            throw new IllegalArgumentException("Chirp text cannot exceed " + MAX_CHIRP_LENGTH + " characters");
        }
        
        this.author = author;
        this.text = text;
        this.timestamp = new Date();
    }

    /**
     * Creates a new chirp with the specified author, text, and timestamp.
     * Used for loading chirps from storage or testing.
     * 
     * @param author the username of the chirp author
     * @param text the content of the chirp
     * @param timestamp when the chirp was posted
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Chirp(String author, String text, Date timestamp) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (text.length() > MAX_CHIRP_LENGTH) {
            throw new IllegalArgumentException("Chirp text cannot exceed " + MAX_CHIRP_LENGTH + " characters");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        
        this.author = author;
        this.text = text;
        this.timestamp = new Date(timestamp.getTime()); // Create defensive copy
    }

    /**
     * Gets the username of the chirp's author.
     * 
     * @return the author's username
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Gets the content of the chirp.
     * 
     * @return the chirp text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets when the chirp was created.
     * 
     * @return defensive copy of the timestamp
     */
    public Date getTimestamp() {
        return new Date(this.timestamp.getTime()); // Return defensive copy
    }

    /**
     * Extracts all hashtags from the chirp text.
     * Hashtags are words that start with # (e.g., #Georgetown, #java).
     * 
     * @return list of hashtags found in the text (without the # symbol)
     */
    public List<String> extractHashtags() {
        List<String> hashtags = new ArrayList<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(this.text);
        
        while (matcher.find()) {
            // Remove the # symbol and add to list
            String hashtag = matcher.group().substring(1);
            if (!hashtags.contains(hashtag)) {
                hashtags.add(hashtag);
            }
        }
        
        return hashtags;
    }

    /**
     * Checks if the chirp contains a specific hashtag.
     * Case-insensitive comparison.
     * 
     * @param hashtag the hashtag to search for (with or without # symbol)
     * @return true if the chirp contains the hashtag, false otherwise
     */
    public boolean containsHashtag(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            return false;
        }
        
        // Remove # symbol if present
        String cleanHashtag = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        
        List<String> chirpHashtags = extractHashtags();
        for (String chirpHashtag : chirpHashtags) {
            if (chirpHashtag.equalsIgnoreCase(cleanHashtag)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets a formatted string representation of the chirp for display.
     * 
     * @return formatted string with author, timestamp, and text
     */
    @Override
    public String toString() {
        return String.format("@%s [%s]: %s", author, timestamp.toString(), text);
    }

    /**
     * Checks if two chirps are equal based on author, text, and timestamp.
     * 
     * @param obj the object to compare with
     * @return true if chirps are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Chirp chirp = (Chirp) obj;
        return author.equals(chirp.author) && 
               text.equals(chirp.text) && 
               timestamp.equals(chirp.timestamp);
    }

    /**
     * Generates hash code based on author, text, and timestamp.
     * 
     * @return hash code for the chirp
     */
    @Override
    public int hashCode() {
        return author.hashCode() + text.hashCode() + timestamp.hashCode();
    }
}