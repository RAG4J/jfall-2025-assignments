package org.rag4j.evals.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a question from the input questions file
 */
public class InputQuestion {
    
    @JsonProperty("question")
    private String question;
    
    // Default constructor
    public InputQuestion() {}
    
    // Constructor
    public InputQuestion(String question) {
        this.question = question;
    }
    
    // Getters and Setters
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputQuestion that = (InputQuestion) o;
        return Objects.equals(question, that.question);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(question);
    }
    
    @Override
    public String toString() {
        return "InputQuestion{" +
                "question='" + question + '\'' +
                '}';
    }
}
