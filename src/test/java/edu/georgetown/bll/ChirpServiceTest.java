package edu.georgetown.bll;

import edu.georgetown.dao.Chirp;
import edu.georgetown.bll.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ChirpServiceTest {

    private ChirpService chirpService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        chirpService = new ChirpService();
        userService = new UserService(Logger.getLogger("test-logger"));
        // create some users
        userService.registerUser("alice", "pw");
        userService.registerUser("bob", "pw");
        userService.registerUser("carol", "pw");
    }

    @Test
    public void testCreateAndGetUserChirps() {
        Chirp c1 = chirpService.createChirp("alice", "hello world #greetings");
        Chirp c2 = chirpService.createChirp("alice", "another chirp");

        List<Chirp> aliceChirps = chirpService.getUserChirps("alice");
        assertEquals(2, aliceChirps.size());
        // newest first
        assertEquals(c2.getId(), aliceChirps.get(0).getId());
        assertEquals(c1.getId(), aliceChirps.get(1).getId());
    }

    @Test
    public void testHomeTimelineIncludesFolloweesChirps() throws InterruptedException {
        // alice follows bob
        userService.followUser("alice", "bob");

        Chirp b1 = chirpService.createChirp("bob", "bob's first #gtest");
        Thread.sleep(5);
        Chirp b2 = chirpService.createChirp("bob", "bob's second");

        List<Chirp> home = chirpService.getHomeTimeline("alice", userService);
        assertEquals(2, home.size());
        assertEquals(b2.getId(), home.get(0).getId());
        assertEquals(b1.getId(), home.get(1).getId());
    }

    @Test
    public void testGetChirpsByHashtag() {
        chirpService.createChirp("alice", "this is #Georgetown awesome");
        chirpService.createChirp("bob", "other post #georgetown");
        chirpService.createChirp("carol", "no tag here");

        List<Chirp> results = chirpService.getChirpsByHashtag("#Georgetown");
        assertEquals(2, results.size());
        // ensure both authors present
        assertTrue(results.stream().anyMatch(c -> c.getAuthor().equals("alice")));
        assertTrue(results.stream().anyMatch(c -> c.getAuthor().equals("bob")));
    }

    @Test
    public void testGetAllChirps() {
        chirpService.createChirp("alice", "a1");
        chirpService.createChirp("bob", "b1");
        List<Chirp> all = chirpService.getAllChirps();
        assertEquals(2, all.size());
    }
}
