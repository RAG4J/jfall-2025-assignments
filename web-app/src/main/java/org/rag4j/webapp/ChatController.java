package org.rag4j.webapp;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.RequestIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.rag4j.agent.core.Sender.USER;
import static org.springframework.web.util.HtmlUtils.htmlEscape;


@Controller
@Validated
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final Agent agent;

    public ChatController(Agent agent) {
        this.agent = agent;
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/chat")
    public String handleChat(
            @Validated @RequestParam("userId") String userId,
            @Validated @RequestParam("message") String message, Model model) {
        if (message == null || message.trim().isEmpty()) {
            model.addAttribute("confirmation", null);
            model.addAttribute("error", "Message cannot be empty.");
            return "chat";
        }
        if (userId == null || userId.trim().isEmpty()) {
            userId = UUID.randomUUID().toString();
        }
        RequestIdentity.set(userId);

        // Sanitize message to prevent XSS
        String sanitizedMessage = htmlEscape(message);
        logger.info("Received chat message: {} from {}", sanitizedMessage, userId);

        Conversation conversation = agent.invoke(userId, new Conversation.Message(sanitizedMessage, USER));

        model.addAttribute("confirmation", "Message received!");
        model.addAttribute("userId", userId);
        model.addAttribute("response", conversation);
        model.addAttribute("error", null);
        return "chat";
    }
}
