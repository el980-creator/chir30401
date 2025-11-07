/**
 * SearchHandler - handles search functionality
 * 
 * @author Chirpy Team
 */

package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import edu.georgetown.bll.ChirpService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Handles search requests and displays search form.
 * Can search for users by username or chirps by hashtag/content.
 */
public class SearchHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private ChirpService chirpService;
    private DisplayLogic displayLogic;
    private final String SEARCH_PAGE = "search.thtml";

    /**
     * Constructor for SearchHandler.
     * 
     * @param logger the logger for logging operations
     * @param userService the user service for user-related operations
     * @param chirpService the chirp service for chirp-related operations
     * @param displayLogic the display logic for template rendering
     */
    public SearchHandler(Logger logger, UserService userService, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("SearchHandler called");

        // Check authentication - search might be available to all users or require login
        String sessionToken = displayLogic.getCookieValue(exchange, "chirpy-session-id");
        String currentUser = null;
        
        if (sessionToken != null) {
            currentUser = userService.getUsernameForSession(sessionToken);
        }

        // Create data model for template
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentUser", currentUser);
        dataModel.put("isLoggedIn", currentUser != null);

        String method = exchange.getRequestMethod();
        String searchQuery = null;
        String searchType = "all"; // Default search type
        boolean hasSearched = false;

        // Check for search query in both GET and POST requests
        if (method.equalsIgnoreCase("GET")) {
            // Handle GET request with query parameters (e.g., from hashtag links)
            String queryString = exchange.getRequestURI().getQuery();
            if (queryString != null) {
                Map<String, String> params = parseQueryString(queryString);
                searchQuery = params.get("q");
                searchType = params.getOrDefault("type", "all");
                hasSearched = searchQuery != null && !searchQuery.trim().isEmpty();
            }
        } else if (method.equalsIgnoreCase("POST")) {
            // Handle POST request from search form
            String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(formData);
            searchQuery = params.get("searchQuery");
            searchType = params.getOrDefault("searchType", "all");
            hasSearched = searchQuery != null && !searchQuery.trim().isEmpty();
        }

        if (hasSearched) {
            // Perform search and redirect to results page
            String encodedQuery = java.net.URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String encodedType = java.net.URLEncoder.encode(searchType, StandardCharsets.UTF_8);
            String redirectUrl = "/searchresults/?q=" + encodedQuery + "&type=" + encodedType;
            
            logger.info("Redirecting search to results page: " + searchQuery + " (type: " + searchType + ")");
            exchange.getResponseHeaders().set("Location", redirectUrl);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Get data for search suggestions
        Vector<String> allUsers = userService.getAllUsernames();
        dataModel.put("allUsers", allUsers);
        dataModel.put("userCount", allUsers.size());

        // Get popular hashtags for suggestions
        List<String> popularHashtags = chirpService.getPopularHashtags(10);
        dataModel.put("popularHashtags", popularHashtags);

        // Get recent search suggestions (could be enhanced with user history)
        dataModel.put("searchQuery", searchQuery);
        dataModel.put("searchType", searchType);

        // Get system statistics
        int totalChirps = chirpService.getChirpCount();
        dataModel.put("totalChirps", totalChirps);

        logger.info("Displaying search page for user: " + (currentUser != null ? currentUser : "anonymous"));

        // Render template
        StringWriter sw = new StringWriter();
        try {
            displayLogic.parseTemplate(SEARCH_PAGE, dataModel, sw);
            
            // Send response
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (Exception e) {
            logger.severe("Error rendering search template: " + e.getMessage());
            e.printStackTrace();
            
            // Send error response
            String errorMsg = "Internal Server Error: " + e.getMessage();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(500, errorMsg.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorMsg.getBytes());
            }
        }
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

    /**
     * Simple form data parser for application/x-www-form-urlencoded data.
     * 
     * @param formData the raw form data string
     * @return map of parameter names to values
     */
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData == null || formData.trim().isEmpty()) {
            return params;
        }
        
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    params.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    );
                } catch (Exception e) {
                    logger.warning("Error parsing form parameter: " + e.getMessage());
                }
            }
        }
        return params;
    }
}