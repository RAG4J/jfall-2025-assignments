package org.rag4j.agent.mcp;

import org.junit.jupiter.api.*;
import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.rag4j.agent.mcp.model.FavouriteRepositoryException;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FavouritesRepositoryTest {
    private FavouritesRepository repository;
    private static final String TEST_USER = "user1";
    private static final File DATA_FILE = new File("data/favourites.json");
    private final FavouriteTalk TALK1 = new FavouriteTalk("Talk 1", new String[]{"Talk 1"});
    private final FavouriteTalk TALK2 = new FavouriteTalk("id2", new String[]{"Talk 2"});

    @BeforeEach
    void setUp() {
        if (DATA_FILE.exists()) {
            DATA_FILE.delete();
        }
        repository = new FavouritesRepository();
    }

    @AfterEach
    void tearDown() {
        if (DATA_FILE.exists()) {
            DATA_FILE.delete();
        }
    }

    @Test
    void addFavouriteShouldStoreTalkForUser() {
        repository.addFavourite(TEST_USER, TALK1);
        List<FavouriteTalk> favs = repository.getFavourites(TEST_USER);
        assertEquals(1, favs.size());
        assertTrue(favs.contains(TALK1));
    }

    @Test
    void addFavouriteShouldNotDuplicateTalk() {
        repository.addFavourite(TEST_USER, TALK1);
        repository.addFavourite(TEST_USER, TALK1);
        List<FavouriteTalk> favs = repository.getFavourites(TEST_USER);
        assertEquals(1, favs.size());
    }

    @Test
    void removeFavouriteShouldRemoveTalkForUser() {
        repository.addFavourite(TEST_USER, TALK1);
        repository.removeFavourite(TEST_USER, TALK1);
        List<FavouriteTalk> favs = repository.getFavourites(TEST_USER);
        assertFalse(favs.contains(TALK1));
        assertEquals(0, favs.size());
    }

    @Test
    void getFavouritesShouldReturnEmptyListForUnknownUser() {
        List<FavouriteTalk> favs = repository.getFavourites("unknown");
        assertNotNull(favs);
        assertTrue(favs.isEmpty());
    }

    @Test
    void addAndRemoveMultipleFavourites() {
        repository.addFavourite(TEST_USER, TALK1);
        repository.addFavourite(TEST_USER, TALK2);
        repository.removeFavourite(TEST_USER, TALK1);
        List<FavouriteTalk> favs = repository.getFavourites(TEST_USER);
        assertEquals(1, favs.size());
        assertTrue(favs.contains(TALK2));
    }
}

