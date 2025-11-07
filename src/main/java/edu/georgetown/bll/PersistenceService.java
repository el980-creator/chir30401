/**
 * PersistenceService - handles JSON file persistence for users and chirps
 * 
 * @author Group 7 - Estheffy De Jesus, Alex Freund, Brian Lee, Fahad Shahbaz
 */

package edu.georgetown.bll;

import edu.georgetown.dao.Chirp;
import edu.georgetown.dao.Chirper;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service for persisting and loading application data to/from JSON files.
 * Handles users.json and chirps.json files in the data directory.
 * Uses simple custom JSON serialization to avoid external dependencies.
 */
public class PersistenceService {
    private static final Logger logger = Logger.getLogger(PersistenceService.class.getName());
    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = DATA_DIR + "users.json";
    private static final String CHIRPS_FILE = DATA_DIR + "chirps.json";
    
    private final SimpleDateFormat dateFormat;
    
    /**
     * Constructor initializes date format for JSON serialization.
     */
    public PersistenceService() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
    
    /**
     * Save users to JSON file.
     * 
     * @param users HashMap of username to Chirper objects
     * @return true if save was successful, false otherwise
     */
    public boolean saveUsers(HashMap<String, Chirper> users) {
        try {
            ensureDataDirectory();
            
            StringBuilder json = new StringBuilder();
            json.append("[\n");
            
            boolean first = true;
            for (Map.Entry<String, Chirper> entry : users.entrySet()) {
                if (!first) json.append(",\n");
                first = false;
                
                Chirper chirper = entry.getValue();
                json.append("  {\n");
                json.append("    \"username\": \"").append(escapeJson(chirper.getUsername())).append("\",\n");
                json.append("    \"password\": \"").append(escapeJson(chirper.getPassword())).append("\",\n");
                
                // Followers array
                json.append("    \"followers\": [");
                boolean firstFollower = true;
                for (Chirper follower : chirper.getFollowers()) {
                    if (!firstFollower) json.append(", ");
                    firstFollower = false;
                    json.append("\"").append(escapeJson(follower.getUsername())).append("\"");
                }
                json.append("],\n");
                
                // Following array
                json.append("    \"following\": [");
                boolean firstFollowing = true;
                for (Chirper following : chirper.getFollowing()) {
                    if (!firstFollowing) json.append(", ");
                    firstFollowing = false;
                    json.append("\"").append(escapeJson(following.getUsername())).append("\"");
                }
                json.append("]\n");
                json.append("  }");
            }
            
            json.append("\n]");
            
            try (FileWriter writer = new FileWriter(USERS_FILE)) {
                writer.write(json.toString());
                logger.info("Successfully saved " + users.size() + " users to " + USERS_FILE);
                return true;
            }
        } catch (Exception e) {
            logger.severe("Failed to save users: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load users from JSON file.
     * 
     * @return HashMap of username to Chirper objects, empty if file doesn't exist or error occurs
     */
    public HashMap<String, Chirper> loadUsers() {
        HashMap<String, Chirper> users = new HashMap<>();
        
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            logger.info("Users file " + USERS_FILE + " does not exist, starting with empty user base");
            return users;
        }
        
        try {
            String content = readFile(USERS_FILE);
            List<Map<String, Object>> userData = parseUsersJson(content);
            
            // First pass: create all users without relationships
            for (Map<String, Object> data : userData) {
                String username = (String) data.get("username");
                String password = (String) data.get("password");
                
                Chirper chirper = new Chirper(username, password);
                users.put(username, chirper);
            }
            
            // Second pass: restore relationships now that all users exist
            for (Map<String, Object> data : userData) {
                String username = (String) data.get("username");
                Chirper chirper = users.get(username);
                
                @SuppressWarnings("unchecked")
                List<String> followerUsernames = (List<String>) data.get("followers");
                if (followerUsernames != null) {
                    for (String followerUsername : followerUsernames) {
                        Chirper follower = users.get(followerUsername);
                        if (follower != null) {
                            chirper.addFollower(follower);
                        }
                    }
                }
                
                @SuppressWarnings("unchecked")
                List<String> followingUsernames = (List<String>) data.get("following");
                if (followingUsernames != null) {
                    for (String followingUsername : followingUsernames) {
                        Chirper following = users.get(followingUsername);
                        if (following != null) {
                            chirper.getFollowing().add(following);
                        }
                    }
                }
            }
            
            logger.info("Successfully loaded " + users.size() + " users from " + USERS_FILE);
        } catch (Exception e) {
            logger.severe("Failed to load users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Save chirps to JSON file.
     * 
     * @param chirps ArrayList of Chirp objects
     * @return true if save was successful, false otherwise
     */
    public boolean saveChirps(ArrayList<Chirp> chirps) {
        try {
            ensureDataDirectory();
            
            StringBuilder json = new StringBuilder();
            json.append("[\n");
            
            boolean first = true;
            for (Chirp chirp : chirps) {
                if (!first) json.append(",\n");
                first = false;
                
                json.append("  {\n");
                json.append("    \"author\": \"").append(escapeJson(chirp.getAuthor())).append("\",\n");
                json.append("    \"text\": \"").append(escapeJson(chirp.getText())).append("\",\n");
                json.append("    \"timestamp\": \"").append(dateFormat.format(chirp.getTimestamp())).append("\",\n");
                
                // Hashtags array
                json.append("    \"hashtags\": [");
                boolean firstHashtag = true;
                for (String hashtag : chirp.extractHashtags()) {
                    if (!firstHashtag) json.append(", ");
                    firstHashtag = false;
                    json.append("\"").append(escapeJson(hashtag)).append("\"");
                }
                json.append("]\n");
                json.append("  }");
            }
            
            json.append("\n]");
            
            try (FileWriter writer = new FileWriter(CHIRPS_FILE)) {
                writer.write(json.toString());
                logger.info("Successfully saved " + chirps.size() + " chirps to " + CHIRPS_FILE);
                return true;
            }
        } catch (Exception e) {
            logger.severe("Failed to save chirps: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load chirps from JSON file.
     * 
     * @return ArrayList of Chirp objects, empty if file doesn't exist or error occurs
     */
    public ArrayList<Chirp> loadChirps() {
        ArrayList<Chirp> chirps = new ArrayList<>();
        
        File file = new File(CHIRPS_FILE);
        if (!file.exists()) {
            logger.info("Chirps file " + CHIRPS_FILE + " does not exist, starting with empty chirp collection");
            return chirps;
        }
        
        try {
            String content = readFile(CHIRPS_FILE);
            List<Map<String, Object>> chirpData = parseChirpsJson(content);
            
            for (Map<String, Object> data : chirpData) {
                String author = (String) data.get("author");
                String text = (String) data.get("text");
                String timestampStr = (String) data.get("timestamp");
                
                Date timestamp = dateFormat.parse(timestampStr);
                
                // Create chirp using the constructor that takes timestamp
                Chirp chirp = new Chirp(author, text, timestamp);
                chirps.add(chirp);
            }
            
            logger.info("Successfully loaded " + chirps.size() + " chirps from " + CHIRPS_FILE);
        } catch (Exception e) {
            logger.severe("Failed to load chirps: " + e.getMessage());
        }
        
        return chirps;
    }
    
    /**
     * Ensure the data directory exists.
     */
    private void ensureDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            logger.info("Created data directory: " + DATA_DIR);
        }
    }
    
    /**
     * Escape special characters for JSON strings.
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Read entire file content as string.
     */
    private String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Simple JSON parser for users array.
     */
    private List<Map<String, Object>> parseUsersJson(String content) {
        List<Map<String, Object>> users = new ArrayList<>();
        
        // Simple state machine parser for JSON array of user objects
        content = content.trim();
        if (!content.startsWith("[") || !content.endsWith("]")) {
            return users;
        }
        
        content = content.substring(1, content.length() - 1); // Remove [ ]
        
        // Split by },{ pattern but preserve the braces
        String[] userBlocks = content.split("\\s*\\}\\s*,\\s*\\{\\s*");
        
        for (int i = 0; i < userBlocks.length; i++) {
            String block = userBlocks[i].trim();
            
            // Add back braces
            if (i == 0 && !block.startsWith("{")) block = "{" + block;
            if (i == userBlocks.length - 1 && !block.endsWith("}")) block = block + "}";
            if (i > 0 && i < userBlocks.length - 1) block = "{" + block + "}";
            
            Map<String, Object> user = parseUserObject(block);
            if (user != null) {
                users.add(user);
            }
        }
        
        return users;
    }
    
    /**
     * Parse a single user JSON object.
     */
    private Map<String, Object> parseUserObject(String jsonObject) {
        Map<String, Object> user = new HashMap<>();
        
        jsonObject = jsonObject.trim();
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}")) {
            return null;
        }
        
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1); // Remove { }
        
        // Parse key-value pairs
        String[] pairs = jsonObject.split(",(?=\\s*\"\\w+\"\\s*:)");
        
        for (String pair : pairs) {
            pair = pair.trim();
            int colonIndex = pair.indexOf(':');
            if (colonIndex == -1) continue;
            
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();
            
            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            
            // Parse value based on type
            if (value.startsWith("[") && value.endsWith("]")) {
                // Array value
                List<String> arrayValue = parseStringArray(value);
                user.put(key, arrayValue);
            } else if (value.startsWith("\"") && value.endsWith("\"")) {
                // String value
                String stringValue = value.substring(1, value.length() - 1);
                stringValue = unescapeJson(stringValue);
                user.put(key, stringValue);
            }
        }
        
        return user;
    }
    
    /**
     * Parse string array from JSON.
     */
    private List<String> parseStringArray(String arrayStr) {
        List<String> result = new ArrayList<>();
        
        arrayStr = arrayStr.trim();
        if (!arrayStr.startsWith("[") || !arrayStr.endsWith("]")) {
            return result;
        }
        
        arrayStr = arrayStr.substring(1, arrayStr.length() - 1); // Remove [ ]
        
        if (arrayStr.trim().isEmpty()) {
            return result;
        }
        
        String[] elements = arrayStr.split(",");
        for (String element : elements) {
            element = element.trim();
            if (element.startsWith("\"") && element.endsWith("\"")) {
                String value = element.substring(1, element.length() - 1);
                value = unescapeJson(value);
                result.add(value);
            }
        }
        
        return result;
    }
    
    /**
     * Simple JSON parser for chirps array.
     */
    private List<Map<String, Object>> parseChirpsJson(String content) {
        List<Map<String, Object>> chirps = new ArrayList<>();
        
        // Simple state machine parser for JSON array of chirp objects
        content = content.trim();
        if (!content.startsWith("[") || !content.endsWith("]")) {
            return chirps;
        }
        
        content = content.substring(1, content.length() - 1); // Remove [ ]
        
        // Split by },{ pattern but preserve the braces
        String[] chirpBlocks = content.split("\\s*\\}\\s*,\\s*\\{\\s*");
        
        for (int i = 0; i < chirpBlocks.length; i++) {
            String block = chirpBlocks[i].trim();
            
            // Add back braces
            if (i == 0 && !block.startsWith("{")) block = "{" + block;
            if (i == chirpBlocks.length - 1 && !block.endsWith("}")) block = block + "}";
            if (i > 0 && i < chirpBlocks.length - 1) block = "{" + block + "}";
            
            Map<String, Object> chirp = parseChirpObject(block);
            if (chirp != null) {
                chirps.add(chirp);
            }
        }
        
        return chirps;
    }
    
    /**
     * Parse a single chirp JSON object.
     */
    private Map<String, Object> parseChirpObject(String jsonObject) {
        Map<String, Object> chirp = new HashMap<>();
        
        jsonObject = jsonObject.trim();
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}")) {
            return null;
        }
        
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1); // Remove { }
        
        // Parse key-value pairs
        String[] pairs = jsonObject.split(",(?=\\s*\"\\w+\"\\s*:)");
        
        for (String pair : pairs) {
            pair = pair.trim();
            int colonIndex = pair.indexOf(':');
            if (colonIndex == -1) continue;
            
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();
            
            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }
            
            // Parse value based on type
            if (value.startsWith("[") && value.endsWith("]")) {
                // Array value (for hashtags)
                List<String> arrayValue = parseStringArray(value);
                chirp.put(key, arrayValue);
            } else if (value.startsWith("\"") && value.endsWith("\"")) {
                // String value
                String stringValue = value.substring(1, value.length() - 1);
                stringValue = unescapeJson(stringValue);
                chirp.put(key, stringValue);
            }
        }
        
        return chirp;
    }
    
    /**
     * Unescape JSON special characters.
     */
    private String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\\", "\\")
                  .replace("\\\"", "\"")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
}