package org.rag4j.agent;

import com.openai.client.OpenAIClient;
import org.rag4j.agent.core.Agent;
import org.rag4j.agent.memory.Memory;
import org.rag4j.agent.reasoning.OpenAIReasoning;
import org.rag4j.agent.reasoning.SystemPrompt;
import org.rag4j.agent.tools.ToolRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("plain")
public class PlainAgentConfig {

    @Bean
    @Primary
    public Agent orchestratorAgent(
            OpenAIClient openAIClient,
            PlainAgentReasoningConfigProperties agentConfigProperties,
            ToolRegistry toolRegistry,
            Memory memory) {

        SystemPrompt systemPrompt = new SystemPrompt(
                "Conference Talks Agent",
                "You are an AI agent that answers questions about conference talks.",
                toolRegistry);
        OpenAIReasoning openAIReasoning = new OpenAIReasoning(openAIClient, systemPrompt);

        return new PlainJavaAgent(openAIReasoning, agentConfigProperties.getMaxReasoningSteps(), toolRegistry, memory);
    }


}
