package org.rag4j.agent.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Conversation.Message;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.rag4j.agent.core.Sender.USER;

class WindowedConversationMemoryTest {
    private WindowedConversationMemory memory;

    @BeforeEach
    void setUp() {
        memory = new WindowedConversationMemory(10);
    }

    @Test
    void testStoreAndRetrieveConversation() {
        String userId = "user1";
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("Hello", USER));
        Conversation conversation = new Conversation(messages);

        memory.storeConversation(userId, conversation);
        Conversation retrieved = memory.retrieveConversation(userId);
        assertEquals(1, retrieved.messages().size());
        assertEquals("Hello", retrieved.messages().getFirst().content());
    }

    @Test
    void testRetrieveEmptyConversationIfNotExists() {
        Conversation conversation = memory.retrieveConversation("unknown");
        assertNotNull(conversation);
        assertTrue(conversation.messages().isEmpty());
    }

    @Test
    void testTrimConversationToMaxSize() {
        String userId = "user2";
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            messages.add(new Message("msg" + i, USER));
        }
        Conversation conversation = new Conversation(messages);
        memory.storeConversation(userId, conversation);
        Conversation retrieved = memory.retrieveConversation(userId);
        assertEquals(10, retrieved.messages().size());
        assertEquals("msg5", retrieved.messages().get(0).content());
        assertEquals("msg14", retrieved.messages().get(9).content());
    }
}

