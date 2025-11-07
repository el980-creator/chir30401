package edu.georgetown;

import edu.georgetown.bll.user.UserService;
import edu.georgetown.dao.Chirper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Unit tests for the UserService BLL class.
 * Tests user registration, authentication, session management, and social features.
 */
public class UserServiceTest {
    
    private UserService userService;
    private Logger logger;
    
    @BeforeEach
    public void setUp() {
        logger = Logger.getLogger("TestLogger");
        userService = new UserService(logger);
    }
    
    @Test
    public void testRegisterUser() {
        boolean result = userService.registerUser("testuser", "password123");
        assertTrue(result, "Should successfully register new user");
        
        Chirper user = userService.getUserByUsername("testuser");
        assertNotNull(user, "Registered user should exist");
        assertEquals("testuser", user.getUsername(), "Username should match");
    }
    
    @Test
    public void testRegisterDuplicateUser() {
        userService.registerUser("duplicate", "pass1");
        boolean result = userService.registerUser("duplicate", "pass2");
        assertFalse(result, "Should not register duplicate username");
    }
    
    @Test
    public void testRegisterNullUsername() {
        boolean result = userService.registerUser(null, "password");
        assertFalse(result, "Should not register user with null username");
    }
    
    @Test
    public void testRegisterEmptyUsername() {
        boolean result = userService.registerUser("", "password");
        assertFalse(result, "Should not register user with empty username");
    }
    
    @Test
    public void testRegisterNullPassword() {
        boolean result = userService.registerUser("testuser", null);
        assertFalse(result, "Should not register user with null password");
    }
    
    @Test
    public void testRegisterEmptyPassword() {
        boolean result = userService.registerUser("testuser", "");
        assertFalse(result, "Should not register user with empty password");
    }
    
    @Test
    public void testLoginUser() {
        userService.registerUser("loginuser", "password123");
        boolean result = userService.loginUser("loginuser", "password123");
        assertTrue(result, "Should successfully login with correct credentials");
    }
    
    @Test
    public void testLoginUserWrongPassword() {
        userService.registerUser("loginuser", "password123");
        boolean result = userService.loginUser("loginuser", "wrongpassword");
        assertFalse(result, "Should not login with wrong password");
    }
    
    @Test
    public void testLoginNonexistentUser() {
        boolean result = userService.loginUser("nonexistent", "password");
        assertFalse(result, "Should not login nonexistent user");
    }
    
    @Test
    public void testLoginNullCredentials() {
        assertFalse(userService.loginUser(null, "password"), "Should not login with null username");
        assertFalse(userService.loginUser("user", null), "Should not login with null password");
        assertFalse(userService.loginUser(null, null), "Should not login with null credentials");
    }
    
    @Test
    public void testAuthenticateAndCreateSession() {
        userService.registerUser("sessionuser", "password123");
        String token = userService.authenticateAndCreateSession("sessionuser", "password123");
        assertNotNull(token, "Should return session token for valid credentials");
        assertFalse(token.isEmpty(), "Session token should not be empty");
    }
    
    @Test
    public void testAuthenticateAndCreateSessionInvalidCredentials() {
        userService.registerUser("sessionuser", "password123");
        String token = userService.authenticateAndCreateSession("sessionuser", "wrongpassword");
        assertNull(token, "Should return null for invalid credentials");
    }
    
    @Test
    public void testGetUsernameForSession() {
        userService.registerUser("sessionuser", "password123");
        String token = userService.authenticateAndCreateSession("sessionuser", "password123");
        String username = userService.getUsernameForSession(token);
        assertEquals("sessionuser", username, "Should return correct username for valid token");
    }
    
    @Test
    public void testGetUsernameForInvalidSession() {
        String username = userService.getUsernameForSession("invalidtoken");
        assertNull(username, "Should return null for invalid token");
        
        String nullUsername = userService.getUsernameForSession(null);
        assertNull(nullUsername, "Should return null for null token");
    }
    
    @Test
    public void testRemoveSession() {
        userService.registerUser("sessionuser", "password123");
        String token = userService.authenticateAndCreateSession("sessionuser", "password123");
        
        boolean result = userService.removeSession(token);
        assertTrue(result, "Should successfully remove session");
        
        String username = userService.getUsernameForSession(token);
        assertNull(username, "Token should be invalid after removal");
    }
    
    @Test
    public void testRemoveInvalidSession() {
        boolean result = userService.removeSession("invalidtoken");
        assertFalse(result, "Should not remove invalid session");
        
        boolean nullResult = userService.removeSession(null);
        assertFalse(nullResult, "Should not remove null session");
    }
    
