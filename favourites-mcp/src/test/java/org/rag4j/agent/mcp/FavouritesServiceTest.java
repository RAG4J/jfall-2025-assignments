package org.rag4j.agent.mcp;

import org.junit.jupiter.api.Test;
import org.rag4j.agent.mcp.model.FavouriteTalk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class FavouritesServiceTest {

    @Test
    void addFavouriteReturnsConfirmationMessage() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk = new FavouriteTalk("Title", new String[]{"Speaker"});
        String result = service.addFavourite("userA", talk);
        verify(repo).addFavourite("userA", talk);
        assertTrue(result.contains("Favourite talk 'Title' by Speaker added for user userA"));
    }

    @Test
    void listFavouritesReturnsAllForUser() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk1 = new FavouriteTalk("Title1", new String[]{"Speaker1"});
        FavouriteTalk talk2 = new FavouriteTalk("Title2", new String[]{"Speaker2"});
        when(repo.getFavourites("userB")).thenReturn(List.of(talk1, talk2));
        List<FavouriteTalk> favs = service.listFavourites("userB", null);
        assertEquals(2, favs.size());
        assertTrue(favs.contains(talk1));
        assertTrue(favs.contains(talk2));
    }

    @Test
    void listFavouritesFiltersBySpeakerName() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk1 = new FavouriteTalk("Title1", new String[]{"Alice"});
        FavouriteTalk talk2 = new FavouriteTalk("Title2", new String[]{"Bob"});
        when(repo.getFavourites("userC")).thenReturn(List.of(talk1, talk2));
        List<FavouriteTalk> filtered = service.listFavourites("userC", "bob");
        assertEquals(1, filtered.size());
        assertEquals("Title2", filtered.getFirst().title());
    }

    @Test
    void addFavouriteDoesNotDuplicateForSameUser() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk = new FavouriteTalk("Title", new String[]{"Speaker"});
        service.addFavourite("userA", talk);
        service.addFavourite("userA", talk);
        verify(repo, times(2)).addFavourite("userA", talk);
    }

    @Test
    void listFavouritesReturnsEmptyForUnknownUser() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        when(repo.getFavourites("unknownUser")).thenReturn(List.of());
        List<FavouriteTalk> favs = service.listFavourites("unknownUser", null);
        assertTrue(favs.isEmpty());
    }

    @Test
    void listFavouritesReturnsEmptyWhenNoSpeakerMatches() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk = new FavouriteTalk("Title", new String[]{"Speaker"});
        when(repo.getFavourites("userD")).thenReturn(List.of(talk));
        List<FavouriteTalk> filtered = service.listFavourites("userD", "Nonexistent");
        assertTrue(filtered.isEmpty());
    }

    @Test
    void addFavouriteWithMultipleSpeakersReturnsCorrectMessage() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk = new FavouriteTalk("Title", new String[]{"Speaker1", "Speaker2"});
        String result = service.addFavourite("userE", talk);
        verify(repo).addFavourite("userE", talk);
        assertTrue(result.contains("Speaker1, Speaker2"));
    }

    @Test
    void listFavouritesIsCaseInsensitiveForSpeakerFilter() {
        FavouritesRepository repo = mock(FavouritesRepository.class);
        FavouritesService service = new FavouritesService(repo);
        FavouriteTalk talk = new FavouriteTalk("Title", new String[]{"Alice"});
        when(repo.getFavourites("userF")).thenReturn(List.of(talk));
        List<FavouriteTalk> filtered = service.listFavourites("userF", "ALICE");
        assertEquals(1, filtered.size());
        assertEquals("Title", filtered.getFirst().title());
    }
}
