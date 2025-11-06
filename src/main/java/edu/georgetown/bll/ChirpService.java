package edu.georgetown.bll;

import edu.georgetown.dao.Chirp;
import edu.georgetown.dao.Chirper;
import edu.georgetown.bll.user.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Business-logic service for creating and querying Chirps.
 */
public class ChirpService {

    // store chirps per user for quick lookup
    private final Map<String, List<Chirp>> chirpsByUser = new ConcurrentHashMap<>();

    /**
     * Create a new chirp for the given author.
     * @param author username of author
     * @param text text content of the chirp
     * @return created Chirp
     */
    public Chirp createChirp(String author, String text) {
        if (author == null || text == null) {
            throw new IllegalArgumentException("author and text must be non-null");
        }
        Chirp c = new Chirp(author, text);
        chirpsByUser.computeIfAbsent(author, k -> Collections.synchronizedList(new ArrayList<>())).add(c);
        return c;
    }

    /**
     * Get chirps authored by a specific user, newest first.
     */
    public List<Chirp> getUserChirps(String username) {
        List<Chirp> list = chirpsByUser.get(username);
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream()
                .sorted(Comparator.comparing(Chirp::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get home timeline for a user: chirps from users they follow, newest first.
     */
    public List<Chirp> getHomeTimeline(String username, UserService userService) {
        if (username == null || userService == null) {
            return new ArrayList<>();
        }
        Chirper user = userService.getUserByUsername(username);
        if (user == null) {
            return new ArrayList<>();
        }
        List<Chirp> out = new ArrayList<>();
        for (Chirper followed : user.getFollowing()) {
            out.addAll(getUserChirps(followed.getUsername()));
        }
        out.sort(Comparator.comparing(Chirp::getTimestamp).reversed());
        return out;
    }

    /**
     * Search chirps containing a particular hashtag (case-insensitive, pass the tag with or without leading '#').
     */
    public List<Chirp> getChirpsByHashtag(String hashtag) {
        if (hashtag == null) {
            return new ArrayList<>();
        }
        String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        normalized = normalized.toLowerCase();
        List<Chirp> out = new ArrayList<>();
        for (List<Chirp> list : chirpsByUser.values()) {
            for (Chirp c : list) {
                if (c.getHashtags().contains("#" + normalized) || c.getHashtags().contains(normalized)) {
                    out.add(c);
                }
            }
        }
        out.sort(Comparator.comparing(Chirp::getTimestamp).reversed());
        return out;
    }

    /**
     * Return all chirps across all users (newest first). Useful for testing.
     */
    public List<Chirp> getAllChirps() {
        List<Chirp> out = new ArrayList<>();
        for (List<Chirp> list : chirpsByUser.values()) {
            out.addAll(list);
        }
        out.sort(Comparator.comparing(Chirp::getTimestamp).reversed());
        return out;
    }
}
