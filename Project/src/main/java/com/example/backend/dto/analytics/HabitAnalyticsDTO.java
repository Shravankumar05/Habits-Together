package com.example.backend.dto.analytics;

import com.example.backend.entity.HabitAnalytics;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for habit analytics data.
 */
public class HabitAnalyticsDTO {
    
    private UUID id;
    private UUID userId;
    private UUID habitId;
    private Double successRate;
    private Double consistencyScore;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime optimalTimeStart;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime optimalTimeEnd;
    
    private String formationStage;
    private Double habitStrength;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAnalyzed;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public HabitAnalyticsDTO() {}
    
    public HabitAnalyticsDTO(HabitAnalytics analytics) {
        this.id = analytics.getId();
        this.userId = analytics.getUserId();
        this.habitId = analytics.getHabitId();
        this.successRate = analytics.getSuccessRate();
        this.consistencyScore = analytics.getConsistencyScore();
        this.optimalTimeStart = analytics.getOptimalTimeStart();
        this.optimalTimeEnd = analytics.getOptimalTimeEnd();
        this.formationStage = analytics.getFormationStage() != null ? analytics.getFormationStage().name() : null;
        this.habitStrength = analytics.getHabitStrength();
        this.lastAnalyzed = analytics.getLastAnalyzed();
        this.createdAt = analytics.getCreatedAt();
        this.updatedAt = analytics.getUpdatedAt();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getHabitId() { return habitId; }
    public void setHabitId(UUID habitId) { this.habitId = habitId; }
    
    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    
    public Double getConsistencyScore() { return consistencyScore; }
    public void setConsistencyScore(Double consistencyScore) { this.consistencyScore = consistencyScore; }
    
    public LocalTime getOptimalTimeStart() { return optimalTimeStart; }
    public void setOptimalTimeStart(LocalTime optimalTimeStart) { this.optimalTimeStart = optimalTimeStart; }
    
    public LocalTime getOptimalTimeEnd() { return optimalTimeEnd; }
    public void setOptimalTimeEnd(LocalTime optimalTimeEnd) { this.optimalTimeEnd = optimalTimeEnd; }
    
    public String getFormationStage() { return formationStage; }
    public void setFormationStage(String formationStage) { this.formationStage = formationStage; }
    
    public Double getHabitStrength() { return habitStrength; }
    public void setHabitStrength(Double habitStrength) { this.habitStrength = habitStrength; }
    
    public LocalDateTime getLastAnalyzed() { return lastAnalyzed; }
    public void setLastAnalyzed(LocalDateTime lastAnalyzed) { this.lastAnalyzed = lastAnalyzed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}