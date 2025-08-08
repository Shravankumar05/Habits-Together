package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_metrics", schema = "public")
public class GroupMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    
    @Column(name = "group_streak")
    private Integer groupStreak;
    
    @Column(name = "momentum_score")
    private Double momentumScore;
    
    @Column(name = "synergistic_score")
    private Double synergisticScore;
    
    @Column(name = "cohesion_score")
    private Double cohesionScore;
    
    @Column(name = "calculated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime calculatedAt;
    
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime updatedAt;
    
    public GroupMetrics() {}
    
    public GroupMetrics(UUID groupId) {
        this.groupId = groupId;
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
    
    public UUID getGroupId() {
        return groupId;
    }
    
    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
    
    public Integer getGroupStreak() {
        return groupStreak;
    }
    
    public void setGroupStreak(Integer groupStreak) {
        this.groupStreak = groupStreak;
    }
    
    public Double getMomentumScore() {
        return momentumScore;
    }
    
    public void setMomentumScore(Double momentumScore) {
        this.momentumScore = momentumScore;
    }
    
    public Double getSynergisticScore() {
        return synergisticScore;
    }
    
    public void setSynergisticScore(Double synergisticScore) {
        this.synergisticScore = synergisticScore;
    }
    
    public Double getCohesionScore() {
        return cohesionScore;
    }
    
    public void setCohesionScore(Double cohesionScore) {
        this.cohesionScore = cohesionScore;
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