package com.example.backend.service.analytics;

import com.example.backend.entity.GroupMember;
import com.example.backend.repository.GroupMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating team challenges based on group performance and capabilities.
 * Creates adaptive challenges that adjust difficulty based on group dynamics.
 */
@Service
public class TeamChallengeGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(TeamChallengeGenerator.class);
    
    @Autowired
    private GroupDynamicsEngine groupDynamicsEngine;
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    /**
     * Generates challenges for a group based on their performance and dynamics.
     */
    public List<TeamChallenge> generateChallengesForGroup(UUID groupId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating challenges for group {} from {} to {}", groupId, startDate, endDate);
        
        // Analyze group dynamics
        GroupDynamicsEngine.GroupDynamicsResult dynamics = 
            groupDynamicsEngine.calculateGroupDynamics(groupId, startDate, endDate);
        
        // Get group completion data
        AnalyticsDataCollector.GroupCompletionData groupData = 
            analyticsDataCollector.collectGroupCompletionData(groupId, startDate, endDate);
        
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        
        List<TeamChallenge> challenges = new ArrayList<>();
        
        // Generate different types of challenges based on group characteristics
        challenges.addAll(generateStreakChallenges(dynamics, groupData, members));
        challenges.addAll(generateCollaborationChallenges(dynamics, groupData, members));
        challenges.addAll(generateImprovementChallenges(dynamics, groupData, members));
        challenges.addAll(generateConsistencyChallenges(dynamics, groupData, members));
        
        // Sort challenges by priority and difficulty
        challenges.sort((c1, c2) -> {
            int priorityCompare = Integer.compare(c2.getPriority(), c1.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(c1.getDifficultyLevel(), c2.getDifficultyLevel());
        });
        
        // Limit to top 5 challenges
        List<TeamChallenge> topChallenges = challenges.stream()
            .limit(5)
            .collect(Collectors.toList());
        
        logger.debug("Generated {} challenges for group {}", topChallenges.size(), groupId);
        
        return topChallenges;
    }
    
    /**
     * Adjusts challenge difficulty based on group capabilities.
     */
    public TeamChallenge adjustChallengeDifficulty(TeamChallenge challenge, GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        double adjustmentFactor = calculateDifficultyAdjustment(dynamics);
        
        // Adjust target values based on group performance
        ChallengeTarget adjustedTarget = adjustTarget(challenge.getTarget(), adjustmentFactor);
        
        // Adjust duration if needed
        int adjustedDuration = adjustDuration(challenge.getDurationDays(), dynamics);
        
        return new TeamChallenge(
            challenge.getId(),
            challenge.getGroupId(),
            challenge.getTitle(),
            challenge.getDescription(),
            challenge.getChallengeType(),
            adjustedTarget,
            adjustedDuration,
            challenge.getDifficultyLevel() * adjustmentFactor,
            challenge.getPriority(),
            challenge.getRewards(),
            challenge.getCreatedAt(),
            challenge.getStartDate(),
            challenge.getEndDate(),
            ChallengeStatus.PENDING
        );
    }
    
    /**
     * Tracks challenge completion and updates rewards.
     */
    public ChallengeProgress trackChallengeProgress(UUID challengeId, UUID groupId, LocalDate currentDate) {
        logger.debug("Tracking progress for challenge {} in group {}", challengeId, groupId);
        
        // This would typically fetch the challenge from database
        // For now, we'll simulate progress tracking
        
        LocalDate startDate = currentDate.minusDays(7);
        AnalyticsDataCollector.GroupCompletionData groupData = 
            analyticsDataCollector.collectGroupCompletionData(groupId, startDate, currentDate);
        
        // Calculate current progress based on challenge type
        double progressPercentage = calculateProgressPercentage(groupData, currentDate);
        
        boolean isCompleted = progressPercentage >= 100.0;
        ChallengeStatus status = isCompleted ? ChallengeStatus.COMPLETED : ChallengeStatus.ACTIVE;
        
        return new ChallengeProgress(
            challengeId,
            groupId,
            progressPercentage,
            status,
            currentDate,
            isCompleted ? calculateRewardPoints(progressPercentage) : 0
        );
    }
    
    // Private helper methods for generating different types of challenges
    
    private List<TeamChallenge> generateStreakChallenges(GroupDynamicsEngine.GroupDynamicsResult dynamics,
                                                        AnalyticsDataCollector.GroupCompletionData groupData,
                                                        List<GroupMember> members) {
        List<TeamChallenge> challenges = new ArrayList<>();
        
        int currentStreak = dynamics.getGroupStreak();
        int targetStreak = Math.max(currentStreak + 3, 7); // At least 7 days or current + 3
        
        // Streak extension challenge
        ChallengeTarget target = new ChallengeTarget(ChallengeMetric.GROUP_STREAK, targetStreak, "days");
        
        TeamChallenge streakChallenge = new TeamChallenge(
            UUID.randomUUID(),
            dynamics.getGroupId(),
            "Streak Master Challenge",
            String.format("Maintain a group streak of %d consecutive days", targetStreak),
            ChallengeType.STREAK,
            target,
            targetStreak + 2, // Give extra days for completion
            calculateStreakDifficulty(currentStreak, targetStreak),
            calculateStreakPriority(dynamics),
            generateStreakRewards(targetStreak),
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusDays(targetStreak + 2),
            ChallengeStatus.PENDING
        );
        
        challenges.add(streakChallenge);
        
        return challenges;
    }
    
    private List<TeamChallenge> generateCollaborationChallenges(GroupDynamicsEngine.GroupDynamicsResult dynamics,
                                                               AnalyticsDataCollector.GroupCompletionData groupData,
                                                               List<GroupMember> members) {
        List<TeamChallenge> challenges = new ArrayList<>();
        
        if (members.size() < 2) return challenges; // Need at least 2 members for collaboration
        
        // Synergy boost challenge
        double currentSynergy = dynamics.getSynergisticScore();
        double targetSynergy = Math.min(currentSynergy + 0.2, 0.9);
        
        ChallengeTarget target = new ChallengeTarget(ChallengeMetric.SYNERGY_SCORE, targetSynergy, "score");
        
        TeamChallenge synergyChallenge = new TeamChallenge(
            UUID.randomUUID(),
            dynamics.getGroupId(),
            "Team Synergy Boost",
            String.format("Achieve a synergy score of %.1f by working together", targetSynergy),
            ChallengeType.COLLABORATION,
            target,
            14, // 2 weeks
            calculateSynergyDifficulty(currentSynergy, targetSynergy),
            calculateCollaborationPriority(dynamics),
            generateCollaborationRewards(),
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusDays(14),
            ChallengeStatus.PENDING
        );
        
        challenges.add(synergyChallenge);
        
        return challenges;
    }
    
    private List<TeamChallenge> generateImprovementChallenges(GroupDynamicsEngine.GroupDynamicsResult dynamics,
                                                             AnalyticsDataCollector.GroupCompletionData groupData,
                                                             List<GroupMember> members) {
        List<TeamChallenge> challenges = new ArrayList<>();
        
        // Momentum improvement challenge
        double currentMomentum = dynamics.getMomentumScore();
        double targetMomentum = Math.min(currentMomentum + 0.15, 0.95);
        
        ChallengeTarget target = new ChallengeTarget(ChallengeMetric.MOMENTUM_SCORE, targetMomentum, "score");
        
        TeamChallenge momentumChallenge = new TeamChallenge(
            UUID.randomUUID(),
            dynamics.getGroupId(),
            "Momentum Builder",
            String.format("Boost group momentum to %.1f through consistent activity", targetMomentum),
            ChallengeType.IMPROVEMENT,
            target,
            10, // 10 days
            calculateMomentumDifficulty(currentMomentum, targetMomentum),
            calculateImprovementPriority(dynamics),
            generateImprovementRewards(),
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            ChallengeStatus.PENDING
        );
        
        challenges.add(momentumChallenge);
        
        return challenges;
    }
    
    private List<TeamChallenge> generateConsistencyChallenges(GroupDynamicsEngine.GroupDynamicsResult dynamics,
                                                             AnalyticsDataCollector.GroupCompletionData groupData,
                                                             List<GroupMember> members) {
        List<TeamChallenge> challenges = new ArrayList<>();
        
        // Cohesion improvement challenge
        double currentCohesion = dynamics.getCohesionScore();
        double targetCohesion = Math.min(currentCohesion + 0.1, 0.9);
        
        ChallengeTarget target = new ChallengeTarget(ChallengeMetric.COHESION_SCORE, targetCohesion, "score");
        
        TeamChallenge cohesionChallenge = new TeamChallenge(
            UUID.randomUUID(),
            dynamics.getGroupId(),
            "Unity Challenge",
            String.format("Improve group cohesion to %.1f by participating together", targetCohesion),
            ChallengeType.CONSISTENCY,
            target,
            21, // 3 weeks
            calculateCohesionDifficulty(currentCohesion, targetCohesion),
            calculateConsistencyPriority(dynamics),
            generateConsistencyRewards(),
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusDays(21),
            ChallengeStatus.PENDING
        );
        
        challenges.add(cohesionChallenge);
        
        return challenges;
    }
    
    // Difficulty calculation methods
    
    private double calculateStreakDifficulty(int currentStreak, int targetStreak) {
        double streakIncrease = (double) (targetStreak - currentStreak) / Math.max(currentStreak, 1);
        return Math.min(0.9, 0.3 + (streakIncrease * 0.6));
    }
    
    private double calculateSynergyDifficulty(double current, double target) {
        double improvement = target - current;
        return Math.min(0.9, 0.4 + (improvement * 2.0));
    }
    
    private double calculateMomentumDifficulty(double current, double target) {
        double improvement = target - current;
        return Math.min(0.9, 0.3 + (improvement * 3.0));
    }
    
    private double calculateCohesionDifficulty(double current, double target) {
        double improvement = target - current;
        return Math.min(0.9, 0.5 + (improvement * 2.5));
    }
    
    // Priority calculation methods
    
    private int calculateStreakPriority(GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        return dynamics.getGroupStreak() > 0 ? 8 : 6; // Higher priority if already have a streak
    }
    
    private int calculateCollaborationPriority(GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        return dynamics.getSynergisticScore() < 0.5 ? 9 : 7; // Higher priority if low synergy
    }
    
    private int calculateImprovementPriority(GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        return dynamics.getMomentumScore() < 0.6 ? 8 : 6; // Higher priority if low momentum
    }
    
    private int calculateConsistencyPriority(GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        return dynamics.getCohesionScore() < 0.7 ? 7 : 5; // Higher priority if low cohesion
    }
    
    // Reward generation methods
    
    private List<ChallengeReward> generateStreakRewards(int streakDays) {
        List<ChallengeReward> rewards = new ArrayList<>();
        rewards.add(new ChallengeReward("Streak Master Badge", RewardType.BADGE, streakDays * 10));
        rewards.add(new ChallengeReward("Bonus Points", RewardType.POINTS, streakDays * 50));
        return rewards;
    }
    
    private List<ChallengeReward> generateCollaborationRewards() {
        List<ChallengeReward> rewards = new ArrayList<>();
        rewards.add(new ChallengeReward("Team Player Badge", RewardType.BADGE, 100));
        rewards.add(new ChallengeReward("Synergy Bonus", RewardType.POINTS, 200));
        return rewards;
    }
    
    private List<ChallengeReward> generateImprovementRewards() {
        List<ChallengeReward> rewards = new ArrayList<>();
        rewards.add(new ChallengeReward("Momentum Builder Badge", RewardType.BADGE, 75));
        rewards.add(new ChallengeReward("Progress Points", RewardType.POINTS, 150));
        return rewards;
    }
    
    private List<ChallengeReward> generateConsistencyRewards() {
        List<ChallengeReward> rewards = new ArrayList<>();
        rewards.add(new ChallengeReward("Unity Badge", RewardType.BADGE, 80));
        rewards.add(new ChallengeReward("Consistency Bonus", RewardType.POINTS, 180));
        return rewards;
    }
    
    // Helper methods
    
    private double calculateDifficultyAdjustment(GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        // Adjust based on overall group performance
        double avgPerformance = (dynamics.getMomentumScore() + dynamics.getCohesionScore() + 
                               dynamics.getSynergisticScore()) / 3.0;
        
        if (avgPerformance > 0.8) return 1.2; // Make it harder for high-performing groups
        if (avgPerformance < 0.4) return 0.8; // Make it easier for struggling groups
        return 1.0; // Normal difficulty
    }
    
    private ChallengeTarget adjustTarget(ChallengeTarget original, double adjustmentFactor) {
        double adjustedValue = original.getTargetValue() * adjustmentFactor;
        return new ChallengeTarget(original.getMetric(), adjustedValue, original.getUnit());
    }
    
    private int adjustDuration(int originalDuration, GroupDynamicsEngine.GroupDynamicsResult dynamics) {
        double avgPerformance = (dynamics.getMomentumScore() + dynamics.getCohesionScore()) / 2.0;
        
        if (avgPerformance < 0.4) {
            return (int) (originalDuration * 1.3); // Give more time to struggling groups
        }
        return originalDuration;
    }
    
    private double calculateProgressPercentage(AnalyticsDataCollector.GroupCompletionData groupData, LocalDate currentDate) {
        // Simplified progress calculation - would be more sophisticated in real implementation
        if (groupData.getHabitCompletions().isEmpty()) return 0.0;
        
        int totalCompletions = groupData.getHabitCompletions().values().stream()
            .mapToInt(List::size)
            .sum();
        
        // Assume target was 100 completions for this example
        return Math.min(100.0, (totalCompletions / 100.0) * 100.0);
    }
    
    private int calculateRewardPoints(double progressPercentage) {
        return (int) (progressPercentage * 2); // 2 points per percentage point
    }
    
    // Data Transfer Objects
    
    public static class TeamChallenge {
        private final UUID id;
        private final UUID groupId;
        private final String title;
        private final String description;
        private final ChallengeType challengeType;
        private final ChallengeTarget target;
        private final int durationDays;
        private final double difficultyLevel;
        private final int priority;
        private final List<ChallengeReward> rewards;
        private final LocalDateTime createdAt;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final ChallengeStatus status;
        
        public TeamChallenge(UUID id, UUID groupId, String title, String description,
                           ChallengeType challengeType, ChallengeTarget target, int durationDays,
                           double difficultyLevel, int priority, List<ChallengeReward> rewards,
                           LocalDateTime createdAt, LocalDate startDate, LocalDate endDate,
                           ChallengeStatus status) {
            this.id = id;
            this.groupId = groupId;
            this.title = title;
            this.description = description;
            this.challengeType = challengeType;
            this.target = target;
            this.durationDays = durationDays;
            this.difficultyLevel = difficultyLevel;
            this.priority = priority;
            this.rewards = rewards;
            this.createdAt = createdAt;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }
        
        // Getters
        public UUID getId() { return id; }
        public UUID getGroupId() { return groupId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public ChallengeType getChallengeType() { return challengeType; }
        public ChallengeTarget getTarget() { return target; }
        public int getDurationDays() { return durationDays; }
        public double getDifficultyLevel() { return difficultyLevel; }
        public int getPriority() { return priority; }
        public List<ChallengeReward> getRewards() { return rewards; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public ChallengeStatus getStatus() { return status; }
    }
    
    public static class ChallengeTarget {
        private final ChallengeMetric metric;
        private final double targetValue;
        private final String unit;
        
        public ChallengeTarget(ChallengeMetric metric, double targetValue, String unit) {
            this.metric = metric;
            this.targetValue = targetValue;
            this.unit = unit;
        }
        
        // Getters
        public ChallengeMetric getMetric() { return metric; }
        public double getTargetValue() { return targetValue; }
        public String getUnit() { return unit; }
    }
    
    public static class ChallengeReward {
        private final String name;
        private final RewardType type;
        private final int value;
        
        public ChallengeReward(String name, RewardType type, int value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }
        
        // Getters
        public String getName() { return name; }
        public RewardType getType() { return type; }
        public int getValue() { return value; }
    }
    
    public static class ChallengeProgress {
        private final UUID challengeId;
        private final UUID groupId;
        private final double progressPercentage;
        private final ChallengeStatus status;
        private final LocalDate lastUpdated;
        private final int rewardPoints;
        
        public ChallengeProgress(UUID challengeId, UUID groupId, double progressPercentage,
                               ChallengeStatus status, LocalDate lastUpdated, int rewardPoints) {
            this.challengeId = challengeId;
            this.groupId = groupId;
            this.progressPercentage = progressPercentage;
            this.status = status;
            this.lastUpdated = lastUpdated;
            this.rewardPoints = rewardPoints;
        }
        
        // Getters
        public UUID getChallengeId() { return challengeId; }
        public UUID getGroupId() { return groupId; }
        public double getProgressPercentage() { return progressPercentage; }
        public ChallengeStatus getStatus() { return status; }
        public LocalDate getLastUpdated() { return lastUpdated; }
        public int getRewardPoints() { return rewardPoints; }
    }
    
    // Enums
    
    public enum ChallengeType {
        STREAK,
        COLLABORATION,
        IMPROVEMENT,
        CONSISTENCY,
        PARTICIPATION
    }
    
    public enum ChallengeMetric {
        GROUP_STREAK,
        SYNERGY_SCORE,
        MOMENTUM_SCORE,
        COHESION_SCORE,
        COMPLETION_RATE,
        PARTICIPATION_RATE
    }
    
    public enum ChallengeStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        FAILED,
        EXPIRED
    }
    
    public enum RewardType {
        POINTS,
        BADGE,
        ACHIEVEMENT,
        BONUS
    }
}