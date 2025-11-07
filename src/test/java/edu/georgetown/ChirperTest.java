package edu.georgetown;

import edu.georgetown.dao.Chirper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Vector;

/**
 * Unit tests for the Chirper DAO class.
 * Tests user creation, authentication, and social relationship functionality.
 */
public class ChirperTest {
    
    private Chirper chirper;
    private Chirper follower;
    private Chirper followee;
    
    @BeforeEach
    public void setUp() {
        chirper = new Chirper("testuser", "password123");
        follower = new Chirper("follower", "pass123");
        followee = new Chirper("followee", "pass456");
    }
    
    @Test
    public void testChirperCreation() {
        assertEquals("testuser", chirper.getUsername(), "Username should match");
        assertEquals("password123", chirper.getPassword(), "Password should match");
        assertTrue(chirper.isPublic(), "Chirps should be public by default");
        assertNotNull(chirper.getFollowers(), "Followers list should be initialized");
        assertNotNull(chirper.getFollowing(), "Following list should be initialized");
        assertTrue(chirper.getFollowers().isEmpty(), "Followers list should be empty initially");
        assertTrue(chirper.getFollowing().isEmpty(), "Following list should be empty initially");
    }
    
    @Test
    public void testChirperCreationWithPrivacy() {
        Chirper privateChirper = new Chirper("private", "pass", false);
        assertEquals("private", privateChirper.getUsername(), "Username should match");
        assertEquals("pass", privateChirper.getPassword(), "Password should match");
        assertFalse(privateChirper.isPublic(), "Chirps should be private");
        
        Chirper publicChirper = new Chirper("public", "pass", true);
        assertTrue(publicChirper.isPublic(), "Chirps should be public");
    }
    
    @Test
    public void testPasswordAuthentication() {
        assertTrue(chirper.checkPassword("password123"), "Should authenticate with correct password");
        assertFalse(chirper.checkPassword("wrongpassword"), "Should not authenticate with wrong password");
        assertFalse(chirper.checkPassword(""), "Should not authenticate with empty password");
        assertFalse(chirper.checkPassword(null), "Should not authenticate with null password");
    }
    
    @Test
    public void testPasswordCaseSensitivity() {
        assertFalse(chirper.checkPassword("PASSWORD123"), "Password should be case sensitive");
        assertFalse(chirper.checkPassword("Password123"), "Password should be case sensitive");
    }
    
    @Test
    public void testFollowUser() {
        boolean result = chirper.followUser(followee);
        assertTrue(result, "Should successfully follow a user");
        assertTrue(chirper.getFollowing().contains(followee), "Followee should be in following list");
        assertTrue(followee.getFollowers().contains(chirper), "Chirper should be in followee's followers list");
        assertEquals(1, chirper.getFollowing().size(), "Should have 1 following");
        assertEquals(1, followee.getFollowers().size(), "Followee should have 1 follower");
    }
    
    @Test
    public void testFollowUserTwice() {
        chirper.followUser(followee);
        boolean result = chirper.followUser(followee);
        assertFalse(result, "Should not be able to follow the same user twice");
        assertEquals(1, chirper.getFollowing().size(), "Should still have only 1 following");
        assertEquals(1, followee.getFollowers().size(), "Followee should still have only 1 follower");
    }
    
    @Test
    public void testFollowSelf() {
        boolean result = chirper.followUser(chirper);
        assertFalse(result, "Should not be able to follow yourself");
        assertTrue(chirper.getFollowing().isEmpty(), "Following list should remain empty");
        assertTrue(chirper.getFollowers().isEmpty(), "Followers list should remain empty");
    }
    
    @Test
    public void testFollowNullUser() {
        boolean result = chirper.followUser(null);
        assertFalse(result, "Should not be able to follow null user");
        assertTrue(chirper.getFollowing().isEmpty(), "Following list should remain empty");
    }
    
    @Test
    public void testUnfollowUser() {
        chirper.followUser(followee);
        boolean result = chirper.unfollowUser(followee);
        assertTrue(result, "Should successfully unfollow a user");
        assertFalse(chirper.getFollowing().contains(followee), "Followee should not be in following list");
        assertFalse(followee.getFollowers().contains(chirper), "Chirper should not be in followee's followers list");
        assertTrue(chirper.getFollowing().isEmpty(), "Following list should be empty");
        assertTrue(followee.getFollowers().isEmpty(), "Followee's followers list should be empty");
    }
    
