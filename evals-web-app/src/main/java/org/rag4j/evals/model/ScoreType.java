package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Enum representing the evaluation score types
 */
public enum ScoreType {
    GOOD("Good"),
    BAD("Bad"),
    @JsonEnumDefaultValue
    UNKNOWN("Unknown");

    private final String displayValue;

    ScoreType(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonCreator
    public static ScoreType fromJson(String value) {
        if (value == null) return UNKNOWN;
        for (ScoreType t : values()) {
            if (t.name().equalsIgnoreCase(value) || t.displayValue.equalsIgnoreCase(value)) {
                return t;
            }
        }
        return UNKNOWN;
    }
    
    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }
    
    public String getCssClass() {
        return switch (this) {
            case GOOD -> "text-success";
            case BAD -> "text-danger";
            case UNKNOWN -> "text-secondary";
        };
    }
    
    public String getBadgeClass() {
        return switch (this) {
            case GOOD -> "badge bg-success";
            case BAD -> "badge bg-danger";
            case UNKNOWN -> "badge bg-secondary";
        };
    }
    
    public static ScoreType fromString(String value) {
        if (value == null) return UNKNOWN;
        return switch (value.toLowerCase()) {
            case "good" -> GOOD;
            case "bad" -> BAD;
            default -> UNKNOWN;
        };
    }
    
    @Override
    public String toString() {
        return displayValue;
    }
}
