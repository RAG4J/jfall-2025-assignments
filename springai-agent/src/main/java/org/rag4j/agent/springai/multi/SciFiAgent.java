package org.rag4j.agent.springai.multi;

import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;
import org.rag4j.agent.springai.ActionAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

public class SciFiAgent extends ActionAgent {
    private static final Logger logger = LoggerFactory.getLogger(SciFiAgent.class);

    public SciFiAgent(ChatClient chatClient, ChatMemory chatMemory) {
        super(chatClient, chatMemory);
    }

    @Override
    public Conversation doInvoke(String userId, Conversation.Message userMessage) {
        logger.info("SciFiAgent invoke userId = {}, userMessage = {}", userId, userMessage);

        String prompt = """
                You are a geek that knows everything about Science Fiction related topics and likes to answer questions about this.
                Science Fiction is your only expertise, so you can not answer questions related to other topics.
                If the question is about a non-scifi topic, just say you don't know anything about that subject.
                """;

        String content = this.chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(userId).build())
                .system(prompt)
                .user(userMessage.content())
                .call()
                .content();

        logger.info("SpringAIAgent invoke content = {}", content);
        return convertChatMemoryToConversation(chatMemory, userId);
    }
}
