package org.rag4j.tokens;

import java.util.Optional;

/**
 * Interface for resolving token-related configuration from various sources.
 * Implementations define the priority order for configuration resolution.
 */
public interface TokenConfigResolver {
    
    /**
     * Resolve the proxy URL from configuration or environment.
     * 
     * @return Optional containing proxy URL, or empty if not configured
     */
    Optional<String> resolveProxyUrl();
    
    /**
     * Resolve an existing token from configuration or environment.
     * This is typically used for manually configured tokens.
     * 
     * @return Optional containing existing token, or empty if not configured
     */
    Optional<String> resolveExistingToken();
    
    /**
     * Resolve the password for automatic token fetching.
     * 
     * @return Optional containing password, or empty if not configured
     */
    Optional<String> resolvePassword();
    
    /**
     * Get the default user ID for token operations.
     * 
     * @return Default user ID
     */
    String resolveDefaultUserId();
}
