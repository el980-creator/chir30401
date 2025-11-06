/**
 * A skeleton of a Chirper
 * 
 * Micah Sherr <msherr@cs.georgetown.edu>
 */

package edu.georgetown.dao;

import java.io.Serializable;
import java.util.Vector;

public class Chirper implements Serializable {
    
    private String username;
    private String password;
    /** if true, the user's chirps are public */
    private boolean publicChirps;   

    /** list of this chirper's followers */
    private Vector<Chirper> followers;

    /** list of who this chirper is following */
    private Vector<Chirper> following;


    public Chirper( String username, String password ) {
        this.username = username;
        this.password = password;
        this.publicChirps = true;
        this.followers = new Vector<Chirper>();  
        this.following = new Vector<Chirper>();      
    }

    //overloaded constrcutor to choose public status on creation
    public Chirper(String username, String password, boolean isPublic) {
    this.username = username;
    this.password = password;
    this.publicChirps = isPublic;
    this.followers = new Vector<>();
    this.following = new Vector<Chirper>();;  
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

    public boolean removeFollower(Chirper follower) {
        return this.followers.remove(follower);
    }

    public Vector<Chirper> getFollowers() {
        return this.followers;
    }

    //for following people / for timeline purposes
    public Vector<Chirper> getFollowing() {
        return this.following;
    }

    public void addFollowing(Chirper chirper) {
        if (chirper == null || chirper == this) {
            return;
        }

        if (!following.contains(chirper)) {
            following.add(chirper);
        }
        }

    public boolean removeFollowing(Chirper chirper) {
        return this.following.remove(chirper);
    }

    public boolean isFollowing(Chirper chirper) {
        return this.following.contains(chirper);
    }

    public boolean isPublic() {
        return this.publicChirps;
    }

    public void setPublic(boolean publicChirps) {
        this.publicChirps = publicChirps;
    }
}