package org.rag4j.webapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.USER;

class ChatControllerTest {

    @Test
    @DisplayName("handleChat returns chat view and sets confirmation and response on valid input")
    void handleChatReturnsChatViewAndSetsConfirmationAndResponseOnValidInput() {
        Agent agent = mock(Agent.class);
        Model model = mock(Model.class);
        String userId = "user123";
        String message = "Hello!";
        Conversation conversation = new Conversation(java.util.List.of(
                new Conversation.Message("Hello!", USER),
                new Conversation.Message("Hi!", ASSISTANT)
        ));
        when(agent.invoke(eq(userId), any(Conversation.Message.class))).thenReturn(conversation);

        ChatController controller = new ChatController(agent);
        String view = controller.handleChat(userId, message, model);

        assertEquals("chat", view);
        verify(model).addAttribute("confirmation", "Message received!");
        verify(model).addAttribute("userId", userId);
        verify(model).addAttribute("response", conversation);
        verify(model).addAttribute("error", null);
    }

    @Test
    @DisplayName("handleChat returns chat view and sets error when message is empty")
    void handleChatReturnsChatViewAndSetsErrorWhenMessageIsEmpty() {
        Agent agent = mock(Agent.class);
        Model model = mock(Model.class);

        ChatController controller = new ChatController(agent);
        String view = controller.handleChat("user123", "   ", model);

        assertEquals("chat", view);
        verify(model).addAttribute("confirmation", null);
        verify(model).addAttribute("error", "Message cannot be empty.");
        verify(model, never()).addAttribute(eq("response"), any());
    }

    @Test
    @DisplayName("handleChat generates new userId if userId is empty")
    void handleChatGeneratesNewUserIdIfUserIdIsEmpty() {
        Agent agent = mock(Agent.class);
        Model model = mock(Model.class);
        String message = "Hi there!";
        Conversation conversation = new Conversation(java.util.List.of(
                new Conversation.Message("Hi there!", USER),
                new Conversation.Message("Hello!", ASSISTANT)
        ));
        when(agent.invoke(anyString(), any(Conversation.Message.class))).thenReturn(conversation);

        ChatController controller = new ChatController(agent);
        String view = controller.handleChat("   ", message, model);

        assertEquals("chat", view);
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(agent).invoke(userIdCaptor.capture(), any(Conversation.Message.class));
        String generatedUserId = userIdCaptor.getValue();
        verify(model).addAttribute("userId", generatedUserId);
        verify(model).addAttribute("confirmation", "Message received!");
        verify(model).addAttribute("response", conversation);
        verify(model).addAttribute("error", null);
    }

    @Test
    @DisplayName("handleChat escapes HTML in message to prevent XSS")
    void handleChatEscapesHtmlInMessageToPreventXss() {
        Agent agent = mock(Agent.class);
        Model model = mock(Model.class);
        String userId = "userX";
        String message = "<script>alert('xss')</script>";
        Conversation conversation = new Conversation(java.util.List.of(
                new Conversation.Message("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;", USER),
                new Conversation.Message("Hi!", ASSISTANT)
        ));
        when(agent.invoke(eq(userId), any(Conversation.Message.class))).thenReturn(conversation);

        ChatController controller = new ChatController(agent);
        controller.handleChat(userId, message, model);

        ArgumentCaptor<Conversation.Message> msgCaptor = ArgumentCaptor.forClass(Conversation.Message.class);
        verify(agent).invoke(eq(userId), msgCaptor.capture());
        assertEquals("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;", msgCaptor.getValue().content());
    }
}