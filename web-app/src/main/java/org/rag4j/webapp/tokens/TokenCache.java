package org.rag4j.webapp.tokens;

import java.util.Optional;

/**
 * Interface for caching tokens with validation capabilities.
 * Implementations may use in-memory, disk, or distributed caching.
 */
public interface TokenCache {
    
    /**
     * Retrieve the cached token if available.
     * 
     * @return Optional containing cached token, or empty if no token is cached
     */
    Optional<CachedToken> get();
    
    /**
     * Store a token in the cache.
     * 
     * @param token Token to cache
     */
    void put(CachedToken token);
    
    /**
     * Clear all cached tokens.
     */
    void clear();
    
    /**
     * Check if a cached token is still valid based on expiration time and buffer.
     * 
     * @param token Token to validate
     * @param bufferMinutes Safety buffer in minutes before actual expiration
     * @return true if token is valid (not expired considering buffer), false otherwise
     */
    boolean isValid(CachedToken token, int bufferMinutes);
    
    /**
     * Represents a cached token with metadata.
     * expiresAtMillis is always stored in milliseconds for consistency.
     */
    record CachedToken(
        String token,
        String userId,
        long expiresAtMillis  // Unix timestamp in MILLISECONDS
    ) {}
}
