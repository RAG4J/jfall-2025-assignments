package org.rag4j.agent.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Tool {
    private final String name;
    private final String description;
    private final String arguments; // "{\"name\": \"string\"}"

    public Tool(String name, String description, String arguments) {
        this.name = name;
        this.description = description;
        this.arguments = arguments;
    }

    /**
     * Executes the tool with the provided arguments.
     * The arguments should be a JSON string that matches the expected format.
     * For example, if the tool expects a name, the arguments should be:
     * <pre>{"name": "string"}</pre>
     * @param arguments the JSON string containing the arguments for the tool
     * @return the result of the tool execution as a string
     * @throws IllegalArgumentException if the arguments are not in the expected format
     */
    public abstract String execute(String arguments);

    /**
     * Returns the following format for this tool:
     * <pre- get_talk_by_name: for obtaining conference talks by name {"name": "string"}</pre>
     * @return the tool definition string
     */
    public String toolDefinition() {
        return String.format("- %s: %s %s", name, description, arguments);
    }

    public String toolName() {
        return name;
    }

    String extractSingleArgument(String argumentName, String arguments) {
        Pattern pattern = Pattern.compile("\\{\"" + Pattern.quote(argumentName) + "\":\\s*\"([^\"]+)\"}");
        Matcher matcher = pattern.matcher(arguments);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
