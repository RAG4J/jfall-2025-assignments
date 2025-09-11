package org.rag4j.evals.exception;

import org.rag4j.evals.service.ProgressTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Global exception handler for AI service and application errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final ProgressTrackingService progressTrackingService;
    
    @Autowired
    public GlobalExceptionHandler(ProgressTrackingService progressTrackingService) {
        this.progressTrackingService = progressTrackingService;
    }
    
    /**
     * Handles general RuntimeExceptions that might be AI service related
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // Check if this looks like a token error
        if (isTokenRelatedError(ex)) {
            logger.error("Token-related error occurred: {}", ex.getMessage(), ex);
            return handleGenericTokenExpiration(ex);
        }
        
        // Check if this looks like an AI service error
        if (isAIServiceError(ex)) {
            logger.error("AI service error occurred: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of(
                            "error", "AI Service Error",
                            "message", "The AI service encountered an error: " + ex.getMessage(),
                            "type", "AI_SERVICE_ERROR"
                    ));
        }
        
        // Let other RuntimeExceptions be handled by default Spring handlers
        throw ex;
    }
    
    /**
     * Handles token expiration exceptions
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpiredException(TokenExpiredException ex) {
        logger.warn("Token expired: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Token Expired",
                        "message", ex.getMessage(),
                        "instructions", ex.getTokenRefreshInstructions(),
                        "type", "TOKEN_EXPIRED"
                ));
    }
    
    /**
     * Handles 401 token errors from generic exceptions
     */
    private ResponseEntity<Map<String, Object>> handleGenericTokenExpiration(RuntimeException ex) {
        String message = "Your authentication token has expired or is invalid";
        String instructions = "To refresh your token:\n" +
                "1. Open the main Thymeleaf Agent web application (usually at http://localhost:8080)\n" +
                "2. Go to the Token Management page\n" +
                "3. Generate a new token or refresh your existing token\n" +
                "4. The new token will be automatically shared with this evaluation application\n" +
                "5. Try running your evaluation again";
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Authentication Token Expired",
                        "message", message,
                        "instructions", instructions,
                        "type", "TOKEN_EXPIRED",
                        "originalError", ex.getMessage()
                ));
    }
    
    /**
     * Updates progress tracking when a token error occurs during run execution
     */
    public void handleTokenErrorInProgress(String runId, Exception ex) {
        if (progressTrackingService.hasProgress(runId)) {
            String errorMessage = "Token expired during execution. Please refresh your token and try again.";
            progressTrackingService.failProgress(runId, errorMessage);
            
            // Also update with detailed instructions
            String instructions = "To refresh your token:\n" +
                    "1. Open the main Thymeleaf Agent app (http://localhost:8080)\n" +
                    "2. Go to Token Management\n" +
                    "3. Generate a new token\n" +
                    "4. Try your evaluation again";
            
            progressTrackingService.updateMessage(runId, "🔐 " + errorMessage + "\n\n" + instructions);
        }
    }
    
    /**
     * Checks if an exception is related to token expiration
     */
    public static boolean isTokenExpiredException(Throwable ex) {
        if (ex instanceof TokenExpiredException) {
            return true;
        }
        
        return isTokenRelatedError(ex);
    }
    
    /**
     * Checks if an exception appears to be token-related based on message content
     */
    private static boolean isTokenRelatedError(Throwable ex) {
        if (ex == null) return false;
        
        String message = ex.getMessage();
        if (message == null) return false;
        
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("401") || 
               lowerMessage.contains("unauthorized") ||
               (lowerMessage.contains("invalid") && lowerMessage.contains("token")) ||
               (lowerMessage.contains("expired") && lowerMessage.contains("token")) ||
               lowerMessage.contains("authentication") ||
               ex.getClass().getSimpleName().toLowerCase().contains("ai") && lowerMessage.contains("401");
    }
    
    /**
     * Checks if an exception appears to be AI service related
     */
    private static boolean isAIServiceError(Throwable ex) {
        if (ex == null) return false;
        
        String className = ex.getClass().getSimpleName().toLowerCase();
        String message = ex.getMessage();
        
        return className.contains("ai") || 
               className.contains("openai") ||
               (message != null && (message.contains("AI service") || message.contains("OpenAI")));
    }
    
    /**
     * Converts generic exception to TokenExpiredException if it's a token error
     */
    public static TokenExpiredException toTokenExpiredException(RuntimeException ex) {
        if (isTokenExpiredException(ex)) {
            return TokenExpiredException.withDefaultInstructions(
                    "Authentication token has expired or is invalid", ex);
        }
        throw new IllegalArgumentException("Exception is not a token expiration error");
    }
}
