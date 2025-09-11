package org.rag4j.webapp.config;

import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class AgentConfig {

    @Profile("default")
    @Bean
    @Primary
    public Agent agent() {
        return new Agent() {
            private static final Logger LOGGER = LoggerFactory.getLogger(AgentConfig.class);
            @Override
            public Conversation invoke(String userId, Conversation.Message userMessage) {
                LOGGER.info("Dummy agent invoke userId = {}", userId);
                return new Conversation(List.of(
                        userMessage,
                        new Conversation.Message("This is a dummy response", Sender.ASSISTANT))
                );
            }
        };
    }
}
