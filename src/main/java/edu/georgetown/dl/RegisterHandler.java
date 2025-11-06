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
import java.util.logging.Logger;

public class RegisterHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private DisplayLogic displayLogic;
    private final String REGISTER_PAGE = "register.thtml";

    public RegisterHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("RegisterHandler called");

        //temp data model with HTTP
        Map<String, Object> dataModel = new HashMap<>();

        //check get or post
        String method = exchange.getRequestMethod();
        boolean registerSuccess = false;
        String errorMsg = null;
        String username = null;

        if (method.equalsIgnoreCase("POST")) {
            String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(formData);
            username = params.get("username");
            String password = params.get("password");
            String confirm = params.get("confirm");

            //validate 
            if (username == null || password == null || confirm == null) {
                errorMsg = "All fields are required.";
            } else if (!password.equals(confirm)) {
                errorMsg = "Passwords do not match.";
            } else if (!userService.registerUser(username, password)) {
                errorMsg = "Username already exists.";
            } else {
                registerSuccess = true;
            }
        }

        //put data in handler
        dataModel.put("registerSuccess", registerSuccess);
        dataModel.put("errorMsg", errorMsg);
        dataModel.put("username", username);
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(REGISTER_PAGE, dataModel, sw);
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, sw.getBuffer().length());
        OutputStream os = exchange.getResponseBody();
        os.write(sw.toString().getBytes());
        os.close();
    }

    //data parser
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
