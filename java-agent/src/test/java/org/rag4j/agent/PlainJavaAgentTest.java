package org.rag4j.agent;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.memory.Memory;
import org.rag4j.agent.reasoning.Reasoning;
import org.rag4j.agent.tools.ToolRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.USER;

class PlainJavaAgentTest {

    @Test
    @Disabled
    @DisplayName("invoke returns conversation with user and assistant messages on happy path")
    void invokeReturnsConversationWithUserAndAssistantMessagesOnHappyPath() {
        ToolRegistry toolRegistry = mock(ToolRegistry.class);
        Reasoning reasoning = mock(Reasoning.class);
        Memory memory = mock(Memory.class);

        String userId = "user1";
        Conversation conversation = new Conversation(new java.util.ArrayList<>());
        when(memory.retrieveConversation(userId)).thenReturn(conversation);

        Conversation.Message assistantMessage = new Conversation.Message("Answer: Hi!", ASSISTANT);
        when(reasoning.reason(any(Conversation.Message.class), any(Conversation.class))).thenReturn(assistantMessage);

        PlainJavaAgent agent = new PlainJavaAgent(reasoning, 5, toolRegistry);
        Conversation.Message userMessage = new Conversation.Message("Hello", USER);
        Conversation result = agent.invoke(userId, userMessage);

        assertEquals(2, result.messages().size());
        assertEquals(userMessage, result.messages().get(0));
        assertEquals(new Conversation.Message("Hi!", ASSISTANT), result.messages().get(1));
        verify(memory).storeConversation(eq(userId), any(Conversation.class));
    }

    @Test
    @Disabled
    @DisplayName("invoke returns fallback message if reasoning returns no answer or action")
    void invokeReturnsFallbackMessageIfNoAnswerOrAction() {
        ToolRegistry toolRegistry = mock(ToolRegistry.class);
        Reasoning reasoning = mock(Reasoning.class);
        Memory memory = mock(Memory.class);
        PlainJavaAgent agent = new PlainJavaAgent(reasoning, 5, toolRegistry);
        String userId = "user2";
        Conversation.Message userMessage = new Conversation.Message("What?", USER);
        Conversation conversation = new Conversation(new java.util.ArrayList<>());
        when(memory.retrieveConversation(userId)).thenReturn(conversation);
        Conversation.Message assistantMessage = new Conversation.Message("No answer here", ASSISTANT);
        when(reasoning.reason(userMessage, conversation)).thenReturn(assistantMessage);

        Conversation result = agent.invoke(userId, userMessage);
        assertEquals(2, result.messages().size());
        assertEquals(userMessage, result.messages().get(0));
        assertEquals(new Conversation.Message("The Agent could not create an answer to your question.", ASSISTANT),
                result.messages().get(1));
    }

    @Test
    @Disabled
    @DisplayName("invoke handles action extraction and executes action")
    void invokeHandlesActionExtractionAndExecutesAction() {
        ToolRegistry toolRegistry = mock(ToolRegistry.class);
        Reasoning reasoning = mock(Reasoning.class);
        Memory memory = mock(Memory.class);

        String userId = "user3";
        Conversation conversation = new Conversation(new java.util.ArrayList<>());
        when(memory.retrieveConversation(userId)).thenReturn(conversation);

        // Simulate reasoning returning an action
        Conversation.Message actionMessage = new Conversation.Message("Action: get_talk_by_name: {\"name\":\"AI " +
                "Agent\"}", ASSISTANT);
        Conversation.Message answerMessage = new Conversation.Message("Answer: The talk is: AI Agent, by Jettro " +
                "Coenradie, in room 42, at 11:00", ASSISTANT);
        doReturn(actionMessage)
                .doReturn(answerMessage)
                .when(reasoning).reason(any(), eq(conversation));

        PlainJavaAgent agent = spy(new PlainJavaAgent(reasoning, 5, toolRegistry));
        Conversation.Message userMessage = new Conversation.Message("Get talk by name called AI Agent", USER);
        Conversation result = agent.invoke(userId, userMessage);
        assertEquals(2, result.messages().size());
        assertEquals(userMessage, result.messages().get(0));
        assertEquals(new Conversation.Message("The talk is: AI Agent, by Jettro Coenradie, in room 42, at 11:00",
                ASSISTANT), result.messages().get(1));
    }

}
