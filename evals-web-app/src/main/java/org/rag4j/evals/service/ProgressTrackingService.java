package org.rag4j.evals.service;

import org.rag4j.evals.model.ExecutionProgress;
import org.rag4j.evals.model.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking and broadcasting execution progress
 */
@Service
public class ProgressTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProgressTrackingService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ExecutionProgress> progressMap = new ConcurrentHashMap<>();
    
    @Autowired
    public ProgressTrackingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Starts tracking progress for a run
     */
    public void startProgress(String runId, int totalSteps) {
        ExecutionProgress progress = new ExecutionProgress(runId, totalSteps);
        progressMap.put(runId, progress);
        
        logger.info("Started progress tracking for run {} with {} steps", runId, totalSteps);
        broadcastProgress(progress);
    }
    
    /**
     * Updates the current step and broadcasts the progress
     */
    public void updateProgress(String runId, int currentStep, String currentQuestion) {
        ExecutionProgress progress = progressMap.get(runId);
        if (progress != null) {
            progress.setCurrentStep(currentStep);
            progress.setCurrentQuestion(currentQuestion);
            progress.setStatus(ExecutionStatus.RUNNING);
            
            // Update message with current progress
            progress.setMessage(String.format("Processing question %d of %d: %s", 
                               currentStep, progress.getTotalSteps(), 
                               truncateQuestion(currentQuestion)));
            
            progress.updateEstimatedTime();
            
            logger.debug("Updated progress for run {}: {}/{} ({}%)", 
                        runId, currentStep, progress.getTotalSteps(), 
                        String.format("%.1f", progress.getPercentage()));
            
            broadcastProgress(progress);
        } else {
            logger.warn("No progress tracking found for run: {}", runId);
        }
    }
    
    /**
     * Increments the current step and broadcasts the progress
     */
    public void incrementProgress(String runId, String currentQuestion) {
        ExecutionProgress progress = progressMap.get(runId);
        if (progress != null) {
            progress.incrementStep(currentQuestion);
            progress.setStatus(ExecutionStatus.RUNNING);
            
            progress.setMessage(String.format("Processing question %d of %d: %s", 
                               progress.getCurrentStep(), progress.getTotalSteps(), 
                               truncateQuestion(currentQuestion)));
            
            progress.updateEstimatedTime();
            
            logger.debug("Incremented progress for run {}: {}/{} ({}%)", 
                        runId, progress.getCurrentStep(), progress.getTotalSteps(), 
                        String.format("%.1f", progress.getPercentage()));
            
            broadcastProgress(progress);
        }
    }
    
    /**
     * Marks the execution as completed
     */
    public void completeProgress(String runId, String message) {
        ExecutionProgress progress = progressMap.get(runId);
        if (progress != null) {
            progress.setStatus(ExecutionStatus.COMPLETED);
            progress.setCurrentStep(progress.getTotalSteps());
            progress.setMessage(message != null ? message : "Execution completed successfully");
            progress.setEstimatedTimeRemainingSeconds(0L);
            
            logger.info("Completed progress tracking for run {}: {}", runId, message);
            broadcastProgress(progress);
            
            // Keep completed progress for a while, then clean up
            cleanupProgressAfterDelay(runId, 300000); // 5 minutes
        }
    }
    
    /**
     * Marks the execution as failed
     */
    public void failProgress(String runId, String errorMessage) {
        ExecutionProgress progress = progressMap.get(runId);
        if (progress != null) {
            progress.setStatus(ExecutionStatus.FAILED);
            progress.setMessage(errorMessage != null ? errorMessage : "Execution failed");
            progress.setEstimatedTimeRemainingSeconds(null);
            
            logger.error("Failed progress tracking for run {}: {}", runId, errorMessage);
            broadcastProgress(progress);
            
            // Keep failed progress for a while, then clean up
            cleanupProgressAfterDelay(runId, 300000); // 5 minutes
        }
    }
    
    /**
     * Updates the progress message without changing step count
     */
    public void updateMessage(String runId, String message) {
        ExecutionProgress progress = progressMap.get(runId);
        if (progress != null) {
            progress.setMessage(message);
            broadcastProgress(progress);
        }
    }
    
    /**
     * Gets the current progress for a run
     */
    public Optional<ExecutionProgress> getProgress(String runId) {
        return Optional.ofNullable(progressMap.get(runId));
    }
    
    /**
     * Checks if progress tracking exists for a run
     */
    public boolean hasProgress(String runId) {
        return progressMap.containsKey(runId);
    }
    
    /**
     * Removes progress tracking for a run
     */
    public void removeProgress(String runId) {
        ExecutionProgress removed = progressMap.remove(runId);
        if (removed != null) {
            logger.info("Removed progress tracking for run: {}", runId);
        }
    }
    
    /**
     * Gets all current progress tracking
     */
    public Map<String, ExecutionProgress> getAllProgress() {
        return Map.copyOf(progressMap);
    }
    
    /**
     * Broadcasts progress update via WebSocket
     */
    private void broadcastProgress(ExecutionProgress progress) {
        try {
            // Send to specific run topic
            messagingTemplate.convertAndSend("/topic/progress/" + progress.getRunId(), progress);
            
            // Also send to general progress topic for dashboard updates
            messagingTemplate.convertAndSend("/topic/progress", progress);
            
            logger.trace("Broadcasted progress for run {}: {}%", 
                        progress.getRunId(), String.format("%.1f", progress.getPercentage()));
            
        } catch (Exception e) {
            logger.error("Failed to broadcast progress for run {}: {}", 
                        progress.getRunId(), e.getMessage(), e);
        }
    }
    
    /**
     * Truncates question text for display in progress messages
     */
    private String truncateQuestion(String question) {
        if (question == null) return "";
        if (question.length() <= 60) return question;
        return question.substring(0, 57) + "...";
    }
    
    /**
     * Schedules cleanup of progress data after a delay
     */
    private void cleanupProgressAfterDelay(String runId, long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                removeProgress(runId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Progress cleanup interrupted for run: {}", runId);
            }
        }).start();
    }
    
    /**
     * Gets statistics about current progress tracking
     */
    public Map<String, Object> getProgressStatistics() {
        long runningCount = progressMap.values().stream()
                .mapToLong(p -> p.isRunning() ? 1 : 0)
                .sum();
        
        long completedCount = progressMap.values().stream()
                .mapToLong(p -> p.isCompleted() ? 1 : 0)
                .sum();
        
        long failedCount = progressMap.values().stream()
                .mapToLong(p -> p.isFailed() ? 1 : 0)
                .sum();
        
        return Map.of(
            "total", progressMap.size(),
            "running", runningCount,
            "completed", completedCount,
            "failed", failedCount
        );
    }
}