    @Test
    public void testFollowUser() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        
        boolean result = userService.followUser("user1", "user2");
        assertTrue(result, "Should successfully follow user");
    }
    
    @Test
    public void testFollowNonexistentUser() {
        userService.registerUser("user1", "pass1");
        
        boolean result = userService.followUser("user1", "nonexistent");
        assertFalse(result, "Should not follow nonexistent user");
        
        boolean result2 = userService.followUser("nonexistent", "user1");
        assertFalse(result2, "Nonexistent user should not follow anyone");
    }
    
    @Test
    public void testFollowSelf() {
        userService.registerUser("user1", "pass1");
        
        boolean result = userService.followUser("user1", "user1");
        assertFalse(result, "User should not be able to follow themselves");
    }
    
    @Test
    public void testFollowUserNullParams() {
        assertFalse(userService.followUser(null, "user1"), "Should not follow with null follower");
        assertFalse(userService.followUser("user1", null), "Should not follow with null followee");
        assertFalse(userService.followUser(null, null), "Should not follow with null parameters");
    }
    
    @Test
    public void testUnfollowUser() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        userService.followUser("user1", "user2");
        
        boolean result = userService.unfollowUser("user1", "user2");
        assertTrue(result, "Should successfully unfollow user");
    }
    
    @Test
    public void testUnfollowUserNotFollowing() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        
        boolean result = userService.unfollowUser("user1", "user2");
        assertFalse(result, "Should not unfollow user that's not being followed");
    }
    
    @Test
    public void testUnfollowUserNullParams() {
        assertFalse(userService.unfollowUser(null, "user1"), "Should not unfollow with null follower");
        assertFalse(userService.unfollowUser("user1", null), "Should not unfollow with null followee");
        assertFalse(userService.unfollowUser(null, null), "Should not unfollow with null parameters");
    }
    
    @Test
    public void testIsUserFollowing() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        
        assertFalse(userService.isUserFollowing("user1", "user2"), "Should not be following initially");
        
        userService.followUser("user1", "user2");
        assertTrue(userService.isUserFollowing("user1", "user2"), "Should be following after follow");
        
        userService.unfollowUser("user1", "user2");
        assertFalse(userService.isUserFollowing("user1", "user2"), "Should not be following after unfollow");
    }
    
    @Test
    public void testIsUserFollowingNullParams() {
        assertFalse(userService.isUserFollowing(null, "user1"), "Should return false for null follower");
        assertFalse(userService.isUserFollowing("user1", null), "Should return false for null followee");
        assertFalse(userService.isUserFollowing(null, null), "Should return false for null parameters");
    }
    
    @Test
    public void testGetUserByUsername() {
        userService.registerUser("getuser", "password");
        
        Chirper user = userService.getUserByUsername("getuser");
        assertNotNull(user, "Should return user object");
        assertEquals("getuser", user.getUsername(), "Username should match");
    }
    
    @Test
    public void testGetUserByUsernameNonexistent() {
        Chirper user = userService.getUserByUsername("nonexistent");
        assertNull(user, "Should return null for nonexistent user");
    }
    
    @Test
    public void testGetUserByUsernameNull() {
        Chirper user = userService.getUserByUsername(null);
        assertNull(user, "Should return null for null username");
    }
    
    @Test
    public void testGetAllUsernames() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        userService.registerUser("user3", "pass3");
        
        Vector<String> usernames = userService.getAllUsernames();
        assertEquals(3, usernames.size(), "Should return all usernames");
        assertTrue(usernames.contains("user1"), "Should contain user1");
        assertTrue(usernames.contains("user2"), "Should contain user2");
        assertTrue(usernames.contains("user3"), "Should contain user3");
    }
    
    @Test
    public void testGetAllUsernamesEmpty() {
        Vector<String> usernames = userService.getAllUsernames();
        assertTrue(usernames.isEmpty(), "Should return empty vector when no users");
    }
    
    @Test
    public void testGetFollowersUsernames() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("follower1", "pass2");
        userService.registerUser("follower2", "pass3");
        
        userService.followUser("follower1", "user1");
        userService.followUser("follower2", "user1");
        
        Vector<String> followers = userService.getFollowers("user1");
        assertEquals(2, followers.size(), "Should have 2 followers");
        assertTrue(followers.contains("follower1"), "Should contain follower1");
        assertTrue(followers.contains("follower2"), "Should contain follower2");
    }
    
    @Test
    public void testGetFollowersUsernamesNonexistentUser() {
        Vector<String> followers = userService.getFollowers("nonexistent");
        assertTrue(followers.isEmpty(), "Should return empty vector for nonexistent user");
    }
    
    @Test
    public void testMultipleSessionTokens() {
        userService.registerUser("user1", "pass1");
        userService.registerUser("user2", "pass2");
        
        String token1 = userService.authenticateAndCreateSession("user1", "pass1");
        String token2 = userService.authenticateAndCreateSession("user2", "pass2");
        
        assertNotEquals(token1, token2, "Different users should have different tokens");
        assertEquals("user1", userService.getUsernameForSession(token1), "Token1 should map to user1");
        assertEquals("user2", userService.getUsernameForSession(token2), "Token2 should map to user2");
    }
    
    @Test
    public void testSessionTokenUniqueness() {
        userService.registerUser("user1", "pass1");
        
        String token1 = userService.authenticateAndCreateSession("user1", "pass1");
        userService.removeSession(token1);
        String token2 = userService.authenticateAndCreateSession("user1", "pass1");
        
        assertNotEquals(token1, token2, "New login should generate different token");
    }
}