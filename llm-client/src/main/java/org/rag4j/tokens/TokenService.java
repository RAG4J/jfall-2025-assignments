package org.rag4j.tokens;

import jakarta.annotation.PostConstruct;
import org.rag4j.tokens.model.TokenStatus;
import org.rag4j.tokens.model.TokenValidationResult;
import org.rag4j.tokens.TokenCache.CachedToken;
import org.rag4j.tokens.TokenClient.TokenCreationResponse;
import org.rag4j.tokens.TokenClient.TokenValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing OpenAI proxy tokens with automatic fetching, validation, and caching.
 * Orchestrates TokenClient, TokenCache, and TokenConfigResolver components.
 */
@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final int TOKEN_REFRESH_BUFFER_MINUTES = 5;
    private static final int DEFAULT_TOKEN_DURATION_MINUTES = 180;
    
    private final TokenClient tokenClient;
    private final TokenCache tokenCache;
    private final TokenConfigResolver configResolver;
    
    public TokenService(
            TokenClient tokenClient,
            TokenCache tokenCache,
            TokenConfigResolver configResolver
    ) {
        this.tokenClient = tokenClient;
        this.tokenCache = tokenCache;
        this.configResolver = configResolver;
    }
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing TokenService");
        
        // Step 1: Try existing token from config/env
        Optional<String> existingToken = configResolver.resolveExistingToken();
        Optional<String> proxyUrl = configResolver.resolveProxyUrl();
        
        if (existingToken.isPresent() && proxyUrl.isPresent()) {
            logger.info("Attempting to use configured/environment token");
            TokenValidationResponse validation = tokenClient.validateToken(proxyUrl.get(), existingToken.get());
            if (validation.valid()) {
                long expiresAtMillis = System.currentTimeMillis() + (validation.minutesRemaining() * 60L * 1000L);
                CachedToken token = new CachedToken(
                    existingToken.get(),
                    validation.user() != null ? validation.user() : configResolver.resolveDefaultUserId(),
                    expiresAtMillis
                );
                tokenCache.put(token);
                logger.info("Configured token is valid for {} more minutes", validation.minutesRemaining());
                return;
            } else {
                logger.warn("Configured token is invalid or expired");
            }
        }
        
        // Step 2: Try cached token
        Optional<CachedToken> cached = tokenCache.get();
        if (cached.isPresent() && tokenCache.isValid(cached.get(), TOKEN_REFRESH_BUFFER_MINUTES)) {
            logger.info("Using valid token from cache for user: {}", cached.get().userId());
            return;
        }
        
        // Step 3: Try auto-fetch with password
        Optional<String> password = configResolver.resolvePassword();
        if (password.isPresent() && proxyUrl.isPresent()) {
            logger.info("Attempting to auto-fetch token using configured password");
            String userId = configResolver.resolveDefaultUserId();
            TokenCreationResponse response = tokenClient.createToken(
                proxyUrl.get(), userId, password.get(), DEFAULT_TOKEN_DURATION_MINUTES
            );
            if (response.success()) {
                long expiresAtMillis = response.expiresAtSeconds() * 1000L;
                CachedToken token = new CachedToken(response.token(), userId, expiresAtMillis);
                tokenCache.put(token);
                logger.info("Successfully auto-fetched token for user: {}", userId);
                return;
            }
        }
        
        logger.warn("No valid token available. Token will need to be fetched manually via /token endpoint");
    }
    
    /**
     * Get the current valid token, auto-refreshing if necessary.
     * @return Optional containing the current token, or empty if no valid token is available
     */
    public Optional<String> getCurrentToken() {
        // Check cache first
        Optional<CachedToken> cached = tokenCache.get();
        if (cached.isPresent() && tokenCache.isValid(cached.get(), TOKEN_REFRESH_BUFFER_MINUTES)) {
            return Optional.of(cached.get().token());
        }
        
        // Try to refresh token
        Optional<String> password = configResolver.resolvePassword();
        Optional<String> proxyUrl = configResolver.resolveProxyUrl();
        
        if (password.isPresent() && proxyUrl.isPresent()) {
            String userId = cached.map(CachedToken::userId).orElse(configResolver.resolveDefaultUserId());
            logger.info("Token expired or missing, attempting to refresh for user: {}", userId);
            
            TokenCreationResponse response = tokenClient.createToken(
                proxyUrl.get(), userId, password.get(), DEFAULT_TOKEN_DURATION_MINUTES
            );
            
            if (response.success()) {
                long expiresAtMillis = response.expiresAtSeconds() * 1000L;
                CachedToken newToken = new CachedToken(response.token(), userId, expiresAtMillis);
                tokenCache.put(newToken);
                logger.info("Successfully refreshed token for user: {}", userId);
                return Optional.of(newToken.token());
            }
        }
        
        logger.warn("Unable to refresh token. No valid token available.");
        return Optional.empty();
    }
    
    /**
     * Manually set a token (e.g., from TokenController).
     */
    public void setToken(String token, String userId) {
        Optional<String> proxyUrl = configResolver.resolveProxyUrl();
        if (proxyUrl.isEmpty()) {
            logger.warn("Cannot validate token: no proxy URL configured");
            return;
        }
        
        TokenValidationResponse validation = tokenClient.validateToken(proxyUrl.get(), token);
        if (validation.valid()) {
            long expiresAtMillis = System.currentTimeMillis() + (validation.minutesRemaining() * 60L * 1000L);
            CachedToken cachedToken = new CachedToken(token, userId, expiresAtMillis);
            tokenCache.put(cachedToken);
            logger.info("Manually set token for user: {}", userId);
        } else {
            logger.warn("Attempted to set invalid token");
        }
    }
    
    /**
     * Get current token status.
     */
    public TokenStatus getTokenStatus() {
        Optional<CachedToken> cached = tokenCache.get();
        if (cached.isEmpty()) {
            return new TokenStatus(false, null, 0, "No token cached");
        }
        
        CachedToken token = cached.get();
        if (!tokenCache.isValid(token, TOKEN_REFRESH_BUFFER_MINUTES)) {
            return new TokenStatus(false, token.userId(), 0, "Token expired");
        }
        
        int minutesRemaining = calculateMinutesRemaining(token.expiresAtMillis());
        return new TokenStatus(true, token.userId(), minutesRemaining, null);
    }
    
    /**
     * Validate a token with the proxy server.
     */
    public TokenValidationResult validateToken(String token) {
        Optional<String> proxyUrl = configResolver.resolveProxyUrl();
        if (proxyUrl.isEmpty()) {
            return new TokenValidationResult(false, "No proxy URL configured", 0, null, null, null);
        }
        
        TokenValidationResponse response = tokenClient.validateToken(proxyUrl.get(), token);
        return new TokenValidationResult(
            response.valid(),
            response.errorMessage(),
            response.minutesRemaining(),
            response.user(),
            response.description(),
            response.expiresAtIso()
        );
    }
    
    // Helper methods
    private int calculateMinutesRemaining(long expiresAtMillis) {
        long millisRemaining = expiresAtMillis - System.currentTimeMillis();
        return (int) Math.max(0, millisRemaining / (60 * 1000));
    }

}
