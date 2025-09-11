package org.rag4j.agent.core;

public interface Agent {
    /**
     * Invokes the agent with a user ID and a user message, returning a conversation.
     *
     * @param userId      the ID of the user
     * @param userMessage the message from the user
     * @return a Conversation object containing the user's message and the agent's response
     */
    Conversation invoke(String userId, Conversation.Message userMessage);
}
