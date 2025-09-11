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

class FindTalksByTitleTest {
    private ConferenceTalksRepository repository;
    private FindTalksByTitle tool;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(ConferenceTalksRepository.class);
        tool = new FindTalksByTitle(repository);
    }

    @Test
    @DisplayName("Returns talks for a known title")
    void returnsTalksForKnownTitle() {
        ConferenceTalk talk = Mockito.mock(ConferenceTalk.class);
        Mockito.when(repository.findTalksByTitle("Java")).thenReturn(List.of(talk));
        Mockito.when(talk.toString()).thenReturn("Talk about Java");
        String result = tool.execute("{\"title\": \"Java\"}");
        assertTrue(result.contains("Found talks with title 'Java':"));
        assertTrue(result.contains("Talk about Java"));
    }

    @Test
    @DisplayName("Returns message when no talks found for title")
    void returnsMessageWhenNoTalksFound() {
        Mockito.when(repository.findTalksByTitle("UnknownTitle")).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"title\": \"UnknownTitle\"}");
        assertEquals("No talks found with the title: UnknownTitle", result);
    }

    @Test
    @DisplayName("Handles empty title argument")
    void handlesEmptyTitleArgument() {
        Mockito.when(repository.findTalksByTitle("")).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"title\": \"\"}");
        assertEquals("Title cannot be empty.", result);
    }

    @Test
    @DisplayName("Handles null title argument")
    void handlesNullTitleArgument() {
        Mockito.when(repository.findTalksByTitle(null)).thenReturn(Collections.emptyList());
        String result = tool.execute("{\"title\": null}");
        assertEquals("Title cannot be empty.", result);
    }

    @Test
    @DisplayName("Returns multiple talks for a title")
    void returnsMultipleTalksForTitle() {
        ConferenceTalk talk1 = Mockito.mock(ConferenceTalk.class);
        ConferenceTalk talk2 = Mockito.mock(ConferenceTalk.class);
        Mockito.when(repository.findTalksByTitle("Spring")).thenReturn(List.of(talk1, talk2));
        Mockito.when(talk1.toString()).thenReturn("Spring Talk 1");
        Mockito.when(talk2.toString()).thenReturn("Spring Talk 2");
        String result = tool.execute("{\"title\": \"Spring\"}");
        assertTrue(result.contains("Spring Talk 1"));
        assertTrue(result.contains("Spring Talk 2"));
    }
}

