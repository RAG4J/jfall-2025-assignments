package org.rag4j.evals.controller;

import org.rag4j.evals.model.EvaluationRecord;
import org.rag4j.evals.model.EvaluationRun;
import org.rag4j.evals.model.ScoreType;
import org.rag4j.evals.service.AccuracyService;
import org.rag4j.evals.service.EvaluationDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/evaluations")
public class EvaluationController {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);
    
    private final EvaluationDataService dataService;
    private final AccuracyService accuracyService;
    
    @Autowired
    public EvaluationController(EvaluationDataService dataService, AccuracyService accuracyService) {
        this.dataService = dataService;
        this.accuracyService = accuracyService;
    }
    
    @GetMapping
    public String evaluationsTable(
            @RequestParam(value = "runId", required = false) String runId,
            Model model) {
        
        List<EvaluationRecord> records;
        List<EvaluationRun> runs = dataService.getAllRuns();
        
        if (runId != null && !runId.isEmpty()) {
            records = dataService.getRecordsByRunId(runId);
            model.addAttribute("selectedRunId", runId);
            
            // Find the selected run for display
            Optional<EvaluationRun> selectedRun = runs.stream()
                    .filter(run -> run.getId().equals(runId))
                    .findFirst();
            selectedRun.ifPresent(run -> model.addAttribute("selectedRun", run));
        } else {
            records = dataService.getAllRecords();
        }
        
        model.addAttribute("records", records);
        model.addAttribute("runs", runs);
        model.addAttribute("scoreTypes", ScoreType.values());
        
        logger.info("Displaying {} evaluation records", records.size());
        return "evals/evaluations-table";
    }
    
    @GetMapping("/{id}/detail")
    public String recordDetail(@PathVariable String id, Model model) {
        Optional<EvaluationRecord> recordOpt = dataService.getRecordById(id);
        
        if (recordOpt.isPresent()) {
            model.addAttribute("record", recordOpt.get());
            return "evals/record-detail";
        } else {
            logger.warn("Record not found: {}", id);
            return "redirect:/evaluations?error=Record not found";
        }
    }
    
    @PostMapping("/{id}/human-score")
    public String updateHumanScore(
            @PathVariable String id,
            @RequestParam("score") String scoreStr,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {
        
        try {
            ScoreType score = ScoreType.fromString(scoreStr);
            EvaluationRecord updatedRecord = dataService.updateHumanScore(id, score, reason);
            
            // Recalculate accuracy for the run after updating human score
            accuracyService.calculateAndUpdateAccuracy(updatedRecord.getRunId());
            
            logger.info("Updated human score for record {}: {} - {}", id, score, reason);
            redirectAttributes.addFlashAttribute("success", "Human score updated successfully");
            
            return "redirect:/evaluations?runId=" + updatedRecord.getRunId();
            
        } catch (Exception e) {
            logger.error("Failed to update human score for record {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update human score: " + e.getMessage());
            return "redirect:/evaluations";
        }
    }
    
    @GetMapping("/{id}/field/{fieldName}")
    @ResponseBody
    public String getFieldContent(@PathVariable String id, @PathVariable String fieldName) {
        Optional<EvaluationRecord> recordOpt = dataService.getRecordById(id);
        
        if (recordOpt.isEmpty()) {
            return "Record not found";
        }
        
        EvaluationRecord record = recordOpt.get();
        return switch (fieldName.toLowerCase()) {
            case "input" -> record.getInput() != null ? record.getInput() : "";
            case "response" -> record.getResponse() != null ? record.getResponse() : "";
            case "llmreason" -> record.getLlmReason() != null ? record.getLlmReason() : "";
            case "humanreason" -> record.getHumanReason() != null ? record.getHumanReason() : "";
            default -> "Unknown field";
        };
    }
    
    @DeleteMapping("/{id}")
    public String deleteRecord(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<EvaluationRecord> recordOpt = dataService.getRecordById(id);
            String runId = recordOpt.map(EvaluationRecord::getRunId).orElse(null);
            
            dataService.deleteRecord(id);
            
            // Recalculate accuracy for the run after deleting a record
            if (runId != null) {
                accuracyService.calculateAndUpdateAccuracy(runId);
            }
            
            logger.info("Deleted evaluation record: {}", id);
            redirectAttributes.addFlashAttribute("success", "Record deleted successfully");
            
            if (runId != null) {
                return "redirect:/evaluations?runId=" + runId;
            }
            return "redirect:/evaluations";
            
        } catch (Exception e) {
            logger.error("Failed to delete record {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete record: " + e.getMessage());
            return "redirect:/evaluations";
        }
    }
}
