package org.rag4j.evals.exception;

/**
 * Exception thrown when the AI service token has expired or is invalid
 */
public class TokenExpiredException extends RuntimeException {
    
    private final String tokenRefreshInstructions;
    
    public TokenExpiredException(String message, String tokenRefreshInstructions) {
        super(message);
        this.tokenRefreshInstructions = tokenRefreshInstructions;
    }
    
    public TokenExpiredException(String message, String tokenRefreshInstructions, Throwable cause) {
        super(message, cause);
        this.tokenRefreshInstructions = tokenRefreshInstructions;
    }
    
    public String getTokenRefreshInstructions() {
        return tokenRefreshInstructions;
    }
    
    /**
     * Creates a standard token expiration exception with default instructions
     */
    public static TokenExpiredException withDefaultInstructions(String message, Throwable cause) {
        String instructions = "To refresh your token:\n" +
                "1. Open the main Thymeleaf Agent web application (usually at http://localhost:8080)\n" +
                "2. Go to the Token Management page\n" +
                "3. Generate a new token or refresh your existing token\n" +
                "4. The new token will be automatically shared with this evaluation application\n" +
                "5. Try running your evaluation again";
        
        return new TokenExpiredException(message, instructions, cause);
    }
}
