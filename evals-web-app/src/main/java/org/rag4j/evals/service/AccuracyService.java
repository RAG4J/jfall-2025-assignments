package org.rag4j.evals.service;

import org.rag4j.evals.model.EvaluationRecord;
import org.rag4j.evals.model.EvaluationRun;
import org.rag4j.evals.model.ScoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for calculating accuracy metrics for evaluation runs
 */
@Service
public class AccuracyService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccuracyService.class);
    
    private final EvaluationDataService dataService;
    
    @Autowired
    public AccuracyService(EvaluationDataService dataService) {
        this.dataService = dataService;
    }
    
    /**
     * Calculates and updates accuracy scores for a specific run
     * @param runId The run ID to calculate accuracy for
     * @return The updated EvaluationRun with accuracy scores, or null if run not found
     */
    public EvaluationRun calculateAndUpdateAccuracy(String runId) {
        logger.debug("Calculating accuracy for run: {}", runId);
        
        EvaluationRun run = dataService.getRunById(runId).orElse(null);
        if (run == null) {
            logger.warn("Run not found for accuracy calculation: {}", runId);
            return null;
        }
        
        List<EvaluationRecord> records = dataService.getRecordsByRunId(runId);
        
        if (records.isEmpty()) {
            logger.info("No records found for run {}, setting accuracy to null", runId);
            run.setHumanScoreAccuracy(null);
            run.setLlmRatingAccuracy(null);
        } else {
            AccuracyResult accuracyResult = calculateAccuracy(records);
            run.setHumanScoreAccuracy(accuracyResult.humanScoreAccuracy);
            run.setLlmRatingAccuracy(accuracyResult.llmRatingAccuracy);
            
            logger.info("Calculated accuracy for run {}: Human Score: {:.2f}%, LLM Rating: {:.2f}%", 
                       runId, 
                       accuracyResult.humanScoreAccuracy != null ? accuracyResult.humanScoreAccuracy * 100 : null,
                       accuracyResult.llmRatingAccuracy != null ? accuracyResult.llmRatingAccuracy * 100 : null);
        }
        
        // Save the updated run
        dataService.saveRun(run);
        return run;
    }
    
    /**
     * Calculates accuracy metrics for all runs
     */
    public void calculateAndUpdateAccuracyForAllRuns() {
        logger.info("Calculating accuracy for all runs");
        
        List<EvaluationRun> runs = dataService.getAllRuns();
        for (EvaluationRun run : runs) {
            calculateAndUpdateAccuracy(run.getId());
        }
        
        logger.info("Completed accuracy calculation for {} runs", runs.size());
    }
    
    /**
     * Calculates accuracy scores for a list of evaluation records
     * @param records The records to calculate accuracy for
     * @return AccuracyResult containing both human and LLM rating accuracy
     */
    private AccuracyResult calculateAccuracy(List<EvaluationRecord> records) {
        if (records == null || records.isEmpty()) {
            return new AccuracyResult(null, null, 0);
        }
        
        int totalRecords = records.size();
        int humanGoodCount = 0;
        int llmRatingMatchCount = 0;
        int recordsWithBothScores = 0;
        int recordsWithHumanScores = 0;
        
        for (EvaluationRecord record : records) {
            ScoreType humanScore = record.getHumanScore();
            ScoreType llmScore = record.getLlmScore();
            
            // Count for human score accuracy (Good = 1, others = 0)
            if (humanScore != null) {
                recordsWithHumanScores++;
                if (humanScore == ScoreType.GOOD) {
                    humanGoodCount++;
                }
            }
            
            // Count for LLM rating accuracy (both same = 1, different = 0)
            if (humanScore != null && llmScore != null) {
                recordsWithBothScores++;
                if (humanScore == llmScore) {
                    llmRatingMatchCount++;
                }
            }
        }
        
        Double humanScoreAccuracy = null;
        Double llmRatingAccuracy = null;
        
        // Calculate human score accuracy
        if (recordsWithHumanScores > 0) {
            humanScoreAccuracy = (double) humanGoodCount / recordsWithHumanScores;
        }
        
        // Calculate LLM rating accuracy
        if (recordsWithBothScores > 0) {
            llmRatingAccuracy = (double) llmRatingMatchCount / recordsWithBothScores;
        }
        
        logger.debug("Accuracy calculation: {} total records, {} with human scores, {} with both scores, " +
                    "human good: {}, LLM matches: {}", 
                    totalRecords, recordsWithHumanScores, recordsWithBothScores, 
                    humanGoodCount, llmRatingMatchCount);
        
        return new AccuracyResult(humanScoreAccuracy, llmRatingAccuracy, totalRecords);
    }
    
    /**
     * Result class for accuracy calculations
     */
    private static class AccuracyResult {
        public final Double humanScoreAccuracy;
        public final Double llmRatingAccuracy;
        public final int totalRecords;
        
        public AccuracyResult(Double humanScoreAccuracy, Double llmRatingAccuracy, int totalRecords) {
            this.humanScoreAccuracy = humanScoreAccuracy;
            this.llmRatingAccuracy = llmRatingAccuracy;
            this.totalRecords = totalRecords;
        }
    }
}
