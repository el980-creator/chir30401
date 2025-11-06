# COSC 2020 Project 1 - Chirpy 1.0 Design Document

**Names: Estheffy De Jesus, Alex Freund, Brian Lee, Fahad Shahbaz**

This document serves as the high-level system design for Chirpy 1.0 and outlines the foundation and future plans for Chirpy 2.0.

---

## Contents
- [Overview and Goals](#1-overview-and-goals)
- [Core Functionalities](#2-core-functionalities)
- [Data Access Objects (DAO)](#3-data-access-objects-dao)
- [Business Logic Layer (BLL)](#4-business-logic-layer-bll)
- [Display Logic (DL)](#5-display-logic-dl)
- [Data Storage and Persistence](#6-data-storage-and-persistence)
- [Storage for Relationships](#7-storage-for-relationships)
- [Authentication and Cookies: Extra Feature](#8-authentication-and-cookies-extra-feature)
- [How the Layers Work Together](#9-how-the-layers-work-together)
- [Testing Plan](#10-testing-plan)
- [Testing Plan Specificity](#11-testing-plan-specificity)
- [Future Work (Chirpy 2.0)](#12-future-work-chirpy-20)
- [Summary](#13-summary)


---

## 1. Overview and Goals

Chirpy is a Twitter-like web service where users (chirpers) can register, log in, and post short messages (chirps) with #hashtags. Chirpy 1.0 implements registration, login, and a user list page, while Chirpy 2.0 will add timeline, chirp posting, and search functionality.

The main goals of the project are:

- Build a modular system with three layers: DAO (data), BLL (logic), and DL (display).
- Implement registration, login, and listing users for Chirpy 1.0.
- Use secure authentication (session tokens instead of plain usernames in cookies).
- Implement persistent-state storage as the special feature.
- Plan ahead for Chirpy 2.0’s advanced features: posting, timelines, and search.

---

## 2. Core Functionalities

Chirpy provides several key user-facing functions.

- For Chirpy 1.0, users can register for an account, log in, and view all registered users.  
- For Chirpy 2.0, additional capabilities will include chirp posting, timelines, searching, and following other users.

**Register (Chirpy 1.0):** Users can create an account with a unique username and password. Passwords are securely stored using hashing.  

**Login and Logout (Chirpy 1.0 & 2.0):** Registered users can log in, stay authenticated via cookies, and log out. Authentication uses a secure session token system.  

**List All Users (Chirpy 1.0):** A debug or testing page (/listusers/) displays all registered usernames.  

**Post Chirp (Chirpy 2.0):** Logged-in users can post short messages (chirps) with optional #hashtags.  

**Timelines (Chirpy 2.0):** Displays chirps as a home timeline (from followed users), user timeline (from one user), and hashtag timeline (all chirps using a hashtag).  

**Contact Management (Chirpy 2.0):** Users can follow and unfollow other users to control their timeline content.  

**Persistent-State Backend (Special Feature):** User and chirp data are saved to disk (using users.json and chirps.json) and restored automatically after server restart.

---

## 3. Data Access Objects (DAO)

The DAO layer defines the main data structures used to store users and chirps. These are plain Java objects (POJOs).

### User.java

Represents a registered user.

- `User(String username, String passwordHash)` creates a new user.  
- `getUsername()` returns the username.  
- `getPasswordHash()` returns the hashed password.  
- `checkPassword(String password)` verifies whether a plain-text password matches the stored hash.  
- `addContact(String username)` adds another user to the current user’s contact list.  
- `removeContact(String username)` removes a contact.  
- `getContacts()` returns a list of all usernames this user follows.  

### Chirp.java

Represents a single chirp (post).

**Constructor:**  
`public Chirp(String author, String text, Date timestamp)` — Creates a new chirp with an author, text, and timestamp.

**Fields:**

- `private String author` — Username of the chirp creator.  
- `private String text` — Content of the chirp (max 280 characters).  
- `private Date timestamp` — When the chirp was posted.  

**Methods:**

- `public String getAuthor()` — Returns the username of the chirp’s author.  
- `public String getText()` — Returns the chirp content.  
- `public Date getTimestamp()` — Returns when the chirp was created.  
- `public List<String> extractHashtags()` — Extracts all hashtags from the chirp text.  
- `public boolean containsHashtag(String hashtag)` — Checks if the chirp contains a specific hashtag.

---

## 4. Business Logic Layer (BLL)

The BLL layer is the “brains” of the application. It handles validation, authentication, and data operations by using the DAO objects.

### UserService.java

Handles all business logic related to user management, including registration, authentication, and managing social connections (contacts).

**Methods:**

- `public boolean registerUser(String username, String password)` — Creates and stores a new user if the username is unique.  
- `public boolean loginUser(String username, String password)` — Verifies a user’s credentials.  
- `public Chirper getUserByUsername(String username)` — Retrieves a user’s data from storage.  
- `public boolean userExists(String username)` — Checks if a username is already registered.  
- `public boolean followUser(String followerUsername, String followeeUsername)` — Adds one user to another’s contact list.  
- `public Vector<Chirper> getUsers()` — Returns all registered users.  
- `public Vector<String> getAllUsernames()` — Returns all usernames for display (/listusers/).

### ChirpService.java

Manages the logic for creating and retrieving chirps, timeline generation, and hashtag searches.

**Methods:**

- `public Chirp createChirp(String authorUsername, String text)` — Creates a new chirp and saves it to the data store.  
- `public Vector<Chirp> getHomeTimeline(String username)` — Retrieves chirps from all users followed by the given username.  
- `public Vector<Chirp> getChirpsByUser(String username)` — Returns all chirps posted by a specific user.  
- `public Vector<Chirp> getChirpsByHashtag(String hashtag)` — Finds and returns all chirps containing a given hashtag.  
- `public Vector<Chirp> getAllChirps()` — Returns all chirps in the system, sorted by most recent.

---

## 5. Display Logic (DL)

Manages how users interact with Chirpy through webpages. It uses Freemarker templates and HTTP handlers to render dynamic content.

### RegisterHandler.java (/register/)

Handles registration form and submission.

- Displays the registration form.  
- Validates password match.  
- Calls `UserService.registerUser()`.  
- On success: 302 redirect to /login/.  
- On failure or first load: renders register.thtml with error messages.

### LoginHandler.java (/login/)

Handles login requests.

- Displays the login form.  
- Calls `UserService.authenticateUser()`.  
- Sets an authentication cookie on success.  
- Redirects to /timeline/ after successful login.

### ListUsersHandler.java (/listusers/)

Displays all usernames.

- Calls `UserService.getAllUsers()`.  
- Renders the list in listusers.thtml.

---

## 6. Data Storage and Persistence

In Chirpy 1.0, data is stored temporarily in memory using Java data structures.  
In Chirpy 2.0, the special feature adds persistent storage using JSON files:

- Two files, `users.json` and `chirps.json`, are stored on the server.  
- When the server starts, these files are read and loaded into memory.  
- When users register or post, both in-memory data and JSON files are updated.  
- Data persists across server restarts.

---

## 7. Storage for Relationships

User relationships (followers and contacts) are stored directly within the User object.  
For Chirpy 2.0, relationships will also be written to and restored from `relationships.json` to ensure connections persist across restarts.

---

## 8. Authentication and Cookies: Extra Feature

Authentication uses secure session tokens instead of storing usernames directly in cookies.

### Login Process

1. User submits credentials.  
2. `UserService` verifies credentials.  
3. Secure random session token (UUID) created.  
4. Server stores mapping of token → username.  
5. Cookie sent to browser:  


### Subsequent Requests

- Browser sends cookie automatically.  
- Server checks token in sessionMap.  
- If valid → user is logged in.  
- If invalid → redirect to login page.

### Output

- On Cookies Page, shows currently logged in user with their secure token

**Methods in UserService:**
- `String authenticateAndCreateSession(String username, String password)` - Returns session token or null
- `String getUsernameFromSession(String token)` - Returns username or null

**Storage:**
- `HashMap<String, String> sessions` - Maps session tokens to usernames

This system keeps users logged in securely while avoiding exposure of sensitive information.

---

## 9. How the Layers Work Together

1. User visits /register/, DL shows the registration form.  
2. BLL validates input and creates user.  
3. DAO stores data (then writes to JSON).  
4. During login, DL passes credentials to BLL.  
5. BLL authenticates and creates a secure cookie.  
6. DL checks cookie and retrieves data via BLL.

---

## 10. Testing Plan

Tests verify both user functionality and data persistence.

- Registering a user adds them to memory and JSON.  
- Duplicate username → error message.  
- Invalid passwords → error.  
- Successful login → valid cookie + redirect.  
- Failed login → error message.  
- Logout removes session token and redirects.  
- Restarting server keeps data intact via persistence.

---

## 11. Testing Plan Specificity

Tests ensure persistent storage works across restarts — users, chirps, and relationships reload correctly from JSON files.  
System should function normally after multiple restarts.

---

## 12. Future Work (Chirpy 2.0)

Future improvements include:

- Search by username or hashtag.  
- Full timeline feeds sorted by time.  
- Image uploads with chirps.  
- Expiring session tokens.  
- Faster storage access.  
- Moderator accounts to manage content.

---

## 13. Summary

Chirpy 1.0 establishes a secure, modular, and scalable foundation using a DAO–BLL–DL architecture.  
It supports user registration, login, and data persistence via JSON-based storage.  
Chirpy 2.0 will extend these capabilities to include timelines, hashtags, search, and richer user interactions.



