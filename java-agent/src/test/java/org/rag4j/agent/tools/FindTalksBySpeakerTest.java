package org.rag4j.agent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rag4j.agent.core.ConferenceTalk;
import org.rag4j.agent.core.ConferenceTalksRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindTalksBySpeakerTest {
    private ConferenceTalksRepository repository;
    private FindTalksBySpeaker tool;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(ConferenceTalksRepository.class);
        tool = new FindTalksBySpeaker(repository);
    }

    @Test
    @DisplayName("Returns talks for a known speaker")
    void returnsTalksForKnownSpeaker() {
        ConferenceTalk talk = Mockito.mock(ConferenceTalk.class);
        Mockito.when(repository.findTalksBySpeaker("Alice")).thenReturn(List.of(talk));
        Mockito.when(talk.toString()).thenReturn("Talk by Alice");
        String result = tool.execute("{\"speaker\": \"Alice\"}");
        assertTrue(result.contains("Found talks with speaker 'Alice':"));
        assertTrue(result.contains("Talk by Alice"));
    }

    @Test
    @DisplayName("Returns message when no talks found for speaker")
    void returnsMessageWhenNoTalksFound() {
        Mockito.when(repository.findTalksBySpeaker("Bob")).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"speaker\": \"Bob\"}");
        assertEquals("No talks found with the speaker: Bob", result);
    }

    @Test
    @DisplayName("Handles empty speaker argument")
    void handlesEmptySpeakerArgument() {
        Mockito.when(repository.findTalksBySpeaker("")).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"speaker\": \"\"}");
        assertEquals("Speaker name cannot be empty.", result);
    }

    @Test
    @DisplayName("Handles null speaker argument")
    void handlesNullSpeakerArgument() {
        Mockito.when(repository.findTalksBySpeaker(null)).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"speaker\": null}");
        assertEquals("Speaker name cannot be empty.", result);
    }

    @Test
    @DisplayName("Returns multiple talks for a speaker")
    void returnsMultipleTalksForSpeaker() {
        ConferenceTalk talk1 = Mockito.mock(ConferenceTalk.class);
        ConferenceTalk talk2 = Mockito.mock(ConferenceTalk.class);
        Mockito.when(repository.findTalksBySpeaker("Carol")).thenReturn(List.of(talk1, talk2));
        Mockito.when(talk1.toString()).thenReturn("Talk 1 by Carol");
        Mockito.when(talk2.toString()).thenReturn("Talk 2 by Carol");
        String result = tool.execute("{\"speaker\": \"Carol\"}");
        assertTrue(result.contains("Talk 1 by Carol"));
        assertTrue(result.contains("Talk 2 by Carol"));
    }
}

