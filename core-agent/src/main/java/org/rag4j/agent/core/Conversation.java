package org.rag4j.agent.core;

import java.util.List;

/**
 * Represents a conversation consisting of a list of messages.
 * <p>
 * This record encapsulates the messages exchanged in a conversation.
 * Each message is represented by the nested {@link Message} record.
 */
public record Conversation(List<Message> messages) {

    /**
     * Represents a single message in a conversation.
     *
     * @param content the textual content of the message
     * @param sender  the sender of the message
     */
    public record Message(String content, Sender sender) {
    }
}
