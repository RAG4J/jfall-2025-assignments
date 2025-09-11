package org.rag4j.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToolRegistry is responsible for managing a collection of AgenticTools.
 * It allows for the registration of tools and provides methods to execute them.
 */
public class ToolRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ToolRegistry.class);
    private final Map<String, Tool> toolMap;


    public ToolRegistry(List<Tool> tools) {
        this.toolMap = new HashMap<>();
        for (Tool tool : tools) {
            this.toolMap.put(tool.toolName(), tool);
        }

        logger.info("Tool registry initialized with {} tools.", toolMap.size());
        tools.forEach(tool -> logger.info("Registered tool: {}", tool.toolDefinition()));
    }

    /**
     * Returns a list of strings describing the tools available in the registry in the format required by the LLM.
     *
     * @return List of tool descriptions. Each description includes the tool name, description, and arguments in JSON
     * format.
     */
    public List<String> toolDescriptions() {
        return toolMap.values().stream()
                .map(Tool::toolDefinition)
                .toList();
    }

    /**
     * Executes a tool by its name with the provided arguments.
     * If the tool does not exist, an IllegalArgumentException is thrown.
     * @param toolName the name of the tool to execute
     * @param arguments the arguments to pass to the tool in JSON format
     * @throws IllegalArgumentException if the tool does not exist
     * @return the result of the tool execution as a String
     */
    public String executeTool(String toolName, String arguments) {
        logger.info("Executing tool: {} with arguments: {}", toolName, arguments);
        if (toolName == null || toolName.isEmpty()) {
            logger.error("Tool name cannot be null or empty.");
            throw new IllegalArgumentException("Tool name cannot be null or empty.");
        }
        if (arguments == null) {
            logger.error("Arguments cannot be null.");
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        Tool tool = toolMap.get(toolName);
        if (tool == null) {
            logger.error("No such tool [{}].", toolName);
            throw new IllegalArgumentException("No such tool: " + toolName);
        }
        String result = tool.execute(arguments);
        logger.info("Tool [{}] executed successfully with result {}.", toolName, result);
        return result;
    }

}
