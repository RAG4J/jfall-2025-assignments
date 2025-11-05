package org.rag4j.tokens.model;

public class TokenCreationResult {
    private final boolean success;
    private final String errorMessage;
    private final String token;
    private final long expiresAt;
    private final int expiresInMinutes;

    public TokenCreationResult(boolean success, String errorMessage, String token,
                               long expiresAt, int expiresInMinutes) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.token = token;
        this.expiresAt = expiresAt;
        this.expiresInMinutes = expiresInMinutes;
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public String getToken() { return token; }
    public long getExpiresAt() { return expiresAt; }
    public int getExpiresInMinutes() { return expiresInMinutes; }
}
