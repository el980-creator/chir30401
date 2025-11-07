/**
 * PostChirpHandler - handles chirp posting functionality
 * 
 * @author Chirpy Team
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles chirp posting requests with authentication and form processing.
 * Integrates with ChirpService to create and store new chirps.
 */
public class PostChirpHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private ChirpService chirpService;
    private DisplayLogic displayLogic;
    private final String POST_PAGE = "post.thtml";

    /**
     * Constructor for PostChirpHandler.
     * 
     * @param logger the logger for logging operations
     * @param userService the user service for authentication
     * @param chirpService the chirp service for creating chirps
     * @param displayLogic the display logic for template rendering
     */
    public PostChirpHandler(Logger logger, UserService userService, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("PostChirpHandler called");

        // Check authentication first
        String sessionToken = displayLogic.getCookieValue(exchange, "chirpy-session-id");
        String currentUser = null;
        
        if (sessionToken != null) {
            currentUser = userService.getUsernameForSession(sessionToken);
        }
        
        // If not authenticated, redirect to login
        if (currentUser == null) {
            logger.info("Unauthenticated user attempted to access post page, redirecting to login");
            exchange.getResponseHeaders().set("Location", "/login/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Create data model for template
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentUser", currentUser);
        
        String method = exchange.getRequestMethod();
        boolean postSuccess = false;
        String errorMsg = null;
        String chirpText = null;
        Chirp createdChirp = null;

        if (method.equalsIgnoreCase("POST")) {
            // Parse form data
            String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(formData);
            chirpText = params.get("chirpText");

            // Validate chirp text
            if (chirpText == null || chirpText.trim().isEmpty()) {
                errorMsg = "Chirp cannot be empty.";
            } else if (chirpText.length() > Chirp.MAX_CHIRP_LENGTH) {
                errorMsg = "Chirp cannot exceed " + Chirp.MAX_CHIRP_LENGTH + " characters.";
            } else {
                try {
                    // Create the chirp
                    createdChirp = chirpService.createChirp(currentUser, chirpText.trim());
                    postSuccess = true;
                    logger.info("User " + currentUser + " posted a new chirp");
                } catch (IllegalArgumentException e) {
                    errorMsg = "Failed to post chirp: " + e.getMessage();
                    logger.warning("Failed to create chirp for user " + currentUser + ": " + e.getMessage());
                }
            }
        }

        // Set template data
        dataModel.put("postSuccess", postSuccess);
        dataModel.put("errorMsg", errorMsg);
        dataModel.put("chirpText", chirpText);
        dataModel.put("createdChirp", createdChirp);
        dataModel.put("maxLength", Chirp.MAX_CHIRP_LENGTH);
        
        // Add user's chirp count
        int userChirpCount = chirpService.getChirpCountByUser(currentUser);
        dataModel.put("userChirpCount", userChirpCount);

        // Render template
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(POST_PAGE, dataModel, sw);
        
        // Send response
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
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
                        java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    );
                } catch (Exception e) {
                    logger.warning("Error parsing form parameter: " + e.getMessage());
                }
            }
        }
        return params;
    }
}