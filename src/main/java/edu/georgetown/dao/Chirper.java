/**
 * Chirper - represents a user in the Chirpy social networking system
 * 
 * This class encapsulates user data including authentication credentials,
 * privacy settings, and social relationships (followers and following).
 * Supports user authentication, following relationships, and profile management.
 * 
 * @author Chirpy Team
 * @version 2.0
 * @since 1.0
 */

package edu.georgetown.dao;

import java.io.Serializable;
import java.util.Vector;

/**
 * Data Access Object representing a user (chirper) in the social network.
 * 
 * <p>This class manages:
 * <ul>
 * <li>User authentication (username/password)</li>
 * <li>Privacy settings for chirp visibility</li>
 * <li>Social relationships (followers and following lists)</li>
 * <li>User profile operations</li>
 * </ul>
 * 
 * <p>The class implements {@link Serializable} to support persistence
 * and session management capabilities.
 * 
 * @author Chirpy Team
 * @version 2.0
 */
public class Chirper implements Serializable {
    
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;
    
    /** The unique username for this chirper */
    private String username;
    
    /** The hashed password for authentication */
    private String password;
    
    /** If true, the user's chirps are publicly visible */
    private boolean publicChirps;   

    /** List of users who follow this chirper */
    private Vector<Chirper> followers;

    /** List of users this chirper is following */
    private Vector<Chirper> following;


    /**
     * Creates a new chirper with default public visibility.
     * 
     * <p>Initializes the user with the provided credentials and sets
     * chirp visibility to public by default. Creates empty followers
     * and following lists.
     * 
     * @param username the unique username for this chirper
     * @param password the password for authentication
     * @throws IllegalArgumentException if username or password is null/empty
     */
    public Chirper( String username, String password ) {
        this.username = username;
        this.password = password;
        this.publicChirps = true;
        this.followers = new Vector<Chirper>();
        this.following = new Vector<Chirper>();        
    }

    /**
     * Creates a new chirper with specified privacy settings.
     * 
     * <p>Overloaded constructor to set public/private status on creation.
     * Allows full control over user privacy settings during initialization.
     * 
     * @param username the unique username for this chirper
     * @param password the password for authentication
     * @param isPublic true if chirps should be public, false for private
     * @throws IllegalArgumentException if username or password is null/empty
     */
    public Chirper(String username, String password, boolean isPublic) {
        this.username = username;
        this.password = password;
        this.publicChirps = isPublic;
        this.followers = new Vector<>();
        this.following = new Vector<>();
    }


    /**
     * Gets the user's username
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean checkPassword( String password ) {
        return this.password.equals( password );
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addFollower( Chirper follower ) {
        if (follower == null || follower == this)
        {
            return;
        }
        if (!followers.contains(follower)) {
            followers.add(follower);
        }
        return;
    }

    public Vector<Chirper> getFollowers() {
        return this.followers;
    }

    /**
     * Gets the list of users this chirper is following.
     * @return vector of chirpers this user follows
     */
    public Vector<Chirper> getFollowing() {
        return this.following;
    }

    /**
     * Adds a user to this chirper's following list.
     * @param userToFollow the chirper to follow
     * @return true if successfully added, false if already following or invalid
     */
    public boolean followUser(Chirper userToFollow) {
        if (userToFollow == null || userToFollow == this) {
            return false;
        }
        if (!following.contains(userToFollow)) {
            following.add(userToFollow);
            // Add this user to the other user's followers list
            userToFollow.addFollower(this);
            return true;
        }
        return false;
    }

    /**
     * Removes a user from this chirper's following list.
     * @param userToUnfollow the chirper to unfollow
     * @return true if successfully removed, false if not following
     */
    public boolean unfollowUser(Chirper userToUnfollow) {
        if (userToUnfollow == null) {
            return false;
        }
        boolean removed = following.remove(userToUnfollow);
        if (removed) {
            // Remove this user from the other user's followers list
            userToUnfollow.removeFollower(this);
        }
        return removed;
    }

    /**
     * Checks if this chirper is following a specific user.
     * @param user the user to check
     * @return true if following, false otherwise
     */
    public boolean isFollowing(Chirper user) {
        return user != null && following.contains(user);
    }

    /**
     * Checks if this chirper is followed by a specific user.
     * @param user the user to check
     * @return true if followed by the user, false otherwise
     */
    public boolean isFollowedBy(Chirper user) {
        return user != null && followers.contains(user);
    }

    /**
     * Gets the count of users this chirper is following.
     * @return number of users being followed
     */
    public int getFollowingCount() {
        return following.size();
    }

    /**
     * Gets the count of followers this chirper has.
     * @return number of followers
     */
    public int getFollowersCount() {
        return followers.size();
    }

    /**
     * Removes a follower from this chirper's followers list.
     * @param follower the follower to remove
     * @return true if successfully removed, false otherwise
     */
    private boolean removeFollower(Chirper follower) {
        return followers.remove(follower);
    }

    public boolean isPublic() {
        return this.publicChirps;
    }

    public void setPublic(boolean publicChirps) {
        this.publicChirps = publicChirps;
    }
}