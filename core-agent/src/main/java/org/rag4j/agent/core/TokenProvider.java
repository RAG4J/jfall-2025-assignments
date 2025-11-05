package org.rag4j.agent.core;

import java.util.Optional;

/**
 * Interface for providing dynamic OpenAI API tokens.
 * This allows implementations to supply tokens from various sources (configuration, cache, etc.)
 * and enables automatic token refresh mechanisms.
 */
public interface TokenProvider {
    /**
     * Get the current valid token.
     * Implementations may auto-refresh the token if needed.
     * 
     * @return Optional containing the current token, or empty if no valid token is available
     */
    Optional<String> getCurrentToken();
}
