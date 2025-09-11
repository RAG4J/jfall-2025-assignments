package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the execution progress of an evaluation run
 */
public class ExecutionProgress {
    
    @JsonProperty("runId")
    private String runId;
    
    @JsonProperty("status")
    private ExecutionStatus status;
    
    @JsonProperty("currentStep")
    private int currentStep;
    
    @JsonProperty("totalSteps")
    private int totalSteps;
    
    @JsonProperty("percentage")
    private double percentage;
    
    @JsonProperty("currentQuestion")
    private String currentQuestion;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonProperty("lastUpdate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdate;
    
    @JsonProperty("estimatedTimeRemaining")
    private Long estimatedTimeRemainingSeconds;
    
    // Default constructor
    public ExecutionProgress() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    // Constructor
    public ExecutionProgress(String runId, int totalSteps) {
        this();
        this.runId = runId;
        this.totalSteps = totalSteps;
        this.currentStep = 0;
        this.percentage = 0.0;
        this.status = ExecutionStatus.STARTING;
        this.startTime = LocalDateTime.now();
        this.message = "Initializing execution...";
    }
    
    // Getters and Setters
    public String getRunId() {
        return runId;
    }
    
    public void setRunId(String runId) {
        this.runId = runId;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
        this.lastUpdate = LocalDateTime.now();
    }
    
    public int getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        updatePercentage();
        this.lastUpdate = LocalDateTime.now();
    }
    
    public int getTotalSteps() {
        return totalSteps;
    }
    
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        updatePercentage();
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public String getCurrentQuestion() {
        return currentQuestion;
    }
    
    public void setCurrentQuestion(String currentQuestion) {
        this.currentQuestion = currentQuestion;
        this.lastUpdate = LocalDateTime.now();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
        this.lastUpdate = LocalDateTime.now();
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public Long getEstimatedTimeRemainingSeconds() {
        return estimatedTimeRemainingSeconds;
    }
    
    public void setEstimatedTimeRemainingSeconds(Long estimatedTimeRemainingSeconds) {
        this.estimatedTimeRemainingSeconds = estimatedTimeRemainingSeconds;
    }
    
    // Utility methods
    private void updatePercentage() {
        if (totalSteps > 0) {
            this.percentage = ((double) currentStep / totalSteps) * 100.0;
        } else {
            this.percentage = 0.0;
        }
    }
    
    public void incrementStep() {
        setCurrentStep(currentStep + 1);
    }
    
    public void incrementStep(String questionText) {
        incrementStep();
        setCurrentQuestion(questionText);
    }
    
    public boolean isCompleted() {
        return status == ExecutionStatus.COMPLETED;
    }
    
    public boolean isRunning() {
        return status == ExecutionStatus.RUNNING;
    }
    
    public boolean isFailed() {
        return status == ExecutionStatus.FAILED;
    }
    
    public void updateEstimatedTime() {
        if (startTime != null && currentStep > 0 && status == ExecutionStatus.RUNNING) {
            long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
            if (elapsedSeconds > 0) {
                double avgTimePerStep = (double) elapsedSeconds / currentStep;
                int remainingSteps = totalSteps - currentStep;
                this.estimatedTimeRemainingSeconds = Math.round(avgTimePerStep * remainingSteps);
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionProgress that = (ExecutionProgress) o;
        return Objects.equals(runId, that.runId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(runId);
    }
    
    @Override
    public String toString() {
        return "ExecutionProgress{" +
                "runId='" + runId + '\'' +
                ", status=" + status +
                ", currentStep=" + currentStep +
                ", totalSteps=" + totalSteps +
                ", percentage=" + percentage +
                ", message='" + message + '\'' +
                '}';
    }
}
