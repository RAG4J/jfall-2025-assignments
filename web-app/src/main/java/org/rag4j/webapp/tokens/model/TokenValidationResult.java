package org.rag4j.webapp.tokens.model;

public class TokenValidationResult {
    private final boolean valid;
    private final String errorMessage;
    private final int minutesRemaining;
    private final String user;
    private final String description;
    private final String expiresAtIso;

    public TokenValidationResult(boolean valid, String errorMessage, int minutesRemaining,
                                 String user, String description, String expiresAtIso) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.minutesRemaining = minutesRemaining;
        this.user = user;
        this.description = description;
        this.expiresAtIso = expiresAtIso;
    }

    public boolean isValid() { return valid; }
    public String getErrorMessage() { return errorMessage; }
    public int getMinutesRemaining() { return minutesRemaining; }
    public String getUser() { return user; }
    public String getDescription() { return description; }
    public String getExpiresAtIso() { return expiresAtIso; }
}
