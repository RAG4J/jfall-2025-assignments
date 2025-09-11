package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for evaluation runs
 */
public class RunConfiguration {
    
    @JsonProperty("modelName")
    private String modelName;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("maxTokens")
    private Integer maxTokens;
    
    @JsonProperty("evaluationPrompt")
    private String evaluationPrompt;
    
    @JsonProperty("inputDataSource")
    private String inputDataSource;
    
    @JsonProperty("outputFormat")
    private String outputFormat;
    
    @JsonProperty("batchSize")
    private Integer batchSize;
    
    @JsonProperty("customSettings")
    private Map<String, Object> customSettings = new HashMap<>();
    
    // Default constructor
    public RunConfiguration() {
        // Set default values
        this.temperature = 0.7;
        this.maxTokens = 1000;
        this.batchSize = 10;
        this.outputFormat = "json";
    }
    
    // Getters and Setters
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
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
    
    public String getEvaluationPrompt() {
        return evaluationPrompt;
    }
    
    public void setEvaluationPrompt(String evaluationPrompt) {
        this.evaluationPrompt = evaluationPrompt;
    }
    
    public String getInputDataSource() {
        return inputDataSource;
    }
    
    public void setInputDataSource(String inputDataSource) {
        this.inputDataSource = inputDataSource;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    public Integer getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
    
    public Map<String, Object> getCustomSettings() {
        return customSettings;
    }
    
    public void setCustomSettings(Map<String, Object> customSettings) {
        this.customSettings = customSettings;
    }
    
    // Utility methods
    public void addCustomSetting(String key, Object value) {
        if (customSettings == null) {
            customSettings = new HashMap<>();
        }
        customSettings.put(key, value);
    }
    
    public Object getCustomSetting(String key) {
        return customSettings != null ? customSettings.get(key) : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunConfiguration that = (RunConfiguration) o;
        return Objects.equals(modelName, that.modelName) &&
                Objects.equals(temperature, that.temperature) &&
                Objects.equals(maxTokens, that.maxTokens) &&
                Objects.equals(evaluationPrompt, that.evaluationPrompt) &&
                Objects.equals(inputDataSource, that.inputDataSource) &&
                Objects.equals(outputFormat, that.outputFormat) &&
                Objects.equals(batchSize, that.batchSize) &&
                Objects.equals(customSettings, that.customSettings);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(modelName, temperature, maxTokens, evaluationPrompt, 
                          inputDataSource, outputFormat, batchSize, customSettings);
    }
    
    @Override
    public String toString() {
        return "RunConfiguration{" +
                "modelName='" + modelName + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", evaluationPrompt='" + evaluationPrompt + '\'' +
                ", inputDataSource='" + inputDataSource + '\'' +
                ", outputFormat='" + outputFormat + '\'' +
                ", batchSize=" + batchSize +
                ", customSettings=" + customSettings +
                '}';
    }
}
