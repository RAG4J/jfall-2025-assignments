package org.rag4j.agent.mcp.model;

public class FavouriteRepositoryException extends RuntimeException {
    public FavouriteRepositoryException(String message) {
        super(message);
    }

    public FavouriteRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
