package org.rag4j.agent.springai;

import org.rag4j.agent.core.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

import static org.rag4j.agent.core.Sender.ASSISTANT;

/**
 * Spring AI-based implementation of the Agent interface.
 * Uses Spring AI's ChatClient for LLM interactions.
 */
public class TalksAgent extends ActionAgent {
    private static final Logger logger = LoggerFactory.getLogger(TalksAgent.class);

    private final ConferenceTalksTools conferenceTalksTools;
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;
    private final ToolCallback[] toolCallbacks;

    public TalksAgent(ChatClient chatClient,
                      ConferenceTalksTools tools,
                      ChatMemory memory,
                      SyncMcpToolCallbackProvider syncMcpToolCallbackProvider,
                      ToolCallback[] toolCallbacks) {
        super(chatClient, memory);
        this.conferenceTalksTools = tools;
        this.syncMcpToolCallbackProvider = syncMcpToolCallbackProvider;
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    protected Conversation doInvoke(String userId, Conversation.Message userMessage) {
        logger.info("SpringAIAgent invoke userId = {}, userMessage = {}", userId, userMessage);

        String prompt = """
                You are an AI agent that answers questions about conference talks.
                Do not answer generic questions, even if you know the answers.
                Stick to information about conferences and the program that is available through your tools.
                
                You can also manage favourites for users. You can add talks and list them.
                When a user asks for a list of favourite talks, they can filter the talks by speaker.
                """;

        String content = this.chatClient.prompt()
                .system(prompt)
                .user(userMessage.content())
                .tools(this.conferenceTalksTools)
                .toolCallbacks(this.toolCallbacks)
                .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory).conversationId(userId).build())
                .call()
                .content();
        assert content != null;
        logger.info("SpringAIAgent invoke content = {}", content);
        return convertChatMemoryToConversation(chatMemory, userId);
    }
}
