package org.rag4j.webapp.tokens;

/**
 * Client interface for communicating with the token proxy API.
 * Abstracts HTTP communication to enable easy testing and alternative implementations.
 */
public interface TokenClient {
    
    /**
     * Validate a token with the proxy server.
     * 
     * @param proxyUrl Base URL of the proxy server
     * @param token Token to validate
     * @return Validation response with token details
     */
    TokenValidationResponse validateToken(String proxyUrl, String token);
    
    /**
     * Create a new token from the proxy server.
     * 
     * @param proxyUrl Base URL of the proxy server
     * @param userId User identifier for the token
     * @param password Password for token creation
     * @param durationMinutes Token validity duration in minutes
     * @return Creation response with new token details
     */
    TokenCreationResponse createToken(String proxyUrl, String userId, String password, int durationMinutes);
    
    /**
     * Response from token validation endpoint.
     */
    record TokenValidationResponse(
        boolean valid,
        int minutesRemaining,
        String user,
        String description,
        String expiresAtIso,
        String errorMessage
    ) {}
    
    /**
     * Response from token creation endpoint.
     */
    record TokenCreationResponse(
        boolean success,
        String token,
        long expiresAtSeconds,  // Unix timestamp in SECONDS from API
        int expiresInMinutes,
        String errorMessage
    ) {}
}
