package com.example.backend.dto.analytics;

import com.example.backend.service.analytics.TeamChallengeGenerator;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for team challenge data.
 */
public class TeamChallengeDTO {
    
    private UUID id;
    private UUID groupId;
    private String title;
    private String description;
    private String challengeType;
    private ChallengeTargetDTO target;
    private int durationDays;
    private double difficultyLevel;
    private int priority;
    private List<ChallengeRewardDTO> rewards;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String status;
    
    public TeamChallengeDTO() {}
    
    public TeamChallengeDTO(TeamChallengeGenerator.TeamChallenge challenge) {
        this.id = challenge.getId();
        this.groupId = challenge.getGroupId();
        this.title = challenge.getTitle();
        this.description = challenge.getDescription();
        this.challengeType = challenge.getChallengeType().name();
        this.target = new ChallengeTargetDTO(challenge.getTarget());
        this.durationDays = challenge.getDurationDays();
        this.difficultyLevel = challenge.getDifficultyLevel();
        this.priority = challenge.getPriority();
        this.rewards = challenge.getRewards().stream()
            .map(ChallengeRewardDTO::new)
            .collect(Collectors.toList());
        this.createdAt = challenge.getCreatedAt();
        this.startDate = challenge.getStartDate();
        this.endDate = challenge.getEndDate();
        this.status = challenge.getStatus().name();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }
    
    public ChallengeTargetDTO getTarget() { return target; }
    public void setTarget(ChallengeTargetDTO target) { this.target = target; }
    
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    
    public double getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(double difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public List<ChallengeRewardDTO> getRewards() { return rewards; }
    public void setRewards(List<ChallengeRewardDTO> rewards) { this.rewards = rewards; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public static class ChallengeTargetDTO {
        private String metric;
        private double targetValue;
        private String unit;
        
        public ChallengeTargetDTO() {}
        
        public ChallengeTargetDTO(TeamChallengeGenerator.ChallengeTarget target) {
            this.metric = target.getMetric().name();
            this.targetValue = target.getTargetValue();
            this.unit = target.getUnit();
        }
        
        // Getters and Setters
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        
        public double getTargetValue() { return targetValue; }
        public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
    
    public static class ChallengeRewardDTO {
        private String name;
        private String type;
        private int value;
        
        public ChallengeRewardDTO() {}
        
        public ChallengeRewardDTO(TeamChallengeGenerator.ChallengeReward reward) {
            this.name = reward.getName();
            this.type = reward.getType().name();
            this.value = reward.getValue();
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}