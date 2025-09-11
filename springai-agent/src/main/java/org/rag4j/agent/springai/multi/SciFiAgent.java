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

    public SciFiAgent(ChatClient chatClient) {
        super(chatClient);
    }

    @Override
    public Conversation doInvoke(String userId, Conversation.Message userMessage) {
        logger.info("SciFiAgent invoke userId = {}, userMessage = {}", userId, userMessage);

        String content = "Dummy reponse from SciFiAgent";

        logger.info("SpringAIAgent invoke content = {}", content);
        return new Conversation(List.of(userMessage, new Conversation.Message(content, Sender.ASSISTANT)));
    }
}
