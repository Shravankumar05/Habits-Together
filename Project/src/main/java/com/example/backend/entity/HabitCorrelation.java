package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "habit_correlations", schema = "public")
public class HabitCorrelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "habit1_id", nullable = false)
    private UUID habit1Id;
    
    @Column(name = "habit2_id", nullable = false)
    private UUID habit2Id;
    
    @Column(name = "correlation_coefficient")
    private Double correlationCoefficient;
    
    @Column(name = "correlation_type")
    @Enumerated(EnumType.STRING)
    private CorrelationType correlationType;
    
    @Column(name = "confidence_level")
    private Double confidenceLevel;
    
    @Column(name = "calculated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime calculatedAt;
    
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime updatedAt;
    
    public enum CorrelationType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL,
        CAUSAL,
        INVERSE_CAUSAL
    }
    
    public HabitCorrelation() {}
    
    public HabitCorrelation(UUID userId, UUID habit1Id, UUID habit2Id, Double correlationCoefficient, CorrelationType correlationType) {
        this.userId = userId;
        this.habit1Id = habit1Id;
        this.habit2Id = habit2Id;
        this.correlationCoefficient = correlationCoefficient;
        this.correlationType = correlationType;
        LocalDateTime now = LocalDateTime.now();
        this.calculatedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
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
    
    public UUID getHabit1Id() {
        return habit1Id;
    }
    
    public void setHabit1Id(UUID habit1Id) {
        this.habit1Id = habit1Id;
    }
    
    public UUID getHabit2Id() {
        return habit2Id;
    }
    
    public void setHabit2Id(UUID habit2Id) {
        this.habit2Id = habit2Id;
    }
    
    public Double getCorrelationCoefficient() {
        return correlationCoefficient;
    }
    
    public void setCorrelationCoefficient(Double correlationCoefficient) {
        this.correlationCoefficient = correlationCoefficient;
    }
    
    public CorrelationType getCorrelationType() {
        return correlationType;
    }
    
    public void setCorrelationType(CorrelationType correlationType) {
        this.correlationType = correlationType;
    }
    
    public Double getConfidenceLevel() {
        return confidenceLevel;
    }
    
    public void setConfidenceLevel(Double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
    
    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
    
    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
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
        if (calculatedAt == null) {
            calculatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}