/**
 * LogoutHandler - handles user logout functionality
 * 
 * @author Chirpy Team
 */

package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles logout requests by clearing session tokens and cookies.
 * Redirects users to the home page after successful logout.
 */
public class LogoutHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private DisplayLogic displayLogic;
    private final String LOGOUT_PAGE = "logout.thtml";

    /**
     * Constructor for LogoutHandler.
     * 
     * @param logger the logger for logging operations
     * @param userService the user service for session management
     * @param displayLogic the display logic for template rendering
     */
    public LogoutHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("LogoutHandler called");

        // Create data model for template
        Map<String, Object> dataModel = new HashMap<>();
        
        String method = exchange.getRequestMethod();
        boolean logoutPerformed = false;
        String username = null;

        // Get current user from session before logging out
        String sessionToken = displayLogic.getCookieValue(exchange, "chirpy-session-id");
        if (sessionToken != null) {
            username = userService.getUsernameForSession(sessionToken);
        }

        if (method.equalsIgnoreCase("POST")) {
            // Perform logout
            if (sessionToken != null) {
                // Remove session from server
                boolean sessionRemoved = userService.removeSession(sessionToken);
                
                if (sessionRemoved && username != null) {
                    logger.info("User " + username + " logged out successfully");
                    logoutPerformed = true;
                    
                    // Clear the cookie by setting it to expire
                    displayLogic.removeCookie(exchange, "chirpy-session-id");
                } else {
                    logger.warning("Failed to remove session for token: " + sessionToken);
                }
            } else {
                logger.info("Logout attempted but no session found");
                logoutPerformed = true; // Still show success even if no session
            }
        }

        // Set template data
        dataModel.put("logoutPerformed", logoutPerformed);
        dataModel.put("username", username);
        dataModel.put("wasLoggedIn", username != null);

        // Render template
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(LOGOUT_PAGE, dataModel, sw);
        
        // Send response
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, sw.getBuffer().length());
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(sw.toString().getBytes());
        }
    }
}