package org.rag4j.evals.service;

import org.rag4j.evals.exception.GlobalExceptionHandler;
import org.rag4j.evals.exception.TokenExpiredException;
import org.rag4j.evals.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for creating and executing evaluation runs with questions and agent responses
 */
@Service
public class EvaluationRunnerService {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationRunnerService.class);

    private final QuestionLoaderService questionLoaderService;
    private final AgentRunner agentRunner;
    private final EvaluationDataService evaluationDataService;
    private final ProgressTrackingService progressTrackingService;
    private final LLMScoreService llmScoreService;

    @Autowired
    public EvaluationRunnerService(QuestionLoaderService questionLoaderService,
                                   AgentRunner agentRunner,
                                   EvaluationDataService evaluationDataService,
                                   ProgressTrackingService progressTrackingService,
                                   LLMScoreService llmScoreService) {
        this.questionLoaderService = questionLoaderService;
        this.agentRunner = agentRunner;
        this.evaluationDataService = evaluationDataService;
        this.progressTrackingService = progressTrackingService;
        this.llmScoreService = llmScoreService;
    }

    /**
     * Creates a new evaluation run with records generated from input questions
     *
     * @param run The evaluation run to create
     * @return The created run with updated statistics
     * @throws IOException if questions cannot be loaded
     */
    public EvaluationRun createRunWithQuestions(EvaluationRun run) throws IOException {
        logger.info("Creating evaluation run with questions: {}", run.getName());

        // Load questions from the input file
        List<String> questions = questionLoaderService.loadQuestionStrings();
        logger.info("Loaded {} questions for run {}", questions.size(), run.getId());

        // Update run statistics
        run.setTotalRecords(questions.size());
        run.setCompletedRecords(0);
        run.setStatus(RunStatus.CREATED);

        // Save the run first
        EvaluationRun savedRun = evaluationDataService.saveRun(run);

        // Create evaluation records for each question
        List<EvaluationRecord> records = new ArrayList<>();
        for (String question : questions) {
            EvaluationRecord record = createEvaluationRecord(question, savedRun.getId());
            records.add(record);
        }

        // Save all records
        for (EvaluationRecord record : records) {
            evaluationDataService.saveRecord(record);
        }

        logger.info("Created {} evaluation records for run {}", records.size(), savedRun.getId());
        return savedRun;
    }

    /**
     * Executes an evaluation run by generating agent responses for all questions
     * This method runs synchronously and may take time depending on the number of questions
     *
     * @param runId The ID of the run to execute
     * @return The updated run with completion status
     */
    public EvaluationRun executeRun(String runId) {
        logger.info("Executing evaluation run: {}", runId);

        EvaluationRun run = evaluationDataService.getRunById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));

        // Update run status
        run.setStatus(RunStatus.RUNNING);
        evaluationDataService.saveRun(run);

        try {
            // Get all records for this run
            List<EvaluationRecord> records = evaluationDataService.getRecordsByRunId(runId);
            logger.info("Found {} records to process for run {}", records.size(), runId);

            // Initialize progress tracking
            progressTrackingService.startProgress(runId, records.size());

            int completed = 0;
            for (EvaluationRecord record : records) {
                try {
                    // Update progress with current question
                    progressTrackingService.updateProgress(runId, completed + 1, record.getInput());

                    // Skip if already has a response
                    if (record.getResponse() == null || record.getResponse().isEmpty()) {
                        // Generate response using AgentRunner
                        long startTime = System.currentTimeMillis();
                        String response = agentRunner.generateResponse(record.getInput());
                        long processingTime = System.currentTimeMillis() - startTime;

                        // Update record with response and metadata
                        record.setResponse(response);

                        // For demo purposes, generate a random LLM score
                        generateLlmScore(record);

                        // Update metadata if available
                        if (record.getMetadata() != null) {
                            record.getMetadata().setProcessingTimeMs(processingTime);
                        }

                        // Save updated record
                        evaluationDataService.saveRecord(record);

                        logger.debug("Successfully processed question {}/{} for run {}", completed + 1,
                                records.size(), runId);
                    } else {
                        logger.debug("Skipping record {} - already has response", record.getId());
                    }

                    // Increment completed count after successful processing
                    completed++;

                } catch (RuntimeException e) {
                    // Check if this is a token expiration error
                    if (GlobalExceptionHandler.isTokenExpiredException(e)) {
                        logger.error("Token expired while processing record {} in run {}", record.getId(), runId, e);

                        // Handle token expiration specifically
                        handleTokenExpirationInRun(runId, e);

                        // Stop processing and mark run as failed
                        run.setStatus(RunStatus.FAILED);
                        throw new TokenExpiredException(
                                "Authentication token expired during run execution",
                                "Please refresh your token in the main Thymeleaf Agent application and try again",
                                e);
                    } else {
                        // Handle other runtime errors
                        logger.error("Runtime error processing record {} in run {}", record.getId(), runId, e);

                        // Increment completed count for failed processing
                        completed++;

                        // Update progress to reflect the failed question
                        progressTrackingService.updateProgress(runId, completed, "Error: " + e.getMessage());
                        progressTrackingService.updateMessage(runId,
                                "Error on question " + completed + " of " + records.size() + ": " + e.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("Failed to process record {} in run {}", record.getId(), runId, e);

                    // Increment completed count for failed processing
                    completed++;

                    // Update progress to reflect the failed question
                    progressTrackingService.updateProgress(runId, completed, "Error: " + e.getMessage());
                    progressTrackingService.updateMessage(runId,
                            "Error on question " + completed + " of " + records.size() + ": " + e.getMessage());
                }

                // Update run progress after each record (success or failure)
                run.setCompletedRecords(completed);
                evaluationDataService.saveRun(run);
            }

            // Mark run as completed
            run.setStatus(RunStatus.COMPLETED);
            run.setCompletedAt(LocalDateTime.now());

            // Complete progress tracking
            progressTrackingService.completeProgress(runId,
                    String.format("Successfully processed %d of %d questions", completed, records.size()));

            logger.info("Completed evaluation run {} with {}/{} records processed",
                    runId, completed, records.size());

        } catch (TokenExpiredException e) {
            logger.error("Token expired during run execution: {}", runId, e);
            run.setStatus(RunStatus.FAILED);

            // Handle token expiration in progress tracking
            handleTokenExpirationInRun(runId, e);

        } catch (Exception e) {
            logger.error("Failed to execute run {}", runId, e);
            run.setStatus(RunStatus.FAILED);

            // Check if this is a token error
            if (GlobalExceptionHandler.isTokenExpiredException(e)) {
                handleTokenExpirationInRun(runId, e);
            } else {
                // Mark progress as failed for other errors
                progressTrackingService.failProgress(runId, "Run execution failed: " + e.getMessage());
            }
        }

        return evaluationDataService.saveRun(run);
    }

    /**
     * Executes an evaluation run asynchronously
     *
     * @param runId The ID of the run to execute
     * @return CompletableFuture that completes when the run is finished
     */
    public CompletableFuture<EvaluationRun> executeRunAsync(String runId) {
        return CompletableFuture.supplyAsync(() -> executeRun(runId));
    }

    /**
     * Creates a single evaluation record from a question
     *
     * @param question The input question
     * @param runId    The ID of the evaluation run
     * @return A new EvaluationRecord
     */
    private EvaluationRecord createEvaluationRecord(String question, String runId) {
        EvaluationRecord record = new EvaluationRecord();
        record.setId(UUID.randomUUID().toString());
        record.setRunId(runId);
        record.setInput(question);
        // Response will be generated later during execution
        record.setResponse("");

        // Initialize with unknown scores
        record.setLlmScore(ScoreType.UNKNOWN);
        record.setLlmReason("");
        record.setHumanScore(ScoreType.UNKNOWN);
        record.setHumanReason("");

        record.setTimestamp(LocalDateTime.now());

        return record;
    }

    /**
     * Generates a dummy LLM score for demonstration purposes
     * In a real implementation, this would be done by an actual evaluation model
     */
    private void generateLlmScore(EvaluationRecord record) {
        String input = record.getInput();
        String response = record.getResponse().toLowerCase();

        EvaluationScore evaluate = llmScoreService.evaluate(input, response);

        record.setLlmScore(evaluate.scoreType());
        record.setLlmReason(evaluate.reason());
    }

    /**
     * Gets the number of available questions from the input file
     *
     * @return Number of questions available
     */
    public int getAvailableQuestionCount() {
        return questionLoaderService.getQuestionCount();
    }

    /**
     * Checks if the question loader and agent runner are ready
     *
     * @return true if both services are ready
     */
    public boolean isReady() {
        boolean agentRunnerReady = agentRunner.isReady();

        logger.debug("Service readiness - AgentRunner: {}", agentRunnerReady);

        return agentRunnerReady;
    }

    /**
     * Gets status information about the evaluation runner service
     *
     * @return Status string with service information
     */
    public String getServiceStatus() {
        return String.format("EvaluationRunnerService - Agent: %s, Available Questions: %d",
                agentRunner.isReady() ? "Ready" : "Not Ready",
                getAvailableQuestionCount());
    }

    /**
     * Handles token expiration during run execution
     * Updates progress tracking with token refresh instructions
     *
     * @param runId The ID of the run that failed due to token expiration
     * @param ex    The exception that caused the token expiration
     */
    private void handleTokenExpirationInRun(String runId, Exception ex) {
        String errorMessage = "\ud83d\udd10 Authentication token has expired";
        String instructions = "To continue:\n" +
                "1. Open the main Thymeleaf Agent application (http://localhost:8080)\n" +
                "2. Navigate to the Token Management page\n" +
                "3. Generate a new token or refresh your existing token\n" +
                "4. The new token will be automatically shared with this evaluation app\n" +
                "5. Start a new evaluation run to continue";

        // Update progress with detailed token refresh instructions
        progressTrackingService.failProgress(runId, errorMessage);
        progressTrackingService.updateMessage(runId, errorMessage + "\n\n" + instructions);

        logger.warn("Run {} failed due to token expiration. User needs to refresh token.", runId);
    }
}
