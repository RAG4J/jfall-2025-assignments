package org.rag4j.agent;

import com.openai.client.OpenAIClient;
import org.rag4j.agent.core.Agent;
import org.rag4j.agent.memory.Memory;
import org.rag4j.agent.reasoning.OpenAIReasoning;
import org.rag4j.agent.reasoning.SystemPrompt;
import org.rag4j.agent.tools.AgentAsTool;
import org.rag4j.agent.tools.Tool;
import org.rag4j.agent.tools.ToolRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("plain-multi")
public class PlainMultiAgentConfig {

    @Bean(name = "agentRegistry")
    public ToolRegistry agentRegistry(@Qualifier("talksAgent") Agent agent, @Qualifier("scifiAgent") Agent scifiAgent) {
        List<Tool> tools = List.of(
                new AgentAsTool("talks_agent", "Agent to lookup talks based on the question from the user.", agent),
                new AgentAsTool("scifi_agent", "Agent that talks about science fiction.", scifiAgent)
        );
        return new ToolRegistry(tools);
    }

    @Bean
    @Primary
    public Agent orchestratorAgent(OpenAIClient openAIClient,
                                   PlainAgentReasoningConfigProperties agentConfigProperties,
                                   @Qualifier("agentRegistry") ToolRegistry toolRegistry,
                                   Memory memory) throws Exception {

        SystemPrompt systemPrompt = new SystemPrompt(
                "Orchestrator Agent",
                "You are an AI agent that orchestrates other agents and ask them to answer questions.",
                toolRegistry);
        OpenAIReasoning openAIReasoning = new OpenAIReasoning(openAIClient, systemPrompt);

        return new PlainJavaAgent(openAIReasoning, agentConfigProperties.getMaxReasoningSteps(), toolRegistry, memory);
    }

    @Bean
    public Agent talksAgent(
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

    @Bean
    public Agent scifiAgent(
            OpenAIClient openAIClient,
            PlainAgentReasoningConfigProperties agentConfigProperties,
            Memory memory) {

        SystemPrompt systemPrompt = new SystemPrompt(
                "SciFi Agent",
                "You are an AI agent that answers questions about SciFi characters and movies. Do not " +
                        "answers questions about other genres. If you don't know the answer, just say you don't know. " +
                        "Do not try to make up an answer.");
        OpenAIReasoning openAIReasoning = new OpenAIReasoning(openAIClient, systemPrompt);

        return new PlainJavaAgent(openAIReasoning, agentConfigProperties.getMaxReasoningSteps(), memory);
    }
}
