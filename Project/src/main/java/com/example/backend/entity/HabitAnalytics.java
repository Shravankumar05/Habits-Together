package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "habit_analytics", schema = "public")
public class HabitAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "habit_id", nullable = false)
    private UUID habitId;
    
    @Column(name = "success_rate")
    private Double successRate;
    
    @Column(name = "consistency_score")
    private Double consistencyScore;
    
    @Column(name = "optimal_time_start")
    private LocalTime optimalTimeStart;
    
    @Column(name = "optimal_time_end")
    private LocalTime optimalTimeEnd;
    
    @Column(name = "formation_stage")
    @Enumerated(EnumType.STRING)
    private FormationStage formationStage;
    
    @Column(name = "habit_strength")
    private Double habitStrength;
    
    @Column(name = "last_analyzed", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime lastAnalyzed;
    
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime updatedAt;
    
    public enum FormationStage {
        INITIATION,
        LEARNING,
        STABILITY,
        MASTERY
    }
    
    public HabitAnalytics() {}
    
    public HabitAnalytics(UUID userId, UUID habitId) {
        this.userId = userId;
        this.habitId = habitId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastAnalyzed = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getHabitId() {
        return habitId;
    }
    
    public void setHabitId(UUID habitId) {
        this.habitId = habitId;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    public Double getConsistencyScore() {
        return consistencyScore;
    }
    
    public void setConsistencyScore(Double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }
    
    public LocalTime getOptimalTimeStart() {
        return optimalTimeStart;
    }
    
    public void setOptimalTimeStart(LocalTime optimalTimeStart) {
        this.optimalTimeStart = optimalTimeStart;
    }
    
    public LocalTime getOptimalTimeEnd() {
        return optimalTimeEnd;
    }
    
    public void setOptimalTimeEnd(LocalTime optimalTimeEnd) {
        this.optimalTimeEnd = optimalTimeEnd;
    }
    
    public FormationStage getFormationStage() {
        return formationStage;
    }
    
    public void setFormationStage(FormationStage formationStage) {
        this.formationStage = formationStage;
    }
    
    public Double getHabitStrength() {
        return habitStrength;
    }
    
    public void setHabitStrength(Double habitStrength) {
        this.habitStrength = habitStrength;
    }
    
    public LocalDateTime getLastAnalyzed() {
        return lastAnalyzed;
    }
    
    public void setLastAnalyzed(LocalDateTime lastAnalyzed) {
        this.lastAnalyzed = lastAnalyzed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        lastAnalyzed = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}