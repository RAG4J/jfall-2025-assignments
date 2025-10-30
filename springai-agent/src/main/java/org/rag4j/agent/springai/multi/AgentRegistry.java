package org.rag4j.agent.springai.multi;

import org.rag4j.agent.core.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Registry for managing multiple agents identified by unique names.
 */
public class AgentRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentRegistry.class);

    private final Map<String, Agent> registeredAgents = new HashMap<>();

    public Agent getAgent(String id) {
        return registeredAgents.get(id);
    }
    public void registerAgent(String name, Agent agent) {
        registeredAgents.put(name, agent);
    }

    private static final Map<Predicate<String>, String> ACCESS_RULES = Map.of(
            userId -> userId.startsWith("Darth"), "SciFi Specialist",
            userId -> userId.endsWith("Attendee"), "Conference Talks Specialist"
    );

    public Set<String> getAvailableAgents(String userId) {
        Set<String> availableAgents = ACCESS_RULES.entrySet().stream()
                .filter(entry -> entry.getKey().test(userId))
                .map(Map.Entry::getValue)
                .filter(agentName -> registeredAgents.keySet().stream()
                        .anyMatch(registered -> registered.equalsIgnoreCase(agentName)))
                .collect(Collectors.toSet());
        LOGGER.info("Registered agents {}", registeredAgents.keySet());
        LOGGER.info("Available agents {}", availableAgents);
        return availableAgents;
    }
}
