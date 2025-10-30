package org.rag4j.agent.springai.multi;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.springai.advisor.PromptInjectionGuardAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.rag4j.agent.core.Sender.ASSISTANT;

public record RouterAgent(ChatClient chatClient, AgentRegistry agentRegistry) implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(RouterAgent.class);

    @Override
    public Conversation invoke(String userId, Conversation.Message userMessage) {
        logger.info("RouterAgent invoke userId = {}, userMessage = {}", userId, userMessage);

        String selectorPrompt = String.format("""
                Analyze the input and select the most appropriate agent from these options: %s
                The chosen agent should be able to answer the question from the input.
                If you think neither of the agents will be able to answer the question, select UNKNOWN instead.
                First explain your reasoning, then provide your selection in this JSON format:
                
                \\{
                    "reasoning": "Brief explanation of why this question should be routed to a specific agent.",
                    "selection": "The chosen agent"
                \\}
                """, agentRegistry.getAvailableAgents(userId));

        RoutingResponse routingResponse = this.chatClient.prompt()
                .advisors(new PromptInjectionGuardAdvisor())
                .system(selectorPrompt)
                .user(userMessage.content())
                .call()
                .entity(RoutingResponse.class);
        assert routingResponse != null;
        logger.info("Router reasoning = {}", routingResponse.reasoning);
        logger.info("Chosen selection = {}", routingResponse.selection);
        if (routingResponse.selection == null || routingResponse.selection.isEmpty() || routingResponse.selection.equals("UNKNOWN")) {
            String answer = "Unfortunately I am not able to answer your question. I can only answer questions about " +
                    "conference talks or SciFi subjects.";
            return new Conversation(List.of(userMessage, new Conversation.Message(answer, ASSISTANT)));
        } else if (routingResponse.selection().equals("BLOCKED")) {
            return new Conversation(List.of(userMessage, new Conversation.Message(routingResponse.reasoning, ASSISTANT)));

        } else {
            return agentRegistry.getAgent(routingResponse.selection).invoke(userId, userMessage);
        }
    }

    record RoutingResponse(String reasoning, String selection) {
    }
}
