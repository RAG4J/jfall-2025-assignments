package org.rag4j.agent.core;

public enum Sender {
    SYSTEM("System"),
    USER("User"),
    ASSISTANT("Assistant"),
    OBSERVATION("Observation");

    private final String displayName;

    Sender(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String toString() {
        return displayName;
    }

    public static Sender from(String displayName) {
        for (Sender sender : Sender.values()) {
            if (sender.displayName.equalsIgnoreCase(displayName)) {
                return sender;
            }
        }
        throw new IllegalArgumentException("Unknown sender: " + displayName);
    }
}
