package org.rag4j.evals.controller;

import org.rag4j.evals.model.EvaluationRun;
import org.rag4j.evals.model.RunConfiguration;
import org.rag4j.evals.service.AccuracyService;
import org.rag4j.evals.service.EvaluationDataService;
import org.rag4j.evals.service.EvaluationRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Validated
@RequestMapping("/runs")
public class RunController {
    
    private static final Logger logger = LoggerFactory.getLogger(RunController.class);
    
    private final EvaluationDataService dataService;
    private final EvaluationRunnerService evaluationRunnerService;
    private final AccuracyService accuracyService;
    
    @Autowired
    public RunController(EvaluationDataService dataService, 
                        EvaluationRunnerService evaluationRunnerService,
                        AccuracyService accuracyService) {
        this.dataService = dataService;
        this.evaluationRunnerService = evaluationRunnerService;
        this.accuracyService = accuracyService;
    }
    
    @GetMapping
    public String runsList(Model model) {
        List<EvaluationRun> runs = dataService.getAllRuns();
        
        // Ensure accuracy is calculated for all runs
        for (EvaluationRun run : runs) {
            if (run.getHumanScoreAccuracy() == null || run.getLlmRatingAccuracy() == null) {
                accuracyService.calculateAndUpdateAccuracy(run.getId());
            }
        }
        
        // Refresh runs list to get updated accuracy values
        runs = dataService.getAllRuns();
        
        model.addAttribute("runs", runs);
        logger.info("Displaying {} evaluation runs", runs.size());
        return "evals/runs-list";
    }
    
    @GetMapping("/new")
    public String newRunForm(Model model) {
        model.addAttribute("run", new EvaluationRun());
        model.addAttribute("configuration", new RunConfiguration());
        model.addAttribute("availableQuestions", evaluationRunnerService.getAvailableQuestionCount());
        model.addAttribute("serviceReady", evaluationRunnerService.isReady());
        return "evals/new-run";
    }
    
    @PostMapping("/new")
    public String createRun(
            @Validated @ModelAttribute("run") EvaluationRun run,
            @ModelAttribute("configuration") RunConfiguration configuration,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Set basic properties
            run.setId(UUID.randomUUID().toString());
            run.setConfiguration(configuration);
            
            // Create the run with questions from input file
            EvaluationRun savedRun = evaluationRunnerService.createRunWithQuestions(run);
            
            logger.info("Created new evaluation run: {} - {} with {} records", 
                       savedRun.getId(), savedRun.getName(), savedRun.getTotalRecords());
            
            String successMessage = String.format("Evaluation run '%s' created successfully with %d questions. You can now execute the run to generate responses.", 
                                                 savedRun.getName(), savedRun.getTotalRecords());
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            
            // Redirect to run details page instead of evaluations
            return "redirect:/runs/" + savedRun.getId();
            
        } catch (Exception e) {
            logger.error("Failed to create evaluation run: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to create evaluation run: " + e.getMessage());
            return "redirect:/runs/new";
        }
    }
    
    @GetMapping("/{id}")
    public String runDetail(@PathVariable String id, Model model) {
        Optional<EvaluationRun> runOpt = dataService.getRunById(id);
        
        if (runOpt.isPresent()) {
            EvaluationRun run = runOpt.get();
            
            // Ensure accuracy is calculated for this run
            if (run.getHumanScoreAccuracy() == null || run.getLlmRatingAccuracy() == null) {
                run = accuracyService.calculateAndUpdateAccuracy(run.getId());
            }
            
            model.addAttribute("run", run);
            
            // Get associated records count
            int recordCount = dataService.getRecordsByRunId(id).size();
            model.addAttribute("recordCount", recordCount);
            
            return "evals/run-detail";
        } else {
            logger.warn("Run not found: {}", id);
            return "redirect:/runs?error=Run not found";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String editRunForm(@PathVariable String id, Model model) {
        Optional<EvaluationRun> runOpt = dataService.getRunById(id);
        
        if (runOpt.isPresent()) {
            EvaluationRun run = runOpt.get();
            model.addAttribute("run", run);
            model.addAttribute("configuration", run.getConfiguration() != null ? run.getConfiguration() : new RunConfiguration());
            return "evals/edit-run";
        } else {
            logger.warn("Run not found for edit: {}", id);
            return "redirect:/runs?error=Run not found";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateRun(
            @PathVariable String id,
            @Validated @ModelAttribute("run") EvaluationRun run,
            @ModelAttribute("configuration") RunConfiguration configuration,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Ensure the ID is set correctly
            run.setId(id);
            run.setConfiguration(configuration);
            
            // Save the updated run
            EvaluationRun updatedRun = dataService.saveRun(run);
            
            logger.info("Updated evaluation run: {} - {}", updatedRun.getId(), updatedRun.getName());
            redirectAttributes.addFlashAttribute("success", "Evaluation run updated successfully");
            
            return "redirect:/runs/" + updatedRun.getId();
            
        } catch (Exception e) {
            logger.error("Failed to update evaluation run {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update evaluation run: " + e.getMessage());
            return "redirect:/runs/" + id + "/edit";
        }
    }
    
    @DeleteMapping("/{id}")
    public String deleteRun(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            dataService.deleteRun(id);
            logger.info("Deleted evaluation run: {}", id);
            redirectAttributes.addFlashAttribute("success", "Evaluation run deleted successfully");
            return "redirect:/runs";
            
        } catch (Exception e) {
            logger.error("Failed to delete evaluation run {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete evaluation run: " + e.getMessage());
            return "redirect:/runs";
        }
    }
    
    
    /**
     * REST API endpoint for executing a run (returns JSON)
     */
    @PostMapping("/{id}/execute")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeRunAPI(@org.hibernate.validator.constraints.UUID @PathVariable String id) {

        try {
            logger.info("Starting execution of evaluation run via API: {}", id);

            // Verify run exists
            Optional<EvaluationRun> runOpt = dataService.getRunById(id);
            if (runOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Run not found",
                                "message", "Evaluation run with ID " + id + " was not found"
                        ));
            }
            
            // Execute the run asynchronously
            evaluationRunnerService.executeRunAsync(id);
            
            logger.info("Successfully started execution of evaluation run: {}", id);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evaluation run execution started successfully",
                    "runId", id,
                    "status", "RUNNING"
            ));
            
        } catch (Exception e) {
            logger.error("Failed to execute evaluation run {}: {}", id, e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", "Execution failed",
                            "message", "Failed to execute evaluation run: " + e.getMessage(),
                            "runId", id
                    ));
        }
    }
    
    /**
     * HTML form endpoint for executing a run (returns redirect) - for traditional form submission
     */
    @PostMapping("/{id}/execute-form")
    public String executeRunForm(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // Execute the run asynchronously
            evaluationRunnerService.executeRunAsync(id);
            
            logger.info("Started execution of evaluation run: {}", id);
            redirectAttributes.addFlashAttribute("success", "Evaluation run execution started in background");
            
            return "redirect:/evaluations?runId=" + id;
            
        } catch (Exception e) {
            logger.error("Failed to execute evaluation run {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to execute evaluation run: " + e.getMessage());
            return "redirect:/runs/" + id;
        }
    }
}
