package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;

public class LoginHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private DisplayLogic displayLogic;
    private final String LOGIN_PAGE = "login.thtml";

    public LoginHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("LoginHandler called");

        //same as before with temp / dynamic data handler
        Map<String, Object> dataModel = new HashMap<>();

        //see post or get 
        String method = exchange.getRequestMethod();
        boolean loginSuccess = false;
        String errorMsg = null;
        String username = null;

        if (method.equalsIgnoreCase("POST")) {
            // Parse form data
            String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(formData);
            username = params.get("username");
            String password = params.get("password");

            // Authenticate and create session
            String token = userService.authenticateAndCreateSession(username, password);
            if (token != null) {
                loginSuccess = true;
                // Set chirpy-session-id cookie
                displayLogic.addCookie(exchange, "chirpy-session-id", token);
            } else {
                errorMsg = "Invalid username or password.";
            }
        }

        dataModel.put("loginSuccess", loginSuccess);
        dataModel.put("errorMsg", errorMsg);
        dataModel.put("username", username);
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(LOGIN_PAGE, dataModel, sw);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    // Simple form data parser (application/x-www-form-urlencoded)
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
