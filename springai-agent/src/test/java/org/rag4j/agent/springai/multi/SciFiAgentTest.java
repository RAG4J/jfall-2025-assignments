package org.rag4j.agent.springai.multi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

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
        sciFiAgent = new SciFiAgent(chatClient, chatMemory);
    }

    @Test
    @DisplayName("doInvoke returns a Conversation for a valid SciFi question")
    void doInvokeReturnsConversationForSciFiQuestion() {
        // given
        String userId = "user1";
        Conversation.Message userMessage = new Conversation.Message("Who is Darth Vader?", org.rag4j.agent.core.Sender.USER);
        String assistantResponse = "Darth Vader is a central character in Star Wars.";
        
        when(chatClient.prompt().system(anyString()).user(anyString()).advisors(any(Advisor[].class)).call().content()).thenReturn(assistantResponse);
        
        // Mock the chat memory to return the conversation history after the call
        List<Message> memoryMessages = List.of(
            new UserMessage(userMessage.content()),
            new AssistantMessage(assistantResponse)
        );
        when(chatMemory.get(userId)).thenReturn(memoryMessages);
        
        // when
        Conversation result = sciFiAgent.doInvoke(userId, userMessage);
        // then
        assertNotNull(result);
        assertEquals(2, result.messages().size());
        assertEquals(userMessage.content(), result.messages().get(0).content());
        assertEquals(assistantResponse, result.messages().get(1).content());
    }

    @Test
    @DisplayName("doInvoke returns a Conversation for a non-SciFi question with correct fallback")
    void doInvokeReturnsConversationForNonSciFiQuestion() {
        // given
        String userId = "user2";
        Conversation.Message userMessage = new Conversation.Message("What is the capital of France?", org.rag4j.agent.core.Sender.USER);
        String assistantResponse = "I don't know anything about that subject.";
        
        when(chatClient.prompt().system(anyString()).user(anyString()).advisors(anyList()).call().content()).thenReturn(assistantResponse);
        
        // Mock the chat memory to return the conversation history after the call
        List<Message> memoryMessages = List.of(
            new UserMessage(userMessage.content()),
            new AssistantMessage(assistantResponse)
        );
        when(chatMemory.get(userId)).thenReturn(memoryMessages);
        
        // when
        Conversation result = sciFiAgent.doInvoke(userId, userMessage);
        // then
        assertNotNull(result);
        assertEquals(2, result.messages().size());
        assertEquals(userMessage.content(), result.messages().get(0).content());
        assertEquals(assistantResponse, result.messages().get(1).content());
    }
}
