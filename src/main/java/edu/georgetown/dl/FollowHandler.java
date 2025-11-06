package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger; 

public class FollowHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private DisplayLogic displayLogic;

    public FollowHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        
        if ("POST".equals(method)) {
            handlePost(exchange);
        } else {
            // Only POST is allowed
            String response = "Method not allowed";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    } 


    //handle posts for following and unfollowing
    public void handlePost(HttpExchange exchange) throws IOException {
        //username from cookies
        String currentUsername = getUsernameFromCookies(exchange);

        if (currentUsername == null) {
            logger.warning("Follow without a session");
            sendRedirect(exchange, "/login/");
            return;
        }

        //parse post data
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> params = parseFormData(requestBody);

        String targetUsername = params.get("targetUser");
        String action = params.get("action"); //follow or unfollowing

        if (targetUsername == null || action == null) {
            logger.warning("Missing parameters in follow action");
            sendRedirect(exchange, "/");
            return;
        }
        
        boolean success = false;
        if ("follow".equals(action)) {
            success = userService.followUser(currentUsername, targetUsername);
        } else if ("unfollow".equals(action)) {
            success = userService.unfollowUser(currentUsername, targetUsername);
        }

        if (success) {
            sendRedirect(exchange, "/userTimeline/?username=" + targetUsername);
        } else {
            logger.warning("Follow/unfollow action failed");
            sendRedirect(exchange, "/userTimeline/?username=" + targetUsername); // redirect back even on failure
        }

        return;


    }


    private String getUsernameFromCookies(HttpExchange exchange) {
        //cookies in header
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        //ex: "sessionToken=abc123; username=Alice"
        if (cookieHeader == null) {
            return null;
        }

        //parsing cookie for session token
        String[] cookies = cookieHeader.split(";");
        //then: ["sessionToken=abc123", " username=Alice"]

        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=");
            //then: ["sessionToken", "abc123"]
            if (parts.length == 2 && "chirpy-session-id".equals(parts[0])) {
                String sessionToken = parts[1];
                return userService.getUsernameForSession(sessionToken);
            }
        }
        return null;
    }

    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData == null || formData.isEmpty()) {
            return params;
        }
        
        String[] pairs = formData.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                } catch (Exception e) {
                    logger.warning("Error decoding data: " + e.getMessage());
                }
            }
        }

        return params;
    }

    //redirect function to different pages
    private void sendRedirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}