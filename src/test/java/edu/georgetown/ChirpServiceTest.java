package edu.georgetown;

import edu.georgetown.bll.ChirpService;
import edu.georgetown.bll.user.UserService;
import edu.georgetown.dao.Chirp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Unit tests for the ChirpService BLL class.
 * Tests chirp creation, timeline generation, search functionality, and hashtag operations.
 */
public class ChirpServiceTest {
    
    private ChirpService chirpService;
    private UserService userService;
    private Logger logger;
    
    @BeforeEach
    public void setUp() {
        logger = Logger.getLogger("TestLogger");
        userService = new UserService(logger);
        chirpService = new ChirpService(logger, userService);
        
        // Setup test users
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        userService.registerUser("user3", "pass3");
    }
    
    @Test
    public void testCreateChirp() {
        Chirp chirp = chirpService.createChirp("user1", "Hello world!");
        assertNotNull(chirp, "Should create chirp successfully");
        assertEquals("user1", chirp.getAuthor(), "Author should match");
        assertEquals("Hello world!", chirp.getText(), "Text should match");
        assertNotNull(chirp.getTimestamp(), "Timestamp should be set");
    }
    
    @Test
    public void testCreateChirpWithHashtags() {
        Chirp chirp = chirpService.createChirp("user1", "Hello #world #testing");
        assertNotNull(chirp, "Should create chirp with hashtags");
        List<String> hashtags = chirp.extractHashtags();
        assertEquals(2, hashtags.size(), "Should extract hashtags");
        assertTrue(hashtags.contains("world"), "Should contain 'world' hashtag");
        assertTrue(hashtags.contains("testing"), "Should contain 'testing' hashtag");
    }
    
