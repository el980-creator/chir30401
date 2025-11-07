package edu.georgetown;

import edu.georgetown.bll.PersistenceService;
import edu.georgetown.dao.Chirp;
import edu.georgetown.dao.Chirper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/**
 * Unit tests for the PersistenceService BLL class.
 * Tests JSON serialization, file operations, and data persistence functionality.
 */
public class PersistenceServiceTest {
    
    private PersistenceService persistenceService;
    private final String TEST_DATA_DIR = "test-data/";
    
    @BeforeEach
    public void setUp() {
        persistenceService = new PersistenceService();
        // Create test data directory
        new File(TEST_DATA_DIR).mkdirs();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test files
        deleteFile(TEST_DATA_DIR + "users.json");
        deleteFile(TEST_DATA_DIR + "chirps.json");
        new File(TEST_DATA_DIR).delete();
    }
    
    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    public void testSaveAndLoadUsers() {
        HashMap<String, Chirper> users = new HashMap<>();
        users.put("user1", new Chirper("user1", "pass1"));
        users.put("user2", new Chirper("user2", "pass2"));
        
        boolean saveResult = persistenceService.saveUsers(users);
        assertTrue(saveResult, "Should successfully save users");
        
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertEquals(2, loadedUsers.size(), "Should load correct number of users");
        assertTrue(loadedUsers.containsKey("user1"), "Should contain user1");
        assertTrue(loadedUsers.containsKey("user2"), "Should contain user2");
        assertEquals("user1", loadedUsers.get("user1").getUsername(), "User1 username should match");
        assertEquals("user2", loadedUsers.get("user2").getUsername(), "User2 username should match");
    }
    
    @Test
    public void testSaveAndLoadEmptyUsers() {
        HashMap<String, Chirper> emptyUsers = new HashMap<>();
        
        boolean saveResult = persistenceService.saveUsers(emptyUsers);
        assertTrue(saveResult, "Should successfully save empty users map");
        
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertTrue(loadedUsers.isEmpty(), "Should load empty users map");
    }
    
    @Test
    public void testSaveAndLoadUsersWithRelationships() {
        HashMap<String, Chirper> users = new HashMap<>();
        Chirper user1 = new Chirper("user1", "pass1");
        Chirper user2 = new Chirper("user2", "pass2");
        
        // Establish relationships
        user1.followUser(user2);
        
        users.put("user1", user1);
        users.put("user2", user2);
        
        boolean saveResult = persistenceService.saveUsers(users);
        assertTrue(saveResult, "Should successfully save users with relationships");
        
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertEquals(2, loadedUsers.size(), "Should load correct number of users");
        
        Chirper loadedUser1 = loadedUsers.get("user1");
        Chirper loadedUser2 = loadedUsers.get("user2");
        
        assertNotNull(loadedUser1, "User1 should be loaded");
        assertNotNull(loadedUser2, "User2 should be loaded");
        
        // Check if relationships are preserved
        assertTrue(loadedUser1.getFollowing().contains(loadedUser2), "User1 should still be following user2");
        assertTrue(loadedUser2.getFollowers().contains(loadedUser1), "User2 should still have user1 as follower");
    }
    
    @Test
    public void testSaveAndLoadChirps() {
        ArrayList<Chirp> chirps = new ArrayList<>();
        chirps.add(new Chirp("user1", "Hello world #test"));
        chirps.add(new Chirp("user2", "Another chirp #hashtag"));
        
        boolean saveResult = persistenceService.saveChirps(chirps);
        assertTrue(saveResult, "Should successfully save chirps");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertEquals(2, loadedChirps.size(), "Should load correct number of chirps");
        assertEquals("user1", loadedChirps.get(0).getAuthor(), "First chirp author should match");
        assertEquals("user2", loadedChirps.get(1).getAuthor(), "Second chirp author should match");
        assertEquals("Hello world #test", loadedChirps.get(0).getText(), "First chirp text should match");
        assertEquals("Another chirp #hashtag", loadedChirps.get(1).getText(), "Second chirp text should match");
    }
    
    @Test
    public void testSaveAndLoadEmptyChirps() {
        ArrayList<Chirp> emptyChirps = new ArrayList<>();
        
        boolean saveResult = persistenceService.saveChirps(emptyChirps);
        assertTrue(saveResult, "Should successfully save empty chirps list");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertTrue(loadedChirps.isEmpty(), "Should load empty chirps list");
    }
    
    @Test
    public void testSaveAndLoadChirpsWithSpecialCharacters() {
        ArrayList<Chirp> chirps = new ArrayList<>();
        chirps.add(new Chirp("user1", "Special chars: \"quotes\" 'apostrophe' \\backslash ñ unicode"));
        chirps.add(new Chirp("user2", "Newlines\nand\ttabs"));
        
        boolean saveResult = persistenceService.saveChirps(chirps);
        assertTrue(saveResult, "Should successfully save chirps with special characters");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertEquals(2, loadedChirps.size(), "Should load correct number of chirps");
        assertEquals("Special chars: \"quotes\" 'apostrophe' \\backslash ñ unicode", 
                    loadedChirps.get(0).getText(), "Special characters should be preserved");
        assertEquals("Newlines\nand\ttabs", 
                    loadedChirps.get(1).getText(), "Newlines and tabs should be preserved");
    }
    
    @Test
    public void testSaveAndLoadChirpsWithTimestamps() {
        Date testDate = new Date();
        ArrayList<Chirp> chirps = new ArrayList<>();
        chirps.add(new Chirp("user1", "Timestamped chirp", testDate));
        
        boolean saveResult = persistenceService.saveChirps(chirps);
        assertTrue(saveResult, "Should successfully save chirps with timestamps");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertEquals(1, loadedChirps.size(), "Should load correct number of chirps");
        
        Chirp loadedChirp = loadedChirps.get(0);
        assertEquals(testDate.getTime(), loadedChirp.getTimestamp().getTime(), 
                    "Timestamp should be preserved (within reasonable tolerance)");
    }
    
