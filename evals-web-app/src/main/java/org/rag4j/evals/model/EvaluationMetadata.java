package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata associated with an evaluation record
 */
public class EvaluationMetadata {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("maxTokens")
    private Integer maxTokens;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("additionalData")
    private Map<String, Object> additionalData = new HashMap<>();
    
    // Default constructor
    public EvaluationMetadata() {}
    
    // Getters and Setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
    
    // Utility methods
    public void addData(String key, Object value) {
        if (additionalData == null) {
            additionalData = new HashMap<>();
        }
        additionalData.put(key, value);
    }
    
    public Object getData(String key) {
        return additionalData != null ? additionalData.get(key) : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationMetadata that = (EvaluationMetadata) o;
        return Objects.equals(model, that.model) &&
                Objects.equals(temperature, that.temperature) &&
                Objects.equals(maxTokens, that.maxTokens) &&
                Objects.equals(processingTimeMs, that.processingTimeMs) &&
                Objects.equals(additionalData, that.additionalData);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(model, temperature, maxTokens, processingTimeMs, additionalData);
    }
    
    @Override
    public String toString() {
        return "EvaluationMetadata{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", processingTimeMs=" + processingTimeMs +
                ", additionalData=" + additionalData +
                '}';
    }
}
