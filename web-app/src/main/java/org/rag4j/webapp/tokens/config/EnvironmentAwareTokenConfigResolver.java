package org.rag4j.webapp.tokens.config;

import org.rag4j.webapp.tokens.TokenConfigResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Configuration resolver that checks both application properties and environment variables.
 * Priority order:
 * 1. Application properties (application.yml)
 * 2. Environment variables
 */
@Component
public class EnvironmentAwareTokenConfigResolver implements TokenConfigResolver {
    
    private final String configuredProxyUrl;
    private final Optional<String> configuredToken;
    private final Optional<String> configuredPassword;
    private final String defaultUserId;
    
    public EnvironmentAwareTokenConfigResolver(
            @Value("${openai.proxy.url}") String proxyUrl,
            @Value("${openai.proxy.token:#{null}}") Optional<String> token,
            @Value("${openai.proxy.password:#{null}}") Optional<String> password,
            @Value("${openai.proxy.user-id:workshop-default}") String userId
    ) {
        this.configuredProxyUrl = proxyUrl;
        this.configuredToken = token;
        this.configuredPassword = password;
        this.defaultUserId = userId;
    }
    
    @Override
    public Optional<String> resolveProxyUrl() {
        // Priority 1: Application configuration
        if (configuredProxyUrl != null && !configuredProxyUrl.trim().isEmpty()) {
            return Optional.of(configuredProxyUrl);
        }
        
        // Priority 2: OPENAI_BASE_URL environment variable
        String envUrl = System.getenv("OPENAI_BASE_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return Optional.of(envUrl);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<String> resolveExistingToken() {
        // Priority 1: Application configuration
        if (configuredToken.isPresent() && !configuredToken.get().trim().isEmpty()) {
            return configuredToken;
        }
        
        // Priority 2: OPENAI_API_KEY environment variable
        // Note: This is typically used for direct OpenAI or manually managed proxy tokens
        String envToken = System.getenv("OPENAI_API_KEY");
        if (envToken != null && !envToken.trim().isEmpty()) {
            return Optional.of(envToken);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<String> resolvePassword() {
        return configuredPassword.filter(s -> !s.trim().isEmpty());
    }
    
    @Override
    public String resolveDefaultUserId() {
        return defaultUserId;
    }
}
