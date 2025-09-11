package org.rag4j.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.plain.memory")
public class PlainAgentMemoryConfigProperties {
    private int maxConversationSize = 10;

    public int getMaxConversationSize() {
        return maxConversationSize;
    }

    public void setMaxConversationSize(int maxConversationSize) {
        this.maxConversationSize = maxConversationSize;
    }
}
