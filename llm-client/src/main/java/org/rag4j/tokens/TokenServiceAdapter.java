package org.rag4j.tokens;

import org.rag4j.agent.core.TokenProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that exposes TokenService as a TokenProvider for agent modules.
 * This allows agent configurations to depend on the core-agent interface
 * while using the web-app TokenService implementation.
 */
@Component
public class TokenServiceAdapter implements TokenProvider {
    private final TokenService tokenService;
    
    public TokenServiceAdapter(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    @Override
    public Optional<String> getCurrentToken() {
        return tokenService.getCurrentToken();
    }
}
