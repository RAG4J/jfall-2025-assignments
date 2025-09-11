package org.rag4j.agent.memory;

import org.jetbrains.annotations.NotNull;
import org.rag4j.agent.core.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WindowedConversationMemory implements Memory {
    private static final Logger logger = LoggerFactory.getLogger(WindowedConversationMemory.class);
    private final int maxConversationSize;
    private final Map<String, Conversation> conversationStore = new HashMap<>();

    public WindowedConversationMemory(int maxConversationSize) {
        this.maxConversationSize = maxConversationSize;
        logger.info("WindowedConversationMemory initialized with max size: {}", maxConversationSize);
    }

    @Override
    public void storeConversation(String userId, @NotNull Conversation conversation) {
        logger.info("Storing conversation for user: {}", userId);

        // If the conversation exceeds a certain size, trim it from the start
        if (conversation.messages().size() > this.maxConversationSize) {
            logger.info("Trimming conversation for user: {} to the last {} messages", userId, this.maxConversationSize);
            conversation.messages().subList(0, conversation.messages().size() - this.maxConversationSize).clear();
        }
        conversationStore.put(userId, conversation);
    }

    @Override
    public Conversation retrieveConversation(String userId) {
        logger.info("Retrieving conversation for user: {}", userId);

        // Check if the conversation exists, return an empty conversation if not
        if (!conversationStore.containsKey(userId)) {
            return new Conversation(new java.util.ArrayList<>());
        }
        return conversationStore.get(userId);
    }
}
