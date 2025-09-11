package org.rag4j.agent.embabel;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

@Configuration
public class McpToolsConfig {

    private final List<McpSyncClient> mcpSyncClients;

    @Autowired
    public McpToolsConfig(@Lazy List<McpSyncClient> mcpSyncClients) {
        Assert.notNull(mcpSyncClients, "McpSyncClients must not be null");
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean(name = "mcpFavouritesToolsGroup")
    public ToolGroup mcpFavouritesToolsGroup() {
        return new McpToolGroup(
                ToolGroupDescription.Companion.invoke(
                        "A collection of tools to handle favourite items",
                        "mcp-favourites"
                ),
                "Java",
                "mcp-favourites",
                Set.of(ToolGroupPermission.HOST_ACCESS),
                mcpSyncClients,
                callback -> callback.getToolDefinition().name().contains("favourite")
        );
    }


}