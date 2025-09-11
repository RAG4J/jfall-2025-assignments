package org.rag4j.evals.model;

public record EvaluationScore(
        ScoreType scoreType,
        String reason) {
}
