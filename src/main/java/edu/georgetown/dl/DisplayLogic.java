/**
 * Display logic for Chirpy.
 */

package edu.georgetown.dl;

import freemarker.core.ParseException;
import freemarker.template.*;
import java.util.*;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

public class DisplayLogic {

    private Logger logger;
    private Configuration cfg;

    /**
     * Initializes the display logic. Specifically, it sets up the template engine.
     * You probably don't want to change much of this code, if any.
     * 
     * @param logger the logger to use
     * @throws IOException
     */
    public DisplayLogic(Logger logger) throws IOException {
        this.logger = logger;
        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(new File("resources/templates"));
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        this.logger.info("Disply logic initialized");
    }

    /**
     * Parses a template given the provided `dataModel`, and writes the output to
     * `out`. Templates should be stored in the resources/templates directory. You
     * probably don't want to change this function. See `DefaultPageHandler.java`
     * for an example of how to use this function.
     * 
     * @param templateName the name of the template to use
     * @param dataModel    the variables to use in the template
     * @param out          the output stream to write to
     */
    public void parseTemplate(String templateName, Map<String, Object> dataModel, Writer out) {
        Template template;
        try {
            template = cfg.getTemplate(templateName);
            template.process(dataModel, out);
        } catch (TemplateNotFoundException e) {
            logger.warning(templateName + " not found");
        } catch (MalformedTemplateNameException e) {
            logger.warning("malformed template: " + templateName);
        } catch (ParseException e) {
            logger.warning(templateName + " parse exception: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("IO exception: " + e.getMessage());
        } catch (TemplateException e) {
            e.printStackTrace();
            logger.warning(templateName + " template exception: " + e.getMessage());
        }
    }

    /**
     * This is a helper function which parses the response from a HTML form and puts
     * the results into a Map. This code was adopted from the code at
     * https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection.
     * See `TestFormHandler.java` for an example of how to use this function.
     * You probably don't want to change this function.
     * 
     * @param query the query string to parse
     * @return the form data as a Map
     * @throws UnsupportedEncodingException
     */
    public Map<String, String> parseResponse(HttpExchange exchange) {
        Map<String, String> myMap = new HashMap<String, String>();

        // the data sent via the HTML form ends up in the request body
        byte[] b;
        try {
            b = exchange.getRequestBody().readAllBytes();
        } catch (IOException e) {
            logger.warning("Cannot get request body: " + e);
            return myMap;
        }
        String formData = new String(b);
        if (formData.equals("")) {
            return myMap;
        }

        try {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                myMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (IOException e) {
            logger.warning("IOException: " + e.getMessage());
        }
        return myMap;
    }

    /**
     * Adds a cookie to the response.
     * 
     * @param exchange the HttpExchange object representing the current exchange
     * @param var      the name of the cookie
     * @param val      the value of the cookie
     * @return true iff the cookie was set successfully
     */
    public boolean addCookie(HttpExchange exchange, String var, String val) {
        try {
            exchange.getResponseHeaders().set("Set-Cookie",
                    var + "=" + URLEncoder.encode(val, "UTF-8") + "; path=/");
        } catch (UnsupportedEncodingException e) {
            logger.warning("UnsupportedEncodingException: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets the value of a cookie from the request.
     * 
     * @param exchange the HttpExchange object representing the current exchange
     * @param var      the name of the cookie
     * @return the value of the cookie, or null if the cookie is not set
     */
    public Map<String, String> getCookies(HttpExchange exchange) {
        Map<String, String> cookies = new HashMap<String, String>();
        List<String> cookieList = exchange.getRequestHeaders().get("Cookie");
        if (cookieList == null) {
            return cookies;
        }

        for (String cookieStr : cookieList) {
            String[] cookiesStr = cookieStr.split(";");
            for (String cookie : cookiesStr) {
                String[] parts = cookie.split("=");
                    try {
                        cookies.put(parts[0].trim(), URLDecoder.decode(parts[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        logger.warning("UnsupportedEncodingException: " + e.getMessage());
                        return null;
                    }
             }
        }
        return cookies;
    }
}