    @Test
    public void testUnfollowUserNotFollowing() {
        boolean result = chirper.unfollowUser(followee);
        assertFalse(result, "Should not be able to unfollow a user you're not following");
        assertTrue(chirper.getFollowing().isEmpty(), "Following list should remain empty");
    }
    
    @Test
    public void testUnfollowNullUser() {
        boolean result = chirper.unfollowUser(null);
        assertFalse(result, "Should not be able to unfollow null user");
    }
    
    @Test
    public void testMultipleFollowers() {
        Chirper follower2 = new Chirper("follower2", "pass");
        Chirper follower3 = new Chirper("follower3", "pass");
        
        follower.followUser(chirper);
        follower2.followUser(chirper);
        follower3.followUser(chirper);
        
        assertEquals(3, chirper.getFollowers().size(), "Should have 3 followers");
        assertTrue(chirper.getFollowers().contains(follower), "Should contain follower");
        assertTrue(chirper.getFollowers().contains(follower2), "Should contain follower2");
        assertTrue(chirper.getFollowers().contains(follower3), "Should contain follower3");
    }
    
    @Test
    public void testMultipleFollowing() {
        Chirper followee2 = new Chirper("followee2", "pass");
        Chirper followee3 = new Chirper("followee3", "pass");
        
        chirper.followUser(followee);
        chirper.followUser(followee2);
        chirper.followUser(followee3);
        
        assertEquals(3, chirper.getFollowing().size(), "Should be following 3 users");
        assertTrue(chirper.getFollowing().contains(followee), "Should be following followee");
        assertTrue(chirper.getFollowing().contains(followee2), "Should be following followee2");
        assertTrue(chirper.getFollowing().contains(followee3), "Should be following followee3");
    }
    
    @Test
    public void testMutualFollowing() {
        chirper.followUser(followee);
        followee.followUser(chirper);
        
        assertTrue(chirper.getFollowing().contains(followee), "Chirper should be following followee");
        assertTrue(followee.getFollowing().contains(chirper), "Followee should be following chirper");
        assertTrue(chirper.getFollowers().contains(followee), "Chirper should have followee as follower");
        assertTrue(followee.getFollowers().contains(chirper), "Followee should have chirper as follower");
    }
    
    @Test
    public void testAddFollowerDirectly() {
        chirper.addFollower(follower);
        assertTrue(chirper.getFollowers().contains(follower), "Follower should be added to followers list");
        assertEquals(1, chirper.getFollowers().size(), "Should have 1 follower");
    }
    
    @Test
    public void testAddFollowerNull() {
        chirper.addFollower(null);
        assertTrue(chirper.getFollowers().isEmpty(), "Should not add null follower");
    }
    
    @Test
    public void testGetFollowersReturnsVector() {
        Vector<Chirper> followers = chirper.getFollowers();
        assertNotNull(followers, "Should return a vector");
        assertTrue(followers instanceof Vector, "Should be a Vector instance");
    }
    
    @Test
    public void testGetFollowingReturnsVector() {
        Vector<Chirper> following = chirper.getFollowing();
        assertNotNull(following, "Should return a vector");
        assertTrue(following instanceof Vector, "Should be a Vector instance");
    }
    
    @Test
    public void testEmptyUsername() {
        // Testing edge cases for username - the constructor should handle these appropriately
        Chirper emptyUser = new Chirper("", "password");
        assertEquals("", emptyUser.getUsername(), "Should store empty username");
    }
    
    @Test
    public void testLongUsername() {
        String longUsername = "a".repeat(100);
        Chirper longUser = new Chirper(longUsername, "password");
        assertEquals(longUsername, longUser.getUsername(), "Should store long username");
    }
    
    @Test
    public void testSpecialCharactersInUsername() {
        String specialUsername = "user@domain.com_123";
        Chirper specialUser = new Chirper(specialUsername, "password");
        assertEquals(specialUsername, specialUser.getUsername(), "Should store username with special characters");
    }
}