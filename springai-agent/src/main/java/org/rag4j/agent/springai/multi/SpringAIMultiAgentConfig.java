package org.rag4j.agent.springai.multi;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.springai.TalksAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("springai-multi")
public class SpringAIMultiAgentConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .build();
    }

    @Bean
    public AgentRegistry agentRegister(TalksAgent talksAgent) {
        AgentRegistry agentRegistry = new AgentRegistry();
        agentRegistry.registerAgent("Conference Talks Specialist",  talksAgent);
        return agentRegistry;
    }

    @Bean
    @Primary
    public Agent routerAgent(ChatClient chatClient, AgentRegistry agentRegistry) {
        return new RouterAgent(chatClient, agentRegistry);
    }

    @Bean
    public TalksAgent talksAgent(ChatClient chatClient) {
        return new TalksAgent(chatClient);
    }
}
