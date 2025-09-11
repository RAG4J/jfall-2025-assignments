package org.rag4j.evals.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.rag4j.evals.model.EvaluationRecord;
import org.rag4j.evals.model.EvaluationRun;
import org.rag4j.evals.model.ScoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing evaluation data using file-based persistence
 */
@Service
public class EvaluationDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluationDataService.class);
    
    private final ObjectMapper objectMapper;
    private final String dataDirectory;
    private final String recordsFileName = "evaluation-records.json";
    private final String runsFileName = "evaluation-runs.json";
    
    public EvaluationDataService(@Value("${evals.data.directory:src/main/data}") String dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeDataDirectory();
    }
    
    private void initializeDataDirectory() {
        try {
            Path dataPath = Paths.get(dataDirectory);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
                logger.info("Created data directory: {}", dataPath.toAbsolutePath());
            }
            
            // Initialize with sample data if files don't exist
            Path recordsPath = dataPath.resolve(recordsFileName);
            Path runsPath = dataPath.resolve(runsFileName);
            
            if (!Files.exists(recordsPath)) {
                initializeRecords(recordsPath);
            }
            
            if (!Files.exists(runsPath)) {
                initializeRuns(runsPath);
            }
            
        } catch (IOException e) {
            logger.error("Failed to initialize data directory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    private void initializeRecords(Path recordsPath) throws IOException {
        List<EvaluationRecord> sampleRecords = List.of();
        saveRecordsToFile(sampleRecords, recordsPath.toFile());
        logger.info("Initialized sample evaluation records");
    }

    private void initializeRuns(Path runsPath) throws IOException {
        List<EvaluationRun> sampleRuns = List.of();
        saveRunsToFile(sampleRuns, runsPath.toFile());
        logger.info("Initialized sample evaluation runs");
    }
    
    // Evaluation Records Management
    
    public List<EvaluationRecord> getAllRecords() {
        try {
            File recordsFile = new File(dataDirectory, recordsFileName);
            if (!recordsFile.exists()) {
                return new ArrayList<>();
            }
            
            List<EvaluationRecord> records = objectMapper.readValue(recordsFile, 
                    new TypeReference<List<EvaluationRecord>>() {});
            logger.debug("Loaded {} evaluation records", records.size());
            return records;
            
        } catch (IOException e) {
            logger.error("Failed to load evaluation records: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public List<EvaluationRecord> getRecordsByRunId(String runId) {
        return getAllRecords().stream()
                .filter(record -> Objects.equals(record.getRunId(), runId))
                .collect(Collectors.toList());
    }
    
    public Optional<EvaluationRecord> getRecordById(String id) {
        return getAllRecords().stream()
                .filter(record -> Objects.equals(record.getId(), id))
                .findFirst();
    }
    
    public EvaluationRecord saveRecord(EvaluationRecord record) {
        List<EvaluationRecord> records = getAllRecords();
        
        // Update existing or add new
        boolean updated = false;
        for (int i = 0; i < records.size(); i++) {
            if (Objects.equals(records.get(i).getId(), record.getId())) {
                records.set(i, record);
                updated = true;
                break;
            }
        }
        
        if (!updated) {
            if (record.getId() == null) {
                record.setId(UUID.randomUUID().toString());
            }
            records.add(record);
        }
        
        saveRecords(records);
        logger.info("Saved evaluation record: {}", record.getId());
        return record;
    }
    
    public void deleteRecord(String id) {
        List<EvaluationRecord> records = getAllRecords();
        records.removeIf(record -> Objects.equals(record.getId(), id));
        saveRecords(records);
        logger.info("Deleted evaluation record: {}", id);
    }
    
    public EvaluationRecord updateHumanScore(String recordId, ScoreType score, String reason) {
        Optional<EvaluationRecord> recordOpt = getRecordById(recordId);
        if (recordOpt.isPresent()) {
            EvaluationRecord record = recordOpt.get();
            record.setHumanScore(score);
            record.setHumanReason(reason);
            return saveRecord(record);
        }
        throw new IllegalArgumentException("Record not found: " + recordId);
    }
    
    // Evaluation Runs Management
    
    public List<EvaluationRun> getAllRuns() {
        try {
            File runsFile = new File(dataDirectory, runsFileName);
            if (!runsFile.exists()) {
                return new ArrayList<>();
            }
            
            List<EvaluationRun> runs = objectMapper.readValue(runsFile, 
                    new TypeReference<List<EvaluationRun>>() {});
            logger.debug("Loaded {} evaluation runs", runs.size());
            return runs;
            
        } catch (IOException e) {
            logger.error("Failed to load evaluation runs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public Optional<EvaluationRun> getRunById(String id) {
        return getAllRuns().stream()
                .filter(run -> Objects.equals(run.getId(), id))
                .findFirst();
    }
    
    public EvaluationRun saveRun(EvaluationRun run) {
        List<EvaluationRun> runs = getAllRuns();
        
        // Update existing or add new
        boolean updated = false;
        for (int i = 0; i < runs.size(); i++) {
            if (Objects.equals(runs.get(i).getId(), run.getId())) {
                runs.set(i, run);
                updated = true;
                break;
            }
        }
        
        if (!updated) {
            if (run.getId() == null) {
                run.setId(UUID.randomUUID().toString());
            }
            runs.add(run);
        }
        
        saveRuns(runs);
        logger.info("Saved evaluation run: {}", run.getId());
        return run;
    }
    
    public void deleteRun(String id) {
        List<EvaluationRun> runs = getAllRuns();
        runs.removeIf(run -> Objects.equals(run.getId(), id));
        saveRuns(runs);
        
        // Also delete associated records
        List<EvaluationRecord> records = getAllRecords();
        records.removeIf(record -> Objects.equals(record.getRunId(), id));
        saveRecords(records);
        
        logger.info("Deleted evaluation run and associated records: {}", id);
    }
    
    // Private helper methods
    
    private void saveRecords(List<EvaluationRecord> records) {
        try {
            File recordsFile = new File(dataDirectory, recordsFileName);
            saveRecordsToFile(records, recordsFile);
        } catch (IOException e) {
            logger.error("Failed to save evaluation records: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save evaluation records", e);
        }
    }
    
    private void saveRuns(List<EvaluationRun> runs) {
        try {
            File runsFile = new File(dataDirectory, runsFileName);
            saveRunsToFile(runs, runsFile);
        } catch (IOException e) {
            logger.error("Failed to save evaluation runs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save evaluation runs", e);
        }
    }
    
    private void saveRecordsToFile(List<EvaluationRecord> records, File file) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, records);
    }
    
    private void saveRunsToFile(List<EvaluationRun> runs, File file) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, runs);
    }
    
    // Sample data creation
    
    private List<EvaluationRecord> createSampleRecords() {
        List<EvaluationRecord> records = new ArrayList<>();
        
        records.add(new EvaluationRecord(
                UUID.randomUUID().toString(),
                "run-001",
                "What is the capital of France?",
                "The capital of France is Paris.",
                ScoreType.GOOD,
                "Response is accurate and concise.",
                ScoreType.GOOD,
                "Correct answer provided clearly."
        ));
        
        records.add(new EvaluationRecord(
                UUID.randomUUID().toString(),
                "run-001",
                "Explain quantum computing in simple terms.",
                "Quantum computing is a type of computing that uses quantum bits instead of regular bits.",
                ScoreType.BAD,
                "Response lacks detail and accuracy.",
                ScoreType.BAD,
                "Too simplistic, missing key concepts."
        ));
        
        records.add(new EvaluationRecord(
                UUID.randomUUID().toString(),
                "run-002",
                "How do you make chocolate chip cookies?",
                "To make chocolate chip cookies, you need flour, butter, sugar, eggs, and chocolate chips. Mix ingredients, form dough, bake at 375°F for 10-12 minutes.",
                ScoreType.GOOD,
                "Provides basic recipe with key steps.",
                ScoreType.UNKNOWN,
                ""
        ));
        
        return records;
    }
    
    private List<EvaluationRun> createSampleRuns() {
        List<EvaluationRun> runs = new ArrayList<>();
        
        EvaluationRun run1 = new EvaluationRun("run-001", "GPT-4 Baseline Evaluation", 
                "Initial evaluation of GPT-4 responses on general knowledge questions.");
        run1.setTotalRecords(2);
        run1.setCompletedRecords(2);
        runs.add(run1);
        
        EvaluationRun run2 = new EvaluationRun("run-002", "Recipe Instructions Test", 
                "Testing model performance on cooking instruction generation.");
        run2.setTotalRecords(1);
        run2.setCompletedRecords(0);
        runs.add(run2);
        
        return runs;
    }
}
