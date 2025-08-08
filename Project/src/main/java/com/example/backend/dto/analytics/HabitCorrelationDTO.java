package com.example.backend.dto.analytics;

import com.example.backend.entity.HabitCorrelation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for habit correlation data.
 */
public class HabitCorrelationDTO {
    
    private UUID id;
    private UUID userId;
    private UUID habit1Id;
    private UUID habit2Id;
    private Double correlationCoefficient;
    private String correlationType;
    private Double confidenceLevel;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public HabitCorrelationDTO() {}
    
    public HabitCorrelationDTO(HabitCorrelation correlation) {
        this.id = correlation.getId();
        this.userId = correlation.getUserId();
        this.habit1Id = correlation.getHabit1Id();
        this.habit2Id = correlation.getHabit2Id();
        this.correlationCoefficient = correlation.getCorrelationCoefficient();
        this.correlationType = correlation.getCorrelationType() != null ? correlation.getCorrelationType().name() : null;
        this.confidenceLevel = correlation.getConfidenceLevel();
        this.calculatedAt = correlation.getCalculatedAt();
        this.createdAt = correlation.getCreatedAt();
        this.updatedAt = correlation.getUpdatedAt();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getHabit1Id() { return habit1Id; }
    public void setHabit1Id(UUID habit1Id) { this.habit1Id = habit1Id; }
    
    public UUID getHabit2Id() { return habit2Id; }
    public void setHabit2Id(UUID habit2Id) { this.habit2Id = habit2Id; }
    
    public Double getCorrelationCoefficient() { return correlationCoefficient; }
    public void setCorrelationCoefficient(Double correlationCoefficient) { this.correlationCoefficient = correlationCoefficient; }
    
    public String getCorrelationType() { return correlationType; }
    public void setCorrelationType(String correlationType) { this.correlationType = correlationType; }
    
    public Double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(Double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}