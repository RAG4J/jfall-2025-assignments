package org.rag4j.agent;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.memory.Memory;
import org.rag4j.agent.reasoning.Reasoning;
import org.rag4j.agent.tools.AgentAction;
import org.rag4j.agent.tools.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.OBSERVATION;

/**
 * Plain java agent that makes use of OpenAI for reasoning.
 */
public record PlainJavaAgent(Reasoning reasoning, int maxReasoningSteps, ToolRegistry toolRegistry, Memory memory) implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(PlainJavaAgent.class);

    public PlainJavaAgent(Reasoning reasoning, int maxReasoningSteps, Memory memory) {
        this(reasoning, maxReasoningSteps, new ToolRegistry(List.of()), memory);
    }


    @Override
    public Conversation invoke(String userId, Conversation.Message message) {

        Conversation conversation = memory.retrieveConversation(userId);
        Conversation.Message answerMessage = this.callReasoning(message, conversation, 1);

        conversation.messages().add(answerMessage);

        memory.storeConversation(userId, conversation);

        return conversation;
    }

    private Conversation.Message callReasoning(Conversation.Message userMessage, Conversation conversation, int reasoningStep) {
        if (reasoningStep > maxReasoningSteps) {
            logger.warn("Max reasoning steps reached, returning conversation without answer.");
            return new Conversation.Message("Unable to provide an answer after multiple reasoning steps.",
                    ASSISTANT);
        }

        // Call the reasoning service to get the response
        Conversation.Message response = reasoning.reason(userMessage, conversation);
//        if (userMessage.sender() != OBSERVATION) {
            conversation.messages().add(userMessage);
//        }
        logger.debug("Received response: {}", response.content());

        // Log thinking and extract answer or action
        logThinking(response.content());
        Optional<String> answer = extractAnswer(response.content());
        if (answer.isPresent()) {
            String answerText = answer.get();
            logger.info("Answer: {}", answerText);
            return new Conversation.Message(answerText, ASSISTANT);
        }

        Optional<AgentAction> action = extractAction(response.content());
        if (action.isPresent()) {
            AgentAction agentAction = action.get();
            logger.info("Action: {} with arguments: {}", agentAction.actionName(), agentAction.arguments());
            String actionResponse = this.executeAction(agentAction);
            Conversation.Message observationMessage = new Conversation.Message(
                    "Observation: " + actionResponse, OBSERVATION);
            return this.callReasoning(observationMessage, conversation, reasoningStep + 1);
        }

        return new Conversation.Message(
                "The Agent could not create an answer to your question.", ASSISTANT);
    }

    private String executeAction(AgentAction action) {
        try {
            return toolRegistry.executeTool(action.actionName(), action.arguments());
        } catch (IllegalArgumentException e) {
            logger.error("Error executing action [{}] with arguments [{}]: {}",
                    action.actionName(), action.arguments(), e.getMessage());
            return "Error executing action: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Unexpected error executing action [{}] with arguments [{}]: {}",
                    action.actionName(), action.arguments(), e.getMessage());
            return "Unexpected error executing action: " + e.getMessage();
        }
    }

    private static void logThinking(String output_message) {
        Pattern thinkPattern = Pattern.compile("^Think: (.*)$", Pattern.MULTILINE);
        Matcher thinkMatcher = thinkPattern.matcher(output_message);
        while (thinkMatcher.find()) {
            String thought = thinkMatcher.group(1);
            logger.info("Think: {}", thought);
        }
    }

    private static Optional<String> extractAnswer(String output_message) {
        Pattern answerPattern = Pattern.compile("^Answer: (.*)$", Pattern.MULTILINE);
        Matcher answerMatcher = answerPattern.matcher(output_message);
        if (answerMatcher.find()) {
            String answer = answerMatcher.group(1);
            return Optional.of(answer.trim());
        }
        return Optional.empty();
    }

    private static Optional<AgentAction> extractAction(String output_message) {
        Pattern actionPattern = Pattern.compile("^Action: (\\w+): (.*)$", Pattern.MULTILINE);
        Matcher actionMatcher = actionPattern.matcher(output_message);
        if (actionMatcher.find()) {
            String actionName = actionMatcher.group(1);
            String arguments = actionMatcher.group(2).trim();
            return Optional.of(new AgentAction(actionName, arguments));
        }
        return Optional.empty();
    }

}
