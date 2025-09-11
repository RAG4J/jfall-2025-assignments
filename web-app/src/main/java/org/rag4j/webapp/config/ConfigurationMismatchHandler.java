package org.rag4j.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Handler for OpenAI configuration mismatch events.
 * This component stores the configuration error state and provides
 * access to error information for controllers to display to users.
 */
@Component
public class ConfigurationMismatchHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationMismatchHandler.class);
    
    private final AtomicReference<OpenAIConfigurationValidator.ValidationResult> mismatchError = 
            new AtomicReference<>();
    
    /**
     * Handle a configuration mismatch by storing the error for later retrieval
     */
    public void handleConfigurationMismatch(OpenAIConfigurationValidator.ValidationResult validationResult) {
        logger.warn("Storing configuration mismatch error: {}", validationResult.getErrorMessage());
        mismatchError.set(validationResult);
    }
    
    /**
     * Check if there is a configuration mismatch error
     */
    public boolean hasConfigurationMismatch() {
        OpenAIConfigurationValidator.ValidationResult result = mismatchError.get();
        return result != null && !result.isValid();
    }
    
    /**
     * Get the configuration mismatch error message
     */
    public String getConfigurationMismatchError() {
        OpenAIConfigurationValidator.ValidationResult result = mismatchError.get();
        return result != null ? result.getErrorMessage() : null;
    }
    
    /**
     * Clear the configuration mismatch error
     * This should be called after the user has been notified of the error
     */
    public void clearConfigurationMismatchError() {
        logger.debug("Clearing configuration mismatch error");
        mismatchError.set(null);
    }
    
    /**
     * Get the full validation result
     */
    public OpenAIConfigurationValidator.ValidationResult getValidationResult() {
        return mismatchError.get();
    }
}
