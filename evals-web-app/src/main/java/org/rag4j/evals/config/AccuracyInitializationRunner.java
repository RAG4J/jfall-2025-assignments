package org.rag4j.evals.config;

import org.rag4j.evals.service.AccuracyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Initialization runner that calculates accuracy for existing runs on application startup
 */
@Component
public class AccuracyInitializationRunner implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(AccuracyInitializationRunner.class);
    
    private final AccuracyService accuracyService;
    
    @Autowired
    public AccuracyInitializationRunner(AccuracyService accuracyService) {
        this.accuracyService = accuracyService;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting accuracy initialization for existing evaluation runs...");
        
        try {
            accuracyService.calculateAndUpdateAccuracyForAllRuns();
            logger.info("Successfully completed accuracy initialization");
        } catch (Exception e) {
            logger.error("Failed to initialize accuracy calculations: {}", e.getMessage(), e);
            // Don't fail application startup if accuracy calculation fails
        }
    }
}
