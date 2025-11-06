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


    public Chirper( String username, String password ) {
        this.username = username;
        this.password = password;
        this.publicChirps = true;
        this.followers = new Vector<Chirper>();        
    }

    //overloaded constrcutor to choose public status on creation
    public Chirper(String username, String password, boolean isPublic) {
    this.username = username;
    this.password = password;
    this.publicChirps = isPublic;
    this.followers = new Vector<>();
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

    public boolean isPublic() {
        return this.publicChirps;
    }

    public void setPublic(boolean publicChirps) {
        this.publicChirps = publicChirps;
    }
}