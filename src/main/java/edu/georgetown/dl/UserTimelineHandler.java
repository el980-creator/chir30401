package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.georgetown.bll.ChirpService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handler to display all chirps for a particular user.
 */
public class UserTimelineHandler implements HttpHandler {
    private final Logger logger;
    private final ChirpService chirpService;
    private final DisplayLogic displayLogic;
    private static final String TIMELINE_PAGE = "timeline.thtml";

    public UserTimelineHandler(Logger logger, ChirpService chirpService, DisplayLogic displayLogic) {
        this.logger = logger;
        this.chirpService = chirpService;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String username = null;
        if (query != null) {
            String[] pairs = query.split("&");
            for (String p : pairs) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2 && "username".equals(kv[0])) {
                    username = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                }
            }
        }

        if (username == null) {
            // redirect to list users
            exchange.getResponseHeaders().set("Location", "/listusers/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Timeline for " + username);
        data.put("chirps", chirpService.getUserChirps(username));

        StringWriter sw = new StringWriter();
        displayLogic.parseTemplate(TIMELINE_PAGE, data, sw);
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, sw.getBuffer().length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(sw.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
