package org.rag4j.agent.tools;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentAsTool extends Tool {
    private final Agent agent;
    private static final String TOOL_ARGUMENTS = "{\"userId\":\"string\",\"message\":\"string\"}";

    public AgentAsTool(String name, String description, Agent agent) {
        super(name, description, TOOL_ARGUMENTS);
        this.agent = agent;
    }

    @Override
    public String execute(String arguments) {
        Map<String, String> stringStringMap = extractAgentArguments(arguments);
        String userId = stringStringMap.get("userId");
        String message = stringStringMap.get("message");
        Conversation conversation = agent.invoke(userId, new Conversation.Message(message, Sender.USER));
        return conversation.messages().getLast().content();
    }
    Map<String, String> extractAgentArguments(String arguments) {
        Pattern pattern = Pattern.compile("\\{\"userId\":\\s*\"([^\"]+)\",\"message\":\\s*\"([^\"]+)\"}");
        Matcher matcher = pattern.matcher(arguments);
        Map<String, String> agentArguments = new HashMap<>();
        if (matcher.find()) {
            agentArguments.put("userId", matcher.group(1));
            agentArguments.put("message", matcher.group(2));
        }
        return agentArguments;
    }
}