    @Test
    public void testCreateChirpInvalidAuthor() {
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp(null, "Hello world!");
        }, "Should throw exception for null author");
        
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("", "Hello world!");
        }, "Should throw exception for empty author");
        
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("   ", "Hello world!");
        }, "Should throw exception for whitespace author");
    }
    
    @Test
    public void testCreateChirpInvalidText() {
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("user1", null);
        }, "Should throw exception for null text");
        
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("user1", "");
        }, "Should throw exception for empty text");
        
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("user1", "   ");
        }, "Should throw exception for whitespace text");
    }
    
    @Test
    public void testCreateChirpNonexistentUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("nonexistent", "Hello world!");
        }, "Should throw exception for nonexistent user");
    }
    
    @Test
    public void testCreateChirpTooLong() {
        String longText = "a".repeat(281);
        assertThrows(IllegalArgumentException.class, () -> {
            chirpService.createChirp("user1", longText);
        }, "Should throw exception for chirp too long");
    }
    
    @Test
    public void testCreateChirpMaxLength() {
        String maxText = "a".repeat(280);
        Chirp chirp = chirpService.createChirp("user1", maxText);
        assertNotNull(chirp, "Should create chirp at max length");
        assertEquals(280, chirp.getText().length(), "Text length should be 280");
    }
    
    @Test
    public void testGetChirpsByUser() {
        chirpService.createChirp("user1", "First chirp");
        chirpService.createChirp("user1", "Second chirp");
        chirpService.createChirp("user2", "Other user chirp");
        
        List<Chirp> user1Chirps = chirpService.getChirpsByUser("user1");
        assertEquals(2, user1Chirps.size(), "Should return 2 chirps for user1");
        
        List<Chirp> user2Chirps = chirpService.getChirpsByUser("user2");
        assertEquals(1, user2Chirps.size(), "Should return 1 chirp for user2");
        
        List<Chirp> user3Chirps = chirpService.getChirpsByUser("user3");
        assertTrue(user3Chirps.isEmpty(), "Should return empty list for user with no chirps");
    }
    
    @Test
    public void testGetChirpsByUserNonexistent() {
        List<Chirp> chirps = chirpService.getChirpsByUser("nonexistent");
        assertTrue(chirps.isEmpty(), "Should return empty list for nonexistent user");
    }
    
    @Test
    public void testGetChirpsByUserNull() {
        List<Chirp> chirps = chirpService.getChirpsByUser(null);
        assertTrue(chirps.isEmpty(), "Should return empty list for null username");
    }
    
    @Test
    public void testGetChirpsByHashtag() {
        chirpService.createChirp("user1", "Hello #world");
        chirpService.createChirp("user2", "Testing #world #hashtags");
        chirpService.createChirp("user3", "No hashtags here");
        
        List<Chirp> worldChirps = chirpService.getChirpsByHashtag("world");
        assertEquals(2, worldChirps.size(), "Should find 2 chirps with #world hashtag");
        
        List<Chirp> hashtagsChirps = chirpService.getChirpsByHashtag("hashtags");
        assertEquals(1, hashtagsChirps.size(), "Should find 1 chirp with #hashtags hashtag");
        
        List<Chirp> noSuchChirps = chirpService.getChirpsByHashtag("nonexistent");
        assertTrue(noSuchChirps.isEmpty(), "Should find no chirps with nonexistent hashtag");
    }
    
    @Test
    public void testGetChirpsByHashtagCaseInsensitive() {
        chirpService.createChirp("user1", "Testing #CamelCase hashtags");
        
        List<Chirp> lowerChirps = chirpService.getChirpsByHashtag("camelcase");
        assertEquals(1, lowerChirps.size(), "Should find chirp with lowercase search");
        
        List<Chirp> upperChirps = chirpService.getChirpsByHashtag("CAMELCASE");
        assertEquals(1, upperChirps.size(), "Should find chirp with uppercase search");
        
        List<Chirp> exactChirps = chirpService.getChirpsByHashtag("CamelCase");
        assertEquals(1, exactChirps.size(), "Should find chirp with exact case search");
    }
    
    @Test
    public void testGetChirpsByHashtagNull() {
        List<Chirp> chirps = chirpService.getChirpsByHashtag(null);
        assertTrue(chirps.isEmpty(), "Should return empty list for null hashtag");
    }
    
    @Test
    public void testSearchChirps() {
        chirpService.createChirp("user1", "Hello world");
        chirpService.createChirp("user2", "Testing search functionality");
        chirpService.createChirp("user3", "No match here");
        
        List<Chirp> searchResults = chirpService.searchChirps("world");
        assertEquals(1, searchResults.size(), "Should find chirps containing 'world'");
        
        List<Chirp> searchResults2 = chirpService.searchChirps("test");
        assertEquals(1, searchResults2.size(), "Should find chirps containing 'test'");
        
        List<Chirp> noResults = chirpService.searchChirps("xyz");
        assertTrue(noResults.isEmpty(), "Should find no chirps for non-matching search");
    }
    
    @Test
    public void testSearchChirpsCaseInsensitive() {
        chirpService.createChirp("user1", "Hello WORLD");
        
        List<Chirp> results1 = chirpService.searchChirps("world");
        assertEquals(1, results1.size(), "Should find with lowercase search");
        
        List<Chirp> results2 = chirpService.searchChirps("WORLD");
        assertEquals(1, results2.size(), "Should find with uppercase search");
        
        List<Chirp> results3 = chirpService.searchChirps("World");
        assertEquals(1, results3.size(), "Should find with title case search");
    }
    
    @Test
    public void testSearchChirpsNull() {
        List<Chirp> results = chirpService.searchChirps(null);
        assertTrue(results.isEmpty(), "Should return empty list for null search");
    }
    
    @Test
    public void testGetHomeTimeline() {
        // user1 follows user2 and user3
        userService.followUser("user1", "user2");
        userService.followUser("user1", "user3");
        
        chirpService.createChirp("user1", "My own chirp");
        chirpService.createChirp("user2", "Followed user chirp 1");
        chirpService.createChirp("user3", "Followed user chirp 2");
        
        List<Chirp> timeline = chirpService.getHomeTimeline("user1");
        assertEquals(3, timeline.size(), "Should include own chirps and followed users' chirps");
    }
    
    @Test
    public void testGetHomeTimelineNoFollowing() {
        chirpService.createChirp("user1", "My only chirp");
        chirpService.createChirp("user2", "Someone else's chirp");
        
        List<Chirp> timeline = chirpService.getHomeTimeline("user1");
        assertEquals(1, timeline.size(), "Should only include own chirps when not following anyone");
        assertEquals("My only chirp", timeline.get(0).getText(), "Should be user's own chirp");
    }
    
    @Test
    public void testGetHomeTimelineNonexistentUser() {
        List<Chirp> timeline = chirpService.getHomeTimeline("nonexistent");
        assertTrue(timeline.isEmpty(), "Should return empty timeline for nonexistent user");
    }
    
    @Test
    public void testGetHomeTimelineNull() {
        List<Chirp> timeline = chirpService.getHomeTimeline(null);
        assertTrue(timeline.isEmpty(), "Should return empty timeline for null user");
    }
    
    @Test
    public void testGetChirpCount() {
        assertEquals(0, chirpService.getChirpCount(), "Should start with 0 chirps");
        
        chirpService.createChirp("user1", "First chirp");
        assertEquals(1, chirpService.getChirpCount(), "Should have 1 chirp after creation");
        
        chirpService.createChirp("user2", "Second chirp");
        assertEquals(2, chirpService.getChirpCount(), "Should have 2 chirps after second creation");
    }
    
    @Test
    public void testGetChirpCountByUser() {
        assertEquals(0, chirpService.getChirpCountByUser("user1"), "Should start with 0 chirps for user");
        
        chirpService.createChirp("user1", "First chirp");
        assertEquals(1, chirpService.getChirpCountByUser("user1"), "Should have 1 chirp for user1");
        assertEquals(0, chirpService.getChirpCountByUser("user2"), "Should have 0 chirps for user2");
        
        chirpService.createChirp("user1", "Second chirp");
        assertEquals(2, chirpService.getChirpCountByUser("user1"), "Should have 2 chirps for user1");
    }
    
    @Test
    public void testGetChirpCountByUserNonexistent() {
        assertEquals(0, chirpService.getChirpCountByUser("nonexistent"), "Should return 0 for nonexistent user");
    }
    
    @Test
    public void testGetChirpCountByUserNull() {
        assertEquals(0, chirpService.getChirpCountByUser(null), "Should return 0 for null user");
    }
    
    @Test
    public void testGetPopularHashtags() {
        chirpService.createChirp("user1", "Hello #world #programming");
        chirpService.createChirp("user2", "Testing #world #java");
        chirpService.createChirp("user3", "Learning #programming #coding");
        
        List<String> popularHashtags = chirpService.getPopularHashtags(5);
        assertFalse(popularHashtags.isEmpty(), "Should return popular hashtags");
        // The exact order may vary based on implementation, but should contain the hashtags
        assertTrue(popularHashtags.size() <= 5, "Should not exceed requested limit");
    }
    
    @Test
    public void testGetPopularHashtagsEmpty() {
        List<String> popularHashtags = chirpService.getPopularHashtags(5);
        assertTrue(popularHashtags.isEmpty(), "Should return empty list when no chirps exist");
    }
    
    @Test
    public void testChirpTextTrimming() {
        Chirp chirp = chirpService.createChirp("user1", "   Hello world   ");
        assertEquals("Hello world", chirp.getText(), "Should trim whitespace from chirp text");
    }
    
    @Test
    public void testMultipleChirpsOrdering() {
        chirpService.createChirp("user1", "First chirp");
        // Small delay to ensure different timestamps
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        chirpService.createChirp("user1", "Second chirp");
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        chirpService.createChirp("user1", "Third chirp");
        
        List<Chirp> chirps = chirpService.getChirpsByUser("user1");
        assertEquals(3, chirps.size(), "Should have 3 chirps");
        // Most implementations show newest first
        assertTrue(chirps.get(0).getTimestamp().getTime() >= chirps.get(1).getTimestamp().getTime(),
                  "Chirps should be ordered by timestamp");
    }
}