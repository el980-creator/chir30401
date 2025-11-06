package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.user.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;

public class LogoutHandler implements HttpHandler {
    private Logger logger;
    private UserService userService;
    private DisplayLogic displayLogic;

    public LogoutHandler(Logger logger, UserService userService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Logout Handler called");

        //get session token from cookies
        Map<String, String> cookies = displayLogic.getCookies(exchange);
        String sessionToken = cookies.get("chirpy-session-id");

        //make sure session exists with the username check
        if (sessionToken != null) {
            String username = userService.getUsernameForSession(sessionToken);
            if (username != null) {
                logger.info("Logging out user: " + username);
                userService.removeSession(sessionToken);
            }

        }
        
        //expire cookie to clear it
         exchange.getResponseHeaders().set("Set-Cookie", "chirpy-session-id=; path=/; Max-Age=0");

         //redirect
         exchange.getResponseHeaders().set("Location", "/login");
         exchange.sendResponseHeaders(302, -1);
         exchange.getResponseBody().close();


    }
}