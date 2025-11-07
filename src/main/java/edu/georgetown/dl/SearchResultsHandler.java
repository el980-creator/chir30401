/**
 * SearchResultsHandler - handles search results display
 * 
 * @author Group 7 - Estheffy De Jesus, Alex Freund, Brian Lee, Fahad Shahbaz
 */

package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import edu.georgetown.bll.ChirpService;
import edu.georgetown.dao.Chirp;
import edu.georgetown.dao.Chirper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Handles search results display with filtering by username, hashtag, or content.
 */
public class SearchResultsHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private ChirpService chirpService;
    private DisplayLogic displayLogic;
    private final String SEARCH_RESULTS_PAGE = "searchresults.thtml";

    /**
     * Constructor for SearchResultsHandler.
     * 
     * @param logger the logger for logging operations
     * @param userService the user service for user-related operations
     * @param chirpService the chirp service for chirp-related operations
     * @param displayLogic the display logic for template rendering
     */
    public SearchResultsHandler(Logger logger, UserService userService, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("SearchResultsHandler called");

        // Check authentication
        String sessionToken = displayLogic.getCookieValue(exchange, "chirpy-session-id");
        String currentUser = null;
        
        if (sessionToken != null) {
            currentUser = userService.getUsernameForSession(sessionToken);
        }

        // Get search parameters from query string
        String queryString = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryString(queryString);
        
        String searchQuery = params.get("q");
        String searchType = params.getOrDefault("type", "all");

        // If no search query, redirect to search page
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            logger.info("No search query provided, redirecting to search page");
            exchange.getResponseHeaders().set("Location", "/search/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        searchQuery = searchQuery.trim();
        logger.info("Processing search: \"" + searchQuery + "\" (type: " + searchType + ") for user: " + 
                   (currentUser != null ? currentUser : "anonymous"));

        // Create data model for template
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentUser", currentUser);
        dataModel.put("isLoggedIn", currentUser != null);
        dataModel.put("searchQuery", searchQuery);
        dataModel.put("searchType", searchType);

        // Perform searches based on type
        List<Chirper> userResults = new ArrayList<>();
        List<Chirp> chirpResults = new ArrayList<>();
        List<String> hashtagResults = new ArrayList<>();
        int totalResults = 0;

        if (searchType.equals("users") || searchType.equals("all")) {
            userResults = searchUsers(searchQuery);
            totalResults += userResults.size();
        }

        if (searchType.equals("chirps") || searchType.equals("all")) {
            chirpResults = searchChirps(searchQuery);
            totalResults += chirpResults.size();
        }

        if (searchType.equals("hashtags") || searchType.equals("all")) {
            // For hashtag search, find chirps containing the hashtag
            String cleanQuery = searchQuery.startsWith("#") ? searchQuery.substring(1) : searchQuery;
            List<Chirp> hashtagChirps = chirpService.getChirpsByHashtag(cleanQuery);
            
            if (!hashtagChirps.isEmpty()) {
                hashtagResults.add(cleanQuery);
                // Add hashtag chirps to chirp results if not already included
                if (searchType.equals("hashtags")) {
                    chirpResults = hashtagChirps;
                    totalResults = hashtagChirps.size();
                } else {
                    // Merge with existing chirp results, avoiding duplicates
                    for (Chirp hashtagChirp : hashtagChirps) {
                        if (!chirpResults.contains(hashtagChirp)) {
                            chirpResults.add(hashtagChirp);
                            totalResults++;
                        }
                    }
                }
            }
        }

        // Add follow/unfollow status for user results (if logged in)
        Map<String, Boolean> followingStatus = new HashMap<>();
        if (currentUser != null) {
            for (Chirper user : userResults) {
                boolean isFollowing = userService.isUserFollowing(currentUser, user.getUsername());
                followingStatus.put(user.getUsername(), isFollowing);
            }
        }

        // Set results in data model
        dataModel.put("userResults", userResults);
        dataModel.put("chirpResults", chirpResults);
        dataModel.put("hashtagResults", hashtagResults);
        dataModel.put("totalResults", totalResults);
        dataModel.put("followingStatus", followingStatus);

        // Add search statistics
        dataModel.put("userResultCount", userResults.size());
        dataModel.put("chirpResultCount", chirpResults.size());
        dataModel.put("hashtagResultCount", hashtagResults.size());

        // Create hashtag highlighter
        dataModel.put("highlightHashtags", new TimelineHandler.HashtagHighlighter());

        logger.info("Search completed: " + totalResults + " total results (" + 
                   userResults.size() + " users, " + chirpResults.size() + " chirps, " + 
                   hashtagResults.size() + " hashtags)");

        // Render template
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(SEARCH_RESULTS_PAGE, dataModel, sw);
        
        // Send response
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Search for users by username.
     * 
     * @param query the search query
     * @return list of matching users
     */
    private List<Chirper> searchUsers(String query) {
        List<Chirper> results = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();
        
        // Remove @ symbol if present
        if (lowercaseQuery.startsWith("@")) {
            lowercaseQuery = lowercaseQuery.substring(1);
        }
        
        Vector<String> allUsernames = userService.getAllUsernames();
        for (String username : allUsernames) {
            if (username.toLowerCase().contains(lowercaseQuery)) {
                Chirper user = userService.getUserByUsername(username);
                if (user != null) {
                    results.add(user);
                }
            }
        }
        
        return results;
    }

    /**
     * Search for chirps by content.
     * 
     * @param query the search query
     * @return list of matching chirps
     */
    private List<Chirp> searchChirps(String query) {
        return chirpService.searchChirps(query);
    }

    /**
     * Parse query string parameters from URL.
     * 
     * @param queryString the query string from the URL
     * @return map of parameter names to values
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.trim().isEmpty()) {
            return params;
        }
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    params.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    );
                } catch (Exception e) {
                    logger.warning("Error parsing query parameter: " + e.getMessage());
                }
            }
        }
        return params;
    }
}