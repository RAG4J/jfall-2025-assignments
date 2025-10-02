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
    protected final ChatMemory chatMemory;

    public ActionAgent(ChatClient chatClient, ChatMemory memory) {
        this.chatClient = chatClient;
        this.chatMemory = memory;
    }

    @Override
    public Conversation invoke(String userId, Conversation.Message userMessage) {
        return this.doInvoke(userId, userMessage);
    }

    protected abstract Conversation doInvoke(String userId, Conversation.Message userMessage);

    protected Conversation convertChatMemoryToConversation(ChatMemory chatMemory, String userId) {
        List<Conversation.Message> messages = new ArrayList<>();
        for  (Message message : chatMemory.get(userId)) {
            switch (message.getMessageType()) {
                case MessageType.USER:
                    messages.add(new Conversation.Message(message.getText(), USER));
                    break;
                case MessageType.ASSISTANT:
                    messages.add(new Conversation.Message(message.getText(), ASSISTANT));
                    break;
                default:
                    throw new RuntimeException("Unknown message type: " + message.getMessageType());
            }
        }
        return new Conversation(messages);
    }

}
