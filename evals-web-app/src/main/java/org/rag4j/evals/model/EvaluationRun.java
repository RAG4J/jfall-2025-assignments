package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an evaluation run containing multiple evaluation records
 */
public class EvaluationRun {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("status")
    private RunStatus status;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("completedAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    @JsonProperty("totalRecords")
    private int totalRecords;
    
    @JsonProperty("completedRecords")
    private int completedRecords;
    
    @JsonProperty("configuration")
    private RunConfiguration configuration;
    
    @JsonProperty("humanScoreAccuracy")
    private Double humanScoreAccuracy;
    
    @JsonProperty("llmRatingAccuracy")
    private Double llmRatingAccuracy;
    
    // Default constructor
    public EvaluationRun() {
        this.createdAt = LocalDateTime.now();
        this.status = RunStatus.CREATED;
    }
    
    // Constructor
    public EvaluationRun(String id, String name, String description) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RunStatus getStatus() {
        return status;
    }
    
    public void setStatus(RunStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public int getCompletedRecords() {
        return completedRecords;
    }
    
    public void setCompletedRecords(int completedRecords) {
        this.completedRecords = completedRecords;
    }
    
    public RunConfiguration getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(RunConfiguration configuration) {
        this.configuration = configuration;
    }
    
    public Double getHumanScoreAccuracy() {
        return humanScoreAccuracy;
    }
    
    public void setHumanScoreAccuracy(Double humanScoreAccuracy) {
        this.humanScoreAccuracy = humanScoreAccuracy;
    }
    
    public Double getLlmRatingAccuracy() {
        return llmRatingAccuracy;
    }
    
    public void setLlmRatingAccuracy(Double llmRatingAccuracy) {
        this.llmRatingAccuracy = llmRatingAccuracy;
    }
    
    // Utility methods
    @JsonIgnore
    public double getCompletionPercentage() {
        if (totalRecords == 0) return 0.0;
        return (double) completedRecords / totalRecords * 100.0;
    }

    @JsonIgnore
    public String getStatusBadgeClass() {
        if (status == null) return "badge bg-secondary";
        return switch (status) {
            case CREATED -> "badge bg-primary";
            case RUNNING -> "badge bg-warning";
            case COMPLETED -> "badge bg-success";
            case FAILED -> "badge bg-danger";
            case CANCELLED -> "badge bg-secondary";
        };
    }
    
    @JsonIgnore
    public String getFormattedHumanScoreAccuracy() {
        if (humanScoreAccuracy == null) return "N/A";
        return String.format("%.1f%%", humanScoreAccuracy * 100);
    }
    
    @JsonIgnore
    public String getFormattedLlmRatingAccuracy() {
        if (llmRatingAccuracy == null) return "N/A";
        return String.format("%.1f%%", llmRatingAccuracy * 100);
    }
    
    @JsonIgnore
    public String getHumanAccuracyBadgeClass() {
        if (humanScoreAccuracy == null) return "badge bg-secondary";
        if (humanScoreAccuracy >= 0.8) return "badge bg-success";
        if (humanScoreAccuracy >= 0.6) return "badge bg-warning";
        return "badge bg-danger";
    }
    
    @JsonIgnore
    public String getLlmAccuracyBadgeClass() {
        if (llmRatingAccuracy == null) return "badge bg-secondary";
        if (llmRatingAccuracy >= 0.8) return "badge bg-success";
        if (llmRatingAccuracy >= 0.6) return "badge bg-warning";
        return "badge bg-danger";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationRun that = (EvaluationRun) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "EvaluationRun{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", totalRecords=" + totalRecords +
                ", completedRecords=" + completedRecords +
                '}';
    }
}

