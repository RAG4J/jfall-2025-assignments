package org.rag4j.agent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ToolRegistryTest {
    private ToolRegistry registry;
    private Tool mockTool;

    @BeforeEach
    void setup() {
        mockTool = Mockito.mock(Tool.class);
        when(mockTool.toolName()).thenReturn("myTool");
        when(mockTool.toolDefinition()).thenReturn("myTool: for testing a tool {\"title\": \"string\"}");
        when(mockTool.execute(Mockito.anyString())).thenReturn("mock result");
        registry = new ToolRegistry(List.of(mockTool));
    }

    @Test
    @DisplayName("Executes tool successfully with valid name and arguments")
    void executesToolSuccessfullyWithValidNameAndArguments() {
        when(mockTool.execute("{\"key\":\"value\"}")).thenReturn("success");
        String result = registry.executeTool("myTool", "{\"key\":\"value\"}");
        assertEquals("success", result);
    }

    @Test
    @DisplayName("Throws exception when tool name is null")
    void throwsExceptionWhenToolNameIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            registry.executeTool(null, "{}")
        );
        assertEquals("Tool name cannot be null or empty.", ex.getMessage());
    }

    @Test
    @DisplayName("Throws exception when tool name is empty")
    void throwsExceptionWhenToolNameIsEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            registry.executeTool("", "{}")
        );
        assertEquals("Tool name cannot be null or empty.", ex.getMessage());
    }

    @Test
    @DisplayName("Throws exception when arguments are null")
    void throwsExceptionWhenArgumentsAreNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            registry.executeTool("myTool", null)
        );
        assertEquals("Arguments cannot be null.", ex.getMessage());
    }

    @Test
    @DisplayName("Throws exception when tool does not exist")
    void throwsExceptionWhenToolDoesNotExist() {
        ToolRegistry emptyRegistry = new ToolRegistry(List.of());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            emptyRegistry.executeTool("unknownTool", "{}")
        );
        assertEquals("No such tool: unknownTool", ex.getMessage());
    }
}

