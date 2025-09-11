package org.rag4j.agent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToolTest {

    private static class DummyTool extends Tool {
        public DummyTool(String name, String description, String arguments) {
            super(name, description, arguments);
        }
        @Override
        public String execute(String arguments) {
            return "executed";
        }
    }

    @Test
    @DisplayName("extractSingleArgument returns value when argument is present")
    void extractSingleArgumentReturnsValueWhenArgumentIsPresent() {
        Tool tool = new DummyTool("dummy", "desc", "{\"name\": \"string\"}");
        String result = tool.extractSingleArgument("name", "{\"name\": \"value\"}");
        Assertions.assertEquals("value", result);
    }

    @Test
    @DisplayName("extractSingleArgument returns null when argument is missing")
    void extractSingleArgumentReturnsNullWhenArgumentIsMissing() {
        Tool tool = new DummyTool("dummy", "desc", "{\"name\": \"string\"}");
        String result = tool.extractSingleArgument("name", "{\"other\": \"value\"}");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("extractSingleArgument returns null for malformed JSON")
    void extractSingleArgumentReturnsNullForMalformedJson() {
        Tool tool = new DummyTool("dummy", "desc", "{\"name\": \"string\"}");
        String result = tool.extractSingleArgument("name", "{name: value}");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("extractSingleArgument handles argument names with special regex characters")
    void extractSingleArgumentHandlesSpecialRegexCharactersInArgumentName() {
        Tool tool = new DummyTool("dummy", "desc", "{\"na.me\": \"string\"}");
        String result = tool.extractSingleArgument("na.me", "{\"na.me\": \"special\"}");
        Assertions.assertEquals("special", result);
    }

    @Test
    @DisplayName("toolDefinition returns correct format")
    void toolDefinitionReturnsCorrectFormat() {
        Tool tool = new DummyTool("get_talk_by_name", "for obtaining conference talks by name", "{\"name\": \"string\"}");
        String result = tool.toolDefinition();
        Assertions.assertEquals("- get_talk_by_name: for obtaining conference talks by name {\"name\": \"string\"}", result);
    }

    @Test
    @DisplayName("toolName returns the correct name")
    void toolNameReturnsCorrectName() {
        Tool tool = new DummyTool("my_tool", "desc", "{\"name\": \"string\"}");
        Assertions.assertEquals("my_tool", tool.toolName());
    }
}
