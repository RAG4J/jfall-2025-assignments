package org.rag4j.evals.controller;

import org.rag4j.evals.model.ExecutionProgress;
import org.rag4j.evals.service.ProgressTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for progress tracking operations
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressTrackingService progressTrackingService;

    @Autowired
    public ProgressController(ProgressTrackingService progressTrackingService) {
        this.progressTrackingService = progressTrackingService;
    }

    /**
     * Get progress for a specific run
     */
    @GetMapping("/{runId}")
    public ResponseEntity<ExecutionProgress> getProgress(@PathVariable String runId) {
        Optional<ExecutionProgress> progress = progressTrackingService.getProgress(runId);
        return progress.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all current progress tracking
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, ExecutionProgress>> getAllProgress() {
        Map<String, ExecutionProgress> allProgress = progressTrackingService.getAllProgress();
        return ResponseEntity.ok(allProgress);
    }

    /**
     * Get progress statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getProgressStatistics() {
        Map<String, Object> statistics = progressTrackingService.getProgressStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Check if progress tracking exists for a run
     */
    @GetMapping("/{runId}/exists")
    public ResponseEntity<Map<String, Boolean>> hasProgress(@PathVariable String runId) {
        boolean exists = progressTrackingService.hasProgress(runId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Remove progress tracking for a run (cleanup endpoint)
     */
    @DeleteMapping("/{runId}")
    public ResponseEntity<Map<String, String>> removeProgress(@PathVariable String runId) {
        progressTrackingService.removeProgress(runId);
        return ResponseEntity.ok(Map.of("message", "Progress tracking removed for run: " + runId));
    }

    /**
     * Update progress message for a run
     */
    @PutMapping("/{runId}/message")
    public ResponseEntity<Map<String, String>> updateMessage(
            @PathVariable String runId, 
            @RequestBody Map<String, String> request) {
        
        String message = request.get("message");
        if (message == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message is required"));
        }

        progressTrackingService.updateMessage(runId, message);
        return ResponseEntity.ok(Map.of("message", "Progress message updated"));
    }
}
