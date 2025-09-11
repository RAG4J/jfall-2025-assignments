package org.rag4j.agent.embabel;

import com.embabel.agent.api.common.autonomy.*;
import com.embabel.agent.core.*;
import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * EmbabelAgent is an implementation of the Agent interface that acts as the wrapper for the Embabel agent platform.
 * We need this wrapper to be able to switch between the different agent platforms without changing the codebase.
 */
public record EmbabelAgent(Autonomy autonomy) implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(EmbabelAgent.class);

    /**
     * Invokes the agent with a user ID and a user message, returning a conversation
     * with an embellished response.
     *
     * @param userId      the ID of the user
     * @param userMessage the message from the user
     * @return a Conversation object containing the user's message and the agent's embellished response
     */
    @Override
    public Conversation invoke(String userId, Conversation.Message userMessage) {
        logger.info("EmbabelAgent processing message from user: {}", userId);
        logger.debug("User message: {}", userMessage.content());

        // Create a list to hold the conversation messages
        List<Conversation.Message> messages = new ArrayList<>();
        messages.add(userMessage);

        try {
            AgentProcessExecution agentProcessExecution = this.autonomy.chooseAndRunAgent(userMessage.content(), ProcessOptions.getDEFAULT());
            
            Conversation conversation = (Conversation) agentProcessExecution.getOutput();

            // Add null checks to handle cases where conversation or messages might be null
            if (conversation.messages() != null && !conversation.messages().isEmpty()) {
                logger.debug("Generated embellished response: {}", conversation.messages().getFirst().content());
                messages.add(conversation.messages().getFirst());
            } else {
                logger.warn("Received null or empty conversation response from agent platform");
                messages.add(new Conversation.Message(
                        "I'm sorry, I couldn't generate a response at this time.",
                        Sender.ASSISTANT
                ));
            }
        } catch (ProcessExecutionException e) {
            logger.error("Error during agent execution: {}", e.getMessage(), e);
            messages.add(new Conversation.Message(
                    "I'm sorry, but I encountered an error while processing your request.",
                    Sender.ASSISTANT
            ));
        }

        return new Conversation(messages);
    }
}
