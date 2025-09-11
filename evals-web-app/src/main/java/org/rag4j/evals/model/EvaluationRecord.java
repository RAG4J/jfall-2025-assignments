package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an evaluation record containing input, response, and scoring information
 */
public class EvaluationRecord {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("runId")
    private String runId;
    
    @JsonProperty("input")
    private String input;
    
    @JsonProperty("response")
    private String response;
    
    @JsonProperty("llmScore")
    private ScoreType llmScore;
    
    @JsonProperty("llmReason")
    private String llmReason;
    
    @JsonProperty("humanScore")
    private ScoreType humanScore;
    
    @JsonProperty("humanReason")
    private String humanReason;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("metadata")
    private EvaluationMetadata metadata;
    
    // Default constructor
    public EvaluationRecord() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Full constructor
    public EvaluationRecord(String id, String runId, String input, String response, 
                          ScoreType llmScore, String llmReason, 
                          ScoreType humanScore, String humanReason) {
        this.id = id;
        this.runId = runId;
        this.input = input;
        this.response = response;
        this.llmScore = llmScore;
        this.llmReason = llmReason;
        this.humanScore = humanScore;
        this.humanReason = humanReason;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public ScoreType getLlmScore() {
        return llmScore;
    }
    
    public void setLlmScore(ScoreType llmScore) {
        this.llmScore = llmScore;
    }
    
    public String getLlmReason() {
        return llmReason;
    }
    
    public void setLlmReason(String llmReason) {
        this.llmReason = llmReason;
    }
    
    public ScoreType getHumanScore() {
        return humanScore;
    }
    
    public void setHumanScore(ScoreType humanScore) {
        this.humanScore = humanScore;
    }
    
    public String getHumanReason() {
        return humanReason;
    }
    
    public void setHumanReason(String humanReason) {
        this.humanReason = humanReason;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public EvaluationMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(EvaluationMetadata metadata) {
        this.metadata = metadata;
    }
    
    // Utility methods
    @JsonIgnore
    public String getShortInput() {
        return truncateText(input, 100);
    }

    @JsonIgnore
    public String getShortResponse() {
        return truncateText(response, 100);
    }

    @JsonIgnore
    public String getShortLlmReason() {
        return truncateText(llmReason, 80);
    }

    @JsonIgnore
    public String getShortHumanReason() {
        return truncateText(humanReason, 80);
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationRecord that = (EvaluationRecord) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "EvaluationRecord{" +
                "id='" + id + '\'' +
                ", runId='" + runId + '\'' +
                ", llmScore=" + llmScore +
                ", humanScore=" + humanScore +
                ", timestamp=" + timestamp +
                '}';
    }
}
