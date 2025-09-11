package org.rag4j.agent.springai.multi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SciFiAgentTest {
    private ChatClient chatClient;
    private ChatMemory chatMemory;
    private SciFiAgent sciFiAgent;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        chatMemory = mock(ChatMemory.class);
        sciFiAgent = new SciFiAgent(chatClient);
    }

    @Test
    @DisplayName("doInvoke returns a Conversation for a valid SciFi question")
    void doInvokeReturnsConversationForSciFiQuestion() {
        // given
        String userId = "user1";
        Conversation.Message userMessage = new Conversation.Message("Who is Darth Vader?", org.rag4j.agent.core.Sender.USER);
        when(chatClient.prompt().system(anyString()).user(anyString()).advisors(anyList()).call().content()).thenReturn("Darth Vader is a central character in Star Wars.");
        when(chatMemory.get(ChatMemory.DEFAULT_CONVERSATION_ID)).thenReturn(Collections.emptyList());
        // when
        Conversation result = sciFiAgent.doInvoke(userId, userMessage);
        // then
        assertNotNull(result);
        assertEquals(2, result.messages().size()); // since chatMemory returns empty
    }

    @Test
    @DisplayName("doInvoke returns a Conversation for a non-SciFi question with correct fallback")
    void doInvokeReturnsConversationForNonSciFiQuestion() {
        // given
        String userId = "user2";
        Conversation.Message userMessage = new Conversation.Message("What is the capital of France?", org.rag4j.agent.core.Sender.USER);
        when(chatClient.prompt().system(anyString()).user(anyString()).advisors(anyList()).call().content()).thenReturn("I don't know anything about that subject.");
        when(chatMemory.get(ChatMemory.DEFAULT_CONVERSATION_ID)).thenReturn(Collections.emptyList());
        // when
        Conversation result = sciFiAgent.doInvoke(userId, userMessage);
        // then
        assertNotNull(result);
        assertEquals(2, result.messages().size());
    }
}
