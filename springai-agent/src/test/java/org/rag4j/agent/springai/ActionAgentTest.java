package org.rag4j.agent.springai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.USER;

class ActionAgentTest {
    private ChatClient chatClient;
    private ChatMemory chatMemory;
    private ActionAgent actionAgent;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        actionAgent = new ActionAgent(chatClient) {
            @Override
            protected Conversation doInvoke(String userId, Conversation.Message userMessage) {
                return new Conversation(Collections.singletonList(userMessage));
            }
        };
    }

    @Test
    @DisplayName("invoke returns result of doInvoke")
    void invokeReturnsDoInvokeResult() {
        // given
        Conversation.Message userMsg = new Conversation.Message("Hello", USER);
        // when
        Conversation result = actionAgent.invoke("user1", userMsg);
        // then
        assertEquals(1, result.messages().size());
        assertEquals("Hello", result.messages().getFirst().content());
    }
}
