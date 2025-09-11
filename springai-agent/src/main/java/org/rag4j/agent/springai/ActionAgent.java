package org.rag4j.agent.springai;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.ArrayList;
import java.util.List;

import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.USER;

public abstract class ActionAgent implements Agent {
    protected final ChatClient chatClient;

    public ActionAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Conversation invoke(String userId, Conversation.Message userMessage) {
        return this.doInvoke(userId, userMessage);
    }

    protected abstract Conversation doInvoke(String userId, Conversation.Message userMessage);
}
