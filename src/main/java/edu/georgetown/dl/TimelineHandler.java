/**
 * TimelineHandler - handles timeline display functionality
 * 
 * @author Group 7 - Estheffy De Jesus, Alex Freund, Brian Lee, Fahad Shahbaz
 */

package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import edu.georgetown.bll.ChirpService;
import edu.georgetown.dao.Chirp;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles timeline requests showing chronological feed of chirps.
 * Displays home timeline with chirps from followed users plus user's own chirps.
 */
public class TimelineHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private ChirpService chirpService;
    private DisplayLogic displayLogic;
    private final String TIMELINE_PAGE = "timeline.thtml";

    /**
     * Constructor for TimelineHandler.
     * 
     * @param logger the logger for logging operations
     * @param userService the user service for authentication
     * @param chirpService the chirp service for retrieving chirps
     * @param displayLogic the display logic for template rendering
     */
    public TimelineHandler(Logger logger, UserService userService, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("TimelineHandler called");

        // Check authentication first
        String sessionToken = displayLogic.getCookieValue(exchange, "chirpy-session-id");
        String currentUser = null;
        
        if (sessionToken != null) {
            currentUser = userService.getUsernameForSession(sessionToken);
        }
        
        // If not authenticated, redirect to login
        if (currentUser == null) {
            logger.info("Unauthenticated user attempted to access timeline, redirecting to login");
            exchange.getResponseHeaders().set("Location", "/login/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Create data model for template
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentUser", currentUser);

        // Get timeline chirps for the current user
        List<Chirp> timelineChirps = chirpService.getHomeTimeline(currentUser);
        dataModel.put("chirps", timelineChirps);
        dataModel.put("chirpCount", timelineChirps.size());

        // Get user statistics
        int userChirpCount = chirpService.getChirpCountByUser(currentUser);
        dataModel.put("userChirpCount", userChirpCount);

        // Get total system statistics
        int totalChirps = chirpService.getChirpCount();
        dataModel.put("totalChirps", totalChirps);

        // Get popular hashtags for display
        List<String> popularHashtags = chirpService.getPopularHashtags(5);
        dataModel.put("popularHashtags", popularHashtags);

        // Create a date formatter for chirp timestamps
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");
        dataModel.put("dateFormat", dateFormat);

        // Create helper function for hashtag highlighting in template
        dataModel.put("highlightHashtags", new HashtagHighlighter());

        logger.info("Displaying timeline for user " + currentUser + " with " + timelineChirps.size() + " chirps");

        // Render template
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(TIMELINE_PAGE, dataModel, sw);
        
        // Send response
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Helper class for highlighting hashtags in chirp text.
     * Used in FreeMarker templates to add styling to hashtags.
     */
    public static class HashtagHighlighter {
        public String highlight(String text) {
            if (text == null) {
                return "";
            }
            // Replace hashtags with styled spans
            return text.replaceAll("#(\\w+)", "<span class=\"hashtag\">#$1</span>");
        }
    }
}