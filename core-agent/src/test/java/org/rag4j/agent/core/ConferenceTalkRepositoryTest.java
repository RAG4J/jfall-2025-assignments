package org.rag4j.agent.core;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConferenceTalkRepositoryTest {
    @Test
    void testParseTalksFromJson_returnsCorrectAmount() throws IOException {
        List<ConferenceTalk> talks = ConferenceTalksRepository.parseTalksFromJson();
        assertNotNull(talks, "Talks list should not be null");
        assertEquals(189, talks.size(), "There should be 189 ConferenceTalk objects read from talks.json");
    }

    @Test
    void testFindTalksByTitle_returnsCorrectTalks() {
        ConferenceTalksRepository parser = new ConferenceTalksRepository();
        List<ConferenceTalk> talks = parser.findTalksByTitle("Java");
        assertNotNull(talks);
        assertTrue(talks.stream().anyMatch(t -> t.title().toLowerCase().contains("java")),
            "Should find talks with 'Java' in the title");
    }

    @Test
    void testFindTalksByAuthor_returnsCorrectTalks() {
        ConferenceTalksRepository parser = new ConferenceTalksRepository();
        List<ConferenceTalk> talks = parser.findTalksBySpeaker("Simon Ritter");
        assertNotNull(talks);
        assertTrue(talks.stream().anyMatch(t -> t.speakers().stream().anyMatch(s -> s.name().equalsIgnoreCase("Simon Ritter"))),
            "Should find talks with 'Simon Ritter' as a speaker");
    }

    @Test
    void testFindTalksByAuthor_returnsCorrectTalks_second_speaker() {
        ConferenceTalksRepository parser = new ConferenceTalksRepository();
        List<ConferenceTalk> talks = parser.findTalksBySpeaker("Daniël Spee");
        assertNotNull(talks);
        assertTrue(talks.stream().anyMatch(t -> t.speakers().stream().anyMatch(s -> s.name().equalsIgnoreCase("Daniël Spee"))),
                "Should find talks with 'Daniël Spee' as a speaker");
    }
}