    @Test
    public void testLoadUsersFromNonexistentFile() {
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertTrue(loadedUsers.isEmpty(), "Should return empty map when file doesn't exist");
    }
    
    @Test
    public void testLoadChirpsFromNonexistentFile() {
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertTrue(loadedChirps.isEmpty(), "Should return empty list when file doesn't exist");
    }
    
    @Test
    public void testSaveUsersWithNullValues() {
        HashMap<String, Chirper> users = new HashMap<>();
        users.put("user1", new Chirper("user1", "pass1"));
        users.put("user2", null); // This should be handled gracefully
        
        // The method should handle null values without crashing
        assertDoesNotThrow(() -> {
            persistenceService.saveUsers(users);
        }, "Should handle null values gracefully");
    }
    
    @Test
    public void testSaveUsersWithEmptyUsernames() {
        HashMap<String, Chirper> users = new HashMap<>();
        users.put("", new Chirper("", "pass1"));
        users.put("normaluser", new Chirper("normaluser", "pass2"));
        
        boolean saveResult = persistenceService.saveUsers(users);
        assertTrue(saveResult, "Should successfully save users with empty usernames");
        
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertEquals(2, loadedUsers.size(), "Should load both users");
        assertTrue(loadedUsers.containsKey(""), "Should contain empty username user");
        assertTrue(loadedUsers.containsKey("normaluser"), "Should contain normal user");
    }
    
    @Test
    public void testSaveChirpsWithMaxLengthText() {
        String maxLengthText = "a".repeat(280);
        ArrayList<Chirp> chirps = new ArrayList<>();
        chirps.add(new Chirp("user1", maxLengthText));
        
        boolean saveResult = persistenceService.saveChirps(chirps);
        assertTrue(saveResult, "Should successfully save chirp with max length text");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertEquals(1, loadedChirps.size(), "Should load chirp");
        assertEquals(280, loadedChirps.get(0).getText().length(), "Text length should be preserved");
        assertEquals(maxLengthText, loadedChirps.get(0).getText(), "Text content should be preserved");
    }
    
    @Test
    public void testSaveAndLoadMultipleHashtags() {
        ArrayList<Chirp> chirps = new ArrayList<>();
        chirps.add(new Chirp("user1", "Multiple #hashtags #in #one #chirp #test"));
        
        boolean saveResult = persistenceService.saveChirps(chirps);
        assertTrue(saveResult, "Should successfully save chirp with multiple hashtags");
        
        ArrayList<Chirp> loadedChirps = persistenceService.loadChirps();
        assertEquals(1, loadedChirps.size(), "Should load chirp");
        
        Chirp loadedChirp = loadedChirps.get(0);
        assertEquals(5, loadedChirp.extractHashtags().size(), "Should preserve all hashtags");
        assertTrue(loadedChirp.containsHashtag("hashtags"), "Should contain hashtags");
        assertTrue(loadedChirp.containsHashtag("test"), "Should contain test hashtag");
    }
    
    @Test
    public void testPersistenceWithComplexUsernames() {
        HashMap<String, Chirper> users = new HashMap<>();
        users.put("user@domain.com", new Chirper("user@domain.com", "pass1"));
        users.put("user_123", new Chirper("user_123", "pass2"));
        users.put("user-with-dashes", new Chirper("user-with-dashes", "pass3"));
        
        boolean saveResult = persistenceService.saveUsers(users);
        assertTrue(saveResult, "Should successfully save users with complex usernames");
        
        HashMap<String, Chirper> loadedUsers = persistenceService.loadUsers();
        assertEquals(3, loadedUsers.size(), "Should load all users");
        assertTrue(loadedUsers.containsKey("user@domain.com"), "Should contain email-like username");
        assertTrue(loadedUsers.containsKey("user_123"), "Should contain username with underscore and numbers");
        assertTrue(loadedUsers.containsKey("user-with-dashes"), "Should contain username with dashes");
    }
    
    @Test
    public void testDataIntegrityAfterMultipleSaveLoad() {
        // Test that data remains consistent after multiple save/load cycles
        HashMap<String, Chirper> originalUsers = new HashMap<>();
        originalUsers.put("user1", new Chirper("user1", "pass1"));
        
        ArrayList<Chirp> originalChirps = new ArrayList<>();
        originalChirps.add(new Chirp("user1", "Test chirp #persistence"));
        
        // First save/load cycle
        persistenceService.saveUsers(originalUsers);
        persistenceService.saveChirps(originalChirps);
        
        HashMap<String, Chirper> loadedUsers1 = persistenceService.loadUsers();
        ArrayList<Chirp> loadedChirps1 = persistenceService.loadChirps();
        
        // Second save/load cycle
        persistenceService.saveUsers(loadedUsers1);
        persistenceService.saveChirps(loadedChirps1);
        
        HashMap<String, Chirper> loadedUsers2 = persistenceService.loadUsers();
        ArrayList<Chirp> loadedChirps2 = persistenceService.loadChirps();
        
        // Data should remain consistent
        assertEquals(originalUsers.size(), loadedUsers2.size(), "User count should remain consistent");
        assertEquals(originalChirps.size(), loadedChirps2.size(), "Chirp count should remain consistent");
        assertEquals("user1", loadedUsers2.get("user1").getUsername(), "Username should remain consistent");
        assertEquals("Test chirp #persistence", loadedChirps2.get(0).getText(), "Chirp text should remain consistent");
    }
}