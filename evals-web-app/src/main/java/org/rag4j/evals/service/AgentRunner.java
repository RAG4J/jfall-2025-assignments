package org.rag4j.evals.service;

import org.rag4j.agent.core.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Dummy AgentRunner service that generates responses to questions.
 * This is a placeholder implementation to be replaced with actual agent logic.
 */
@Service
public class AgentRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentRunner.class);
    private final Agent agent;

    public AgentRunner(Agent agent) {
        this.agent = agent;
    }

    /**
     * Generates a response to the given question.
     * This is a dummy implementation that returns mock responses.
     * 
     * @param question The input question to process
     * @return A generated response (currently dummy data)
     */
    public String generateResponse(String question) {
        logger.info("Processing question: {}", question);

        // Add some question-specific context to make it more realistic
        String response = agent.invoke(UUID.randomUUID().toString(), new org.rag4j.agent.core.Conversation.Message(question, org.rag4j.agent.core.Sender.USER))
                .messages().stream()
                .filter(msg -> msg.sender() == org.rag4j.agent.core.Sender.ASSISTANT)
                .map(org.rag4j.agent.core.Conversation.Message::content)
                .reduce((first, second) -> second) // Get the last assistant message
                .orElse("I'm not sure how to answer that right now.");
        
        logger.info("Generated response for question '{}': {}", 
                   question.length() > 50 ? question.substring(0, 50) + "..." : question,
                   response.length() > 100 ? response.substring(0, 100) + "..." : response);
        
        return response;
    }

    /**
     * Generates responses for a batch of questions.
     * This can be useful for processing multiple questions efficiently.
     * 
     * @param questions List of questions to process
     * @return List of responses in the same order as the questions
     */
    public List<String> generateResponses(List<String> questions) {
        logger.info("Processing batch of {} questions", questions.size());
        
        return questions.stream()
                .map(this::generateResponse)
                .toList();
    }
    
    /**
     * Checks if the agent runner is available and ready to process questions.
     * In a real implementation, this might check API connectivity, model availability, etc.
     * 
     * @return true if the agent is ready, false otherwise
     */
    public boolean isReady() {
        // For the dummy implementation, always return true
        return true;
    }
    
    /**
     * Gets information about the current agent configuration.
     * This is useful for debugging and monitoring.
     * 
     * @return A string describing the current agent setup
     */
    public String getAgentInfo() {
        return "Dummy AgentRunner v1.0 - Mock responses enabled";
    }
}
