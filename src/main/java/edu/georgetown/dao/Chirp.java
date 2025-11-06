/**
 * Chirp data object for Chirpy 2.0
 *
 * Represents a short message posted by a Chirper. Stored in-memory for this
 * assignment.
 */
package edu.georgetown.dao;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Chirp implements Serializable {

    private final String id;
    private final String author; // username of chirper
    private final String text;
    private final Instant timestamp;
    private final Set<String> hashtags;

    public Chirp(String author, String text) {
        this.id = UUID.randomUUID().toString();
        this.author = author;
        this.text = text;
        this.timestamp = Instant.now();
        this.hashtags = extractHashtags(text);
    }

    private Set<String> extractHashtags(String text) {
        Set<String> tags = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return tags;
        }
        String[] parts = text.split("\\s+");
        for (String p : parts) {
            if (p.startsWith("#") && p.length() > 1) {
                // normalize hashtag to lower-case (without punctuation)
                String tag = p.replaceAll("[^#a-zA-Z0-9_]", "");
                tags.add(tag.toLowerCase());
            }
        }
        return tags;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Set<String> getHashtags() {
        return hashtags;
    }
}
