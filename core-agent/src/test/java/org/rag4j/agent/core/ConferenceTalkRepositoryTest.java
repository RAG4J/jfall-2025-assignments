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
        assertEquals(63, talks.size(), "There should be 189 ConferenceTalk objects read from talks.json");
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
        List<ConferenceTalk> talks = parser.findTalksBySpeaker("Jettro Coenradie");
        assertNotNull(talks);
        assertTrue(talks.stream().anyMatch(t -> t.speakers().stream().anyMatch(s -> s.name().equalsIgnoreCase("Jettro Coenradie"))),
            "Should find talks with 'Simon Ritter' as a speaker");
    }

    @Test
    void testFindTalksByAuthor_returnsCorrectTalks_second_speaker() {
        ConferenceTalksRepository parser = new ConferenceTalksRepository();
        List<ConferenceTalk> talks = parser.findTalksBySpeaker("Daniel Spee");
        assertNotNull(talks);
        assertTrue(talks.stream().anyMatch(t -> t.speakers().stream().anyMatch(s -> s.name().equalsIgnoreCase("Daniel Spee"))),
                "Should find talks with 'Daniel Spee' as a speaker");
    }
}
