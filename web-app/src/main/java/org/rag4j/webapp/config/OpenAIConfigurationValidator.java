package org.rag4j.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Component that validates OpenAI configuration on application startup.
 * When the 'embabel' profile is active, it checks that environment variables
 * OPENAI_API_KEY and OPENAI_BASE_URL are in sync with application properties.
 */
@Component
public class OpenAIConfigurationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfigurationValidator.class);
    
    private final Environment environment;
    private final Optional<String> openAIProxyToken;
    private final ConfigurationMismatchHandler mismatchHandler;
    
    public OpenAIConfigurationValidator(
            Environment environment,
            @Value("${openai.proxy.token:#{null}}") Optional<String> openAIProxyToken,
            @Value("${openai.proxy.url}") String openAIProxyUrl,
            ConfigurationMismatchHandler mismatchHandler) {
        this.environment = environment;
        this.openAIProxyToken = openAIProxyToken;
        this.mismatchHandler = mismatchHandler;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        // Only validate when embabel profile is active
        if (!Arrays.asList(environment.getActiveProfiles()).contains("embabel")) {
            logger.debug("Skipping OpenAI configuration validation - embabel profile not active");
            return;
        }
        
        logger.info("Validating OpenAI configuration for embabel profile");
        
        // Get environment variables
        String envApiKey = System.getenv("OPENAI_API_KEY");
        String envBaseUrl = System.getenv("OPENAI_BASE_URL");
        
        ValidationResult result = performValidation(envApiKey, envBaseUrl);
        
        if (!result.isValid()) {
            logger.error("OpenAI configuration validation failed: {}", result.getErrorMessage());
            mismatchHandler.handleConfigurationMismatch(result);
        } else {
            logger.info("OpenAI configuration validation passed");
        }
    }
    
    private ValidationResult performValidation(String envApiKey, String envBaseUrl) {
        // Check if OPENAI_API_KEY environment variable is set
        if (envApiKey == null || envApiKey.trim().isEmpty()) {
            return ValidationResult.invalid("Environment variable OPENAI_API_KEY is not set");
        }
        
        // Check if application property openai.proxy.token is set
        if (openAIProxyToken.isEmpty() || openAIProxyToken.get().trim().isEmpty()) {
            return ValidationResult.invalid("Application property openai.proxy.token is not set");
        }
        
        // Check if OPENAI_API_KEY matches openai.proxy.token
        if (!envApiKey.equals(openAIProxyToken.get())) {
            return ValidationResult.invalid(String.format(
                "Environment variable OPENAI_API_KEY does not match application property openai.proxy.token. " +
                "Environment variable starts with: %s..., property starts with: %s...",
                envApiKey.length() > 10 ? envApiKey.substring(0, 10) : envApiKey,
                openAIProxyToken.get().length() > 10 ? openAIProxyToken.get().substring(0, 10) : openAIProxyToken.get()
            ));
        }
        
        // Optional: Check OPENAI_BASE_URL if provided
        if (envBaseUrl == null || envBaseUrl.trim().isEmpty()) {
            // Expected a baseUrl to be set
            return ValidationResult.invalid("Environment variable OPENAI_BASE_URL is not set");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Result of the OpenAI configuration validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
