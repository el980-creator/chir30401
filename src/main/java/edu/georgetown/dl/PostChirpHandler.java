package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.ChirpService;
import edu.georgetown.bll.user.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handler to allow logged-in users to post a new chirp.
 */
public class PostChirpHandler implements HttpHandler {
    private final Logger logger;
    private final UserService userService;
    private final ChirpService chirpService;
    private final DisplayLogic displayLogic;
    private static final String POST_PAGE = "postchirp.thtml";

    public PostChirpHandler(Logger logger, UserService userService, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.userService = userService;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            // show form
            StringWriter sw = new StringWriter();
            displayLogic.parseTemplate(POST_PAGE, Map.of(), sw);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, sw.getBuffer().length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(sw.toString().getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            // parse body
            Map<String, String> params = displayLogic.parseResponse(exchange);
            String text = params.get("text");

            // get session cookie
            Map<String, String> cookies = displayLogic.getCookies(exchange);
            String token = cookies.get("chirpy-session-id");
            String username = null;
            if (token != null) {
                username = userService.getUsernameForSession(token);
            }

            if (username == null) {
                // not logged in
                exchange.getResponseHeaders().set("Location", "/login/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            if (text == null || text.trim().isEmpty()) {
                // redisplay form with error
                Map<String, Object> data = Map.of("error", "Chirp text cannot be empty");
                StringWriter sw = new StringWriter();
                displayLogic.parseTemplate(POST_PAGE, data, sw);
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, sw.getBuffer().length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(sw.toString().getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            // create chirp
            chirpService.createChirp(username, text.trim());

            // redirect to home
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }

        // method not allowed
        String resp = "Method not allowed";
        exchange.sendResponseHeaders(405, resp.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp.getBytes(StandardCharsets.UTF_8));
        }
    }
}
