package org.rag4j.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.plain.reasoning")
public class PlainAgentReasoningConfigProperties {
    private int maxReasoningSteps = 5;

    public int getMaxReasoningSteps() {
        return maxReasoningSteps;
    }

    public void setMaxReasoningSteps(int maxReasoningSteps) {
        this.maxReasoningSteps = maxReasoningSteps;
    }
}
