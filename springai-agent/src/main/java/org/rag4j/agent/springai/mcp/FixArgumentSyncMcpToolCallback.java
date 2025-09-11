package org.rag4j.agent.springai.mcp;

import org.rag4j.agent.core.RequestIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.Map;

/**
 * A ToolCallback wrapper that ensures the "userId" argument is always set to the current request's user ID.
 * This is useful for tools that require user-specific context, such as managing user favourites.
 */
public class FixArgumentSyncMcpToolCallback implements ToolCallback {
    private static final Logger logger = LoggerFactory.getLogger(FixArgumentSyncMcpToolCallback.class);

    private final ToolCallback wrappedCallback;

    /**
     * Wraps a {@code SyncMcpToolCallback} instance.
     */
    public FixArgumentSyncMcpToolCallback(ToolCallback callback) {
        this.wrappedCallback = callback;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return wrappedCallback.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return wrappedCallback.getToolMetadata();
    }

    @Override
    public String call(String functionInput) {
        Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);
        if (arguments.containsKey("userId")) {
            String userId = RequestIdentity.getUserId();
            arguments.put("userId", userId);
            logger.info("Fixed userId argument to {}", userId);
        }

        return wrappedCallback.call(ModelOptionsUtils.toJsonString(arguments));
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return call(toolInput);
    }
}
