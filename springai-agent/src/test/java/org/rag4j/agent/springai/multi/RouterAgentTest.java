package org.rag4j.agent.springai.multi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RouterAgentTest {
    @Test
    @DisplayName("Returns fallback answer when selection is UNKNOWN")
    void returnsFallbackAnswerWhenSelectionIsUnknown() {
        //Given
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        RouterAgent.RoutingResponse routingResponse = new RouterAgent.RoutingResponse("No agent matches", "UNKNOWN");
        when(chatClient.prompt().system(anyString()).user(anyString()).call().entity(RouterAgent.RoutingResponse.class)).thenReturn(routingResponse);
        AgentRegistry agentRegistry = mock(AgentRegistry.class);
        when(agentRegistry.getAvailableAgents()).thenReturn(Set.of("TalksAgent", "SciFiAgent"));

        // When
        RouterAgent agent = new RouterAgent(chatClient, agentRegistry);
        Conversation.Message userMessage = new Conversation.Message("What is the weather?", Sender.USER);
        Conversation result = agent.invoke("user1", userMessage);

        // Then
        assertEquals(2, result.messages().size());
        assertEquals(Sender.USER, result.messages().get(0).sender());
        assertEquals(Sender.ASSISTANT, result.messages().get(1).sender());
        assertTrue(result.messages().get(1).content().contains("not able to answer your question"));
    }

    @Test
    @DisplayName("Routes to correct agent when selection is valid")
    void routesToCorrectAgentWhenSelectionIsValid() {
        // Given
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        RouterAgent.RoutingResponse routingResponse = new RouterAgent.RoutingResponse("SciFiAgent is best", "SciFiAgent");
        when(chatClient.prompt().system(anyString()).user(anyString()).call().entity(RouterAgent.RoutingResponse.class)).thenReturn(routingResponse);

        AgentRegistry agentRegistry = mock(AgentRegistry.class);
        SciFiAgent sciFiAgent = mock(SciFiAgent.class);
        Conversation conversation = mock(Conversation.class);
        when(agentRegistry.getAvailableAgents()).thenReturn(Set.of("TalksAgent", "SciFiAgent"));
        when(agentRegistry.getAgent("SciFiAgent")).thenReturn(sciFiAgent);
        when(sciFiAgent.invoke(anyString(), any(Conversation.Message.class))).thenReturn(conversation);

        // When
        RouterAgent agent = new RouterAgent(chatClient, agentRegistry);
        Conversation.Message userMessage = new Conversation.Message("Tell me about SciFi", Sender.USER);
        agent.invoke("user2", userMessage);

        // Then
        Mockito.verify(agentRegistry).getAgent("SciFiAgent");
        Mockito.verify(agentRegistry.getAgent("SciFiAgent")).invoke(eq("user2"), eq(userMessage));
    }

}

