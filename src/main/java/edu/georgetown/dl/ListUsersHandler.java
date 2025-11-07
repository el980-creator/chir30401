package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ListUsersHandler implements HttpHandler {
    private final Logger logger;
    private final UserService userService;
    private final DisplayLogic displayLogic;
    private static final String LIST_USERS_PAGE = "listusers.thtml";

    public ListUsersHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("ListUsersHandler called");

        // Create data model to pass dynamic content to the template, temp for HTTP response
        Map<String, Object> dataModel = new HashMap<>();

        //call to bll
        List<String> usernames = new ArrayList<>(userService.getAllUsernames());
        dataModel.put("usernames", usernames);

        //hold data for html
        StringWriter sw = new StringWriter();
        try {
            displayLogic.parseTemplate(LIST_USERS_PAGE, dataModel, sw);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            
            byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);

            //write to html
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (Exception e) {
            logger.severe("Error rendering listusers template: " + e.getMessage());
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
}
