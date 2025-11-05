package org.rag4j.tokens.model;

public class TokenStatus {
    private final boolean valid;
    private final String userId;
    private final int minutesRemaining;
    private final String errorMessage;

    public TokenStatus(boolean valid, String userId, int minutesRemaining, String errorMessage) {
        this.valid = valid;
        this.userId = userId;
        this.minutesRemaining = minutesRemaining;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() { return valid; }
    public String getUserId() { return userId; }
    public int getMinutesRemaining() { return minutesRemaining; }
    public String getErrorMessage() { return errorMessage; }
}
