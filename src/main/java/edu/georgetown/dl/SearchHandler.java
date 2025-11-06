package edu.georgetown.dl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple search handler: presents a form and redirects to a userTimeline or hashtag page.
 */
public class SearchHandler implements HttpHandler {
    private final Logger logger;
    private final DisplayLogic displayLogic;
    private static final String SEARCH_PAGE = "search.thtml";

    public SearchHandler(Logger logger, DisplayLogic displayLogic) {
        this.logger = logger;
        this.displayLogic = displayLogic;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            StringWriter sw = new StringWriter();
            displayLogic.parseTemplate(SEARCH_PAGE, Map.of(), sw);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, sw.getBuffer().length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(sw.toString().getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        if ("POST".equalsIgnoreCase(method)) {
            Map<String, String> params = displayLogic.parseResponse(exchange);
            String query = params.get("query");
            if (query == null || query.trim().isEmpty()) {
                exchange.getResponseHeaders().set("Location", "/search/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }
            query = query.trim();
            if (query.startsWith("#")) {
                String tag = query.substring(1);
                exchange.getResponseHeaders().set("Location", "/hashtag/?tag=" + java.net.URLEncoder.encode(tag, "UTF-8"));
            } else {
                exchange.getResponseHeaders().set("Location", "/userTimeline/?username=" + java.net.URLEncoder.encode(query, "UTF-8"));
            }
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }

        String resp = "Method not allowed";
        exchange.sendResponseHeaders(405, resp.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp.getBytes(StandardCharsets.UTF_8));
        }
    }
}
