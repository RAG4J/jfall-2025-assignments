package org.rag4j.agent.memory;

import org.rag4j.agent.core.Conversation;

/**
 * Memory interface for storing and retrieving conversations.
 * <p>
 * This interface defines methods to store a conversation associated with a user ID
 * and retrieve the conversation for a given user ID.
 */
public interface Memory {

    /**
     * Stores a conversation for a given user ID.
     *
     * @param userId       the ID of the user
     * @param conversation the conversation to store
     */
    void storeConversation(String userId, Conversation conversation);

    /**
     * Retrieves the conversation for a given user ID.
     * If no conversation exists for the user, an empty conversation is returned.
     *
     * @param userId the ID of the user
     * @return the conversation associated with the user ID, or an empty conversation if none exists
     */
    Conversation retrieveConversation(String userId);
}
