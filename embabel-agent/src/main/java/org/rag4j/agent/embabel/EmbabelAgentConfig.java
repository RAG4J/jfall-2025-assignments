package org.rag4j.agent.embabel;

import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.config.annotation.LoggingThemes;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.agent.core.Agent;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("embabel")
public class EmbabelAgentConfig {

    @Bean
    @Primary
    public Agent embabelAgent(AgentPlatform agentPlatform) {
        return new EmbabelAgent(agentPlatform);
    }

    @Bean
    public EmbabelConferenceTools embabelConferenceTools(ConferenceTalksRepository conferenceTalksRepository) {
        return new EmbabelConferenceTools(conferenceTalksRepository);
    }

    @Bean
    public ConferenceTalksRepository getConferenceTalksRepository() {
        return new ConferenceTalksRepository();
    }
}
