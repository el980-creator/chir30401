package edu.georgetown.dl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;

public class ListCookiesHandler implements HttpHandler {

    final String COOKIELIST_PAGE = "showcookies.thtml";
    private Logger logger;
    private DisplayLogic displayLogic;
    private UserService userService;

    public ListCookiesHandler(Logger log, DisplayLogic dl, UserService userService) {
        logger = log;
        displayLogic = dl;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("ListCookiesHandler called");

        // grab cookies
        Map<String, String> cookies = displayLogic.getCookies(exchange);

        // like other uses
        Map<String, Object> dataModel = new HashMap<String, Object>();

        dataModel.put("cookienames", cookies.keySet());
        dataModel.put("cookievalues", cookies.values());
        
        // Check if user is logged in by looking up session token
        String sessionToken = cookies.get("chirpy-session-id");
        if (sessionToken != null) {
            String username = userService.getUsernameForSession(sessionToken);
            if (username != null) {
                dataModel.put("loggedInUser", username);
            }
        }

        // output parse template
        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(COOKIELIST_PAGE, dataModel, sw);

        // set html
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}