package edu.georgetown;

import edu.georgetown.dao.Chirp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for the Chirp DAO class.
 * Tests hashtag extraction, validation, and core functionality.
 */
public class ChirpTest {
    
    private Chirp chirp;
    private Date testDate;
    
    @BeforeEach
    public void setUp() {
        testDate = new Date();
        chirp = new Chirp("testuser", "Hello world! #testing #junit", testDate);
    }
    
    @Test
    public void testChirpCreation() {
        assertEquals("testuser", chirp.getAuthor(), "Author should match");
        assertEquals("Hello world! #testing #junit", chirp.getText(), "Text should match");
        assertEquals(testDate, chirp.getTimestamp(), "Timestamp should match");
    }
    
    @Test
    public void testChirpCreationWithCurrentTime() {
        Chirp currentChirp = new Chirp("user1", "Current time chirp");
        assertNotNull(currentChirp.getTimestamp(), "Timestamp should be set automatically");
        assertEquals("user1", currentChirp.getAuthor(), "Author should be set correctly");
        assertEquals("Current time chirp", currentChirp.getText(), "Text should be set correctly");
    }
    
    @Test
    public void testInvalidChirpCreation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Chirp(null, "Valid text");
        }, "Should throw exception for null author");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Chirp("", "Valid text");
        }, "Should throw exception for empty author");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Chirp("validuser", null);
        }, "Should throw exception for null text");
    }
    
    @Test
    public void testMaxLengthChirp() {
        String maxLengthText = "a".repeat(280);
        Chirp maxChirp = new Chirp("user", maxLengthText);
        assertEquals(280, maxChirp.getText().length(), "Should accept 280 character chirp");
    }
    
    @Test
    public void testTooLongChirp() {
        String tooLongText = "a".repeat(281);
        assertThrows(IllegalArgumentException.class, () -> {
            new Chirp("user", tooLongText);
        }, "Should reject chirps longer than 280 characters");
    }
    
    @Test
    public void testHashtagExtraction() {
        List<String> hashtags = chirp.extractHashtags();
        assertEquals(2, hashtags.size(), "Should find 2 hashtags");
        assertTrue(hashtags.contains("testing"), "Should contain 'testing' hashtag");
        assertTrue(hashtags.contains("junit"), "Should contain 'junit' hashtag");
    }
    
    @Test
    public void testHashtagExtractionNoHashtags() {
        Chirp noHashtagChirp = new Chirp("user", "No hashtags here");
        List<String> hashtags = noHashtagChirp.extractHashtags();
        assertTrue(hashtags.isEmpty(), "Should find no hashtags");
    }
    
    @Test
    public void testHashtagExtractionMultiple() {
        Chirp multiHashtagChirp = new Chirp("user", "#first #second #third word #fourth");
        List<String> hashtags = multiHashtagChirp.extractHashtags();
        assertEquals(4, hashtags.size(), "Should find 4 hashtags");
        assertTrue(hashtags.contains("first"), "Should contain 'first'");
        assertTrue(hashtags.contains("second"), "Should contain 'second'");
        assertTrue(hashtags.contains("third"), "Should contain 'third'");
        assertTrue(hashtags.contains("fourth"), "Should contain 'fourth'");
    }
    
    @Test
    public void testHashtagExtractionCaseAndSpecialChars() {
        Chirp specialChirp = new Chirp("user", "#CamelCase #with123numbers #under_score");
        List<String> hashtags = specialChirp.extractHashtags();
        assertEquals(3, hashtags.size(), "Should find 3 hashtags with special characters");
        assertTrue(hashtags.contains("CamelCase"), "Should handle camel case");
        assertTrue(hashtags.contains("with123numbers"), "Should handle numbers");
        assertTrue(hashtags.contains("under_score"), "Should handle underscores");
    }
    
    @Test
    public void testContainsHashtag() {
        assertTrue(chirp.containsHashtag("testing"), "Should find existing hashtag");
        assertTrue(chirp.containsHashtag("junit"), "Should find existing hashtag");
        assertFalse(chirp.containsHashtag("nonexistent"), "Should not find non-existent hashtag");
        assertFalse(chirp.containsHashtag("test"), "Should not match partial hashtag");
    }
    
    @Test
    public void testContainsHashtagCaseInsensitive() {
        Chirp caseChirp = new Chirp("user", "Testing #CamelCase hashtags");
        assertTrue(caseChirp.containsHashtag("CamelCase"), "Should find exact case");
        assertTrue(caseChirp.containsHashtag("camelcase"), "Should find lowercase");
        assertTrue(caseChirp.containsHashtag("CAMELCASE"), "Should find uppercase");
    }
    
    @Test
    public void testEmptyText() {
        Chirp emptyChirp = new Chirp("user", "");
        assertEquals("", emptyChirp.getText(), "Should handle empty text");
        assertTrue(emptyChirp.extractHashtags().isEmpty(), "Empty text should have no hashtags");
    }
    
    @Test
    public void testWhitespaceText() {
        Chirp whitespaceChirp = new Chirp("user", "   ");
        assertEquals("   ", whitespaceChirp.getText(), "Should preserve whitespace");
        assertTrue(whitespaceChirp.extractHashtags().isEmpty(), "Whitespace should have no hashtags");
    }
    
    @Test
    public void testChirpWithOnlyHashtags() {
        Chirp hashtagOnlyChirp = new Chirp("user", "#first #second #third");
        List<String> hashtags = hashtagOnlyChirp.extractHashtags();
        assertEquals(3, hashtags.size(), "Should find all hashtags");
    }
    
    @Test
    public void testChirpTextImmutability() {
        String originalText = chirp.getText();
        String retrievedText = chirp.getText();
        assertEquals(originalText, retrievedText, "Text should remain unchanged");
    }
}