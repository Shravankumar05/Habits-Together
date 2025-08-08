package com.example.backend.dto.analytics;

import com.example.backend.service.analytics.GroupDynamicsEngine;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for group dynamics analysis results.
 */
public class GroupDynamicsDTO {
    
    private UUID groupId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private double momentumScore;
    private double cohesionScore;
    private int groupStreak;
    private double synergisticScore;
    private List<KeyContributorDTO> keyContributors;
    private ParticipationMetricsDTO participationMetrics;
    
    public GroupDynamicsDTO() {}
    
    public GroupDynamicsDTO(GroupDynamicsEngine.GroupDynamicsResult result) {
        this.groupId = result.getGroupId();
        this.startDate = result.getStartDate();
        this.endDate = result.getEndDate();
        this.momentumScore = result.getMomentumScore();
        this.cohesionScore = result.getCohesionScore();
        this.groupStreak = result.getGroupStreak();
        this.synergisticScore = result.getSynergisticScore();
        
        this.keyContributors = result.getKeyContributors().stream()
            .map(KeyContributorDTO::new)
            .collect(Collectors.toList());
        
        this.participationMetrics = new ParticipationMetricsDTO(result.getParticipationMetrics());
    }
    
    // Getters and Setters
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public double getMomentumScore() { return momentumScore; }
    public void setMomentumScore(double momentumScore) { this.momentumScore = momentumScore; }
    
    public double getCohesionScore() { return cohesionScore; }
    public void setCohesionScore(double cohesionScore) { this.cohesionScore = cohesionScore; }
    
    public int getGroupStreak() { return groupStreak; }
    public void setGroupStreak(int groupStreak) { this.groupStreak = groupStreak; }
    
    public double getSynergisticScore() { return synergisticScore; }
    public void setSynergisticScore(double synergisticScore) { this.synergisticScore = synergisticScore; }
    
    public List<KeyContributorDTO> getKeyContributors() { return keyContributors; }
    public void setKeyContributors(List<KeyContributorDTO> keyContributors) { this.keyContributors = keyContributors; }
    
    public ParticipationMetricsDTO getParticipationMetrics() { return participationMetrics; }
    public void setParticipationMetrics(ParticipationMetricsDTO participationMetrics) { this.participationMetrics = participationMetrics; }
    
    public static class KeyContributorDTO {
        private UUID userId;
        private int totalAttempts;
        private int successfulCompletions;
        private double completionRate;
        private double contributionScore;
        private String contributorType;
        
        public KeyContributorDTO() {}
        
        public KeyContributorDTO(GroupDynamicsEngine.KeyContributor contributor) {
            this.userId = contributor.getUserId();
            this.totalAttempts = contributor.getTotalAttempts();
            this.successfulCompletions = contributor.getSuccessfulCompletions();
            this.completionRate = contributor.getCompletionRate();
            this.contributionScore = contributor.getContributionScore();
            this.contributorType = contributor.getContributorType().name();
        }
        
        // Getters and Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        
        public int getSuccessfulCompletions() { return successfulCompletions; }
        public void setSuccessfulCompletions(int successfulCompletions) { this.successfulCompletions = successfulCompletions; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        
        public double getContributionScore() { return contributionScore; }
        public void setContributionScore(double contributionScore) { this.contributionScore = contributionScore; }
        
        public String getContributorType() { return contributorType; }
        public void setContributorType(String contributorType) { this.contributorType = contributorType; }
    }
    
    public static class ParticipationMetricsDTO {
        private int totalMembers;
        private int activeMembers;
        private double participationRate;
        private int totalAttempts;
        private int totalCompletions;
        private double completionRate;
        
        public ParticipationMetricsDTO() {}
        
        public ParticipationMetricsDTO(GroupDynamicsEngine.ParticipationMetrics metrics) {
            this.totalMembers = metrics.getTotalMembers();
            this.activeMembers = metrics.getActiveMembers();
            this.participationRate = metrics.getParticipationRate();
            this.totalAttempts = metrics.getTotalAttempts();
            this.totalCompletions = metrics.getTotalCompletions();
            this.completionRate = metrics.getCompletionRate();
        }
        
        // Getters and Setters
        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
        
        public int getActiveMembers() { return activeMembers; }
        public void setActiveMembers(int activeMembers) { this.activeMembers = activeMembers; }
        
        public double getParticipationRate() { return participationRate; }
        public void setParticipationRate(double participationRate) { this.participationRate = participationRate; }
        
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        
        public int getTotalCompletions() { return totalCompletions; }
        public void setTotalCompletions(int totalCompletions) { this.totalCompletions = totalCompletions; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }
}