package org.rag4j.agent.springai.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

public class ToolCallbackUtility {
    public static ToolCallback[] wrapToolCallbacks(List<McpSyncClient> mcpSyncClients) {
        var provider = new SyncMcpToolCallbackProvider(mcpSyncClients);
        ToolCallback[] toolCallbacks = provider.getToolCallbacks();
        ToolCallback[] wrappedCallbacks = new ToolCallback[toolCallbacks.length];
        for (int i = 0; i < toolCallbacks.length; i++) {
            wrappedCallbacks[i] = new FixArgumentSyncMcpToolCallback(toolCallbacks[i]);
        }
        return wrappedCallbacks;
    }
}
