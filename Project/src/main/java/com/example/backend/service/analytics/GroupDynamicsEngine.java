package com.example.backend.service.analytics;

import com.example.backend.entity.GroupHabitCompletion;
import com.example.backend.entity.GroupHabit;
import com.example.backend.entity.GroupMember;
import com.example.backend.repository.GroupMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating group dynamics metrics including momentum, cohesion, and streaks.
 * Analyzes group participation patterns and member interactions.
 */
@Service
public class GroupDynamicsEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupDynamicsEngine.class);
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    /**
     * Calculates comprehensive group dynamics metrics.
     */
    public GroupDynamicsResult calculateGroupDynamics(UUID groupId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Calculating group dynamics for group {} from {} to {}", groupId, startDate, endDate);
        
        // Collect group completion data
        AnalyticsDataCollector.GroupCompletionData groupData = 
            analyticsDataCollector.collectGroupCompletionData(groupId, startDate, endDate);
        
        // Get group members
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        
        // Calculate individual metrics
        double momentumScore = calculateMomentumScore(groupData, groupMembers);
        double cohesionScore = calculateCohesionScore(groupData, groupMembers);
        int groupStreak = calculateGroupStreak(groupData);
        double synergisticScore = calculateSynergisticScore(groupData, groupMembers);
        
        // Identify key contributors
        List<KeyContributor> keyContributors = identifyKeyContributors(groupData, groupMembers);
        
        // Calculate participation metrics
        ParticipationMetrics participationMetrics = calculateParticipationMetrics(groupData, groupMembers);
        
        GroupDynamicsResult result = new GroupDynamicsResult(
            groupId, startDate, endDate, momentumScore, cohesionScore, 
            groupStreak, synergisticScore, keyContributors, participationMetrics
        );
        
        logger.debug("Group dynamics calculated - Momentum: {:.2f}, Cohesion: {:.2f}, Streak: {}, Synergistic: {:.2f}",
                    momentumScore, cohesionScore, groupStreak, synergisticScore);
        
        return result;
    }
    
    /**
     * Calculates group momentum based on recent activity trends.
     */
    public double calculateMomentumScore(AnalyticsDataCollector.GroupCompletionData groupData, List<GroupMember> members) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        if (habitCompletions.isEmpty() || members.isEmpty()) {
            return 0.0;
        }
        
        // Calculate daily completion rates over time
        Map<LocalDate, Double> dailyRates = new HashMap<>();
        LocalDate currentDate = groupData.getStartDate();
        
        while (!currentDate.isAfter(groupData.getEndDate())) {
            final LocalDate date = currentDate;
            
            int totalPossibleCompletions = habitCompletions.size() * members.size();
            int actualCompletions = 0;
            
            for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
                actualCompletions += (int) completions.stream()
                    .filter(completion -> completion.getCompletionDate().equals(date))
                    .filter(GroupHabitCompletion::getCompleted)
                    .count();
            }
            
            double dailyRate = totalPossibleCompletions > 0 ? 
                (double) actualCompletions / totalPossibleCompletions : 0.0;
            dailyRates.put(currentDate, dailyRate);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Calculate momentum as weighted average with recent days having more weight
        List<LocalDate> sortedDates = dailyRates.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        if (sortedDates.size() < 2) {
            return dailyRates.values().stream().findFirst().orElse(0.0);
        }
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < sortedDates.size(); i++) {
            LocalDate date = sortedDates.get(i);
            double weight = Math.pow(1.1, i); // Recent days get exponentially more weight
            double rate = dailyRates.get(date);
            
            weightedSum += rate * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
    
    /**
     * Calculates group cohesion based on participation consistency.
     */
    public double calculateCohesionScore(AnalyticsDataCollector.GroupCompletionData groupData, List<GroupMember> members) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        if (habitCompletions.isEmpty() || members.isEmpty()) {
            return 0.0;
        }
        
        // Calculate individual participation rates
        Map<UUID, Double> memberParticipationRates = new HashMap<>();
        
        for (GroupMember member : members) {
            int totalPossibleCompletions = 0;
            int actualCompletions = 0;
            
            for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
                List<GroupHabitCompletion> memberCompletions = completions.stream()
                    .filter(completion -> completion.getUserId().equals(member.getUserId()))
                    .collect(Collectors.toList());
                
                totalPossibleCompletions += memberCompletions.size();
                actualCompletions += (int) memberCompletions.stream()
                    .filter(GroupHabitCompletion::getCompleted)
                    .count();
            }
            
            double participationRate = totalPossibleCompletions > 0 ? 
                (double) actualCompletions / totalPossibleCompletions : 0.0;
            memberParticipationRates.put(member.getUserId(), participationRate);
        }
        
        // Calculate cohesion as inverse of participation variance
        if (memberParticipationRates.isEmpty()) {
            return 0.0;
        }
        
        double mean = memberParticipationRates.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double variance = memberParticipationRates.values().stream()
            .mapToDouble(rate -> Math.pow(rate - mean, 2))
            .average()
            .orElse(0.0);
        
        // Convert variance to cohesion score (lower variance = higher cohesion)
        double cohesion = Math.max(0.0, 1.0 - Math.sqrt(variance));
        
        // Boost cohesion if overall participation is high
        double participationBoost = Math.min(0.2, mean * 0.2);
        
        return Math.min(1.0, cohesion + participationBoost);
    }
    
    /**
     * Calculates current group streak.
     */
    public int calculateGroupStreak(AnalyticsDataCollector.GroupCompletionData groupData) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        if (habitCompletions.isEmpty()) {
            return 0;
        }
        
        // Calculate daily group completion rates
        Map<LocalDate, Double> dailyRates = new HashMap<>();
        LocalDate currentDate = groupData.getStartDate();
        
        while (!currentDate.isAfter(groupData.getEndDate())) {
            final LocalDate date = currentDate;
            
            int totalCompletions = 0;
            int totalAttempts = 0;
            
            for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
                List<GroupHabitCompletion> dayCompletions = completions.stream()
                    .filter(completion -> completion.getCompletionDate().equals(date))
                    .collect(Collectors.toList());
                
                totalAttempts += dayCompletions.size();
                totalCompletions += (int) dayCompletions.stream()
                    .filter(GroupHabitCompletion::getCompleted)
                    .count();
            }
            
            double dailyRate = totalAttempts > 0 ? (double) totalCompletions / totalAttempts : 0.0;
            dailyRates.put(currentDate, dailyRate);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Calculate streak from most recent date backwards
        List<LocalDate> sortedDates = dailyRates.keySet().stream()
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());
        
        double averageRate = dailyRates.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double streakThreshold = Math.max(0.5, averageRate); // At least 50% or above average
        
        int streak = 0;
        for (LocalDate date : sortedDates) {
            if (dailyRates.get(date) >= streakThreshold) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    /**
     * Calculates synergistic score based on group collaboration effects.
     */
    public double calculateSynergisticScore(AnalyticsDataCollector.GroupCompletionData groupData, List<GroupMember> members) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        if (habitCompletions.isEmpty() || members.size() < 2) {
            return 0.0;
        }
        
        // Calculate correlation between member activities
        double totalCorrelation = 0.0;
        int correlationCount = 0;
        
        List<UUID> memberIds = members.stream()
            .map(GroupMember::getUserId)
            .collect(Collectors.toList());
        
        // Compare each pair of members
        for (int i = 0; i < memberIds.size(); i++) {
            for (int j = i + 1; j < memberIds.size(); j++) {
                UUID member1Id = memberIds.get(i);
                UUID member2Id = memberIds.get(j);
                
                double correlation = calculateMemberActivityCorrelation(habitCompletions, member1Id, member2Id);
                totalCorrelation += correlation;
                correlationCount++;
            }
        }
        
        double averageCorrelation = correlationCount > 0 ? totalCorrelation / correlationCount : 0.0;
        
        // Convert correlation to synergistic score (positive correlation indicates synergy)
        return Math.max(0.0, (averageCorrelation + 1.0) / 2.0); // Normalize from [-1,1] to [0,1]
    }
    
    /**
     * Identifies key contributors in the group.
     */
    public List<KeyContributor> identifyKeyContributors(AnalyticsDataCollector.GroupCompletionData groupData, List<GroupMember> members) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        Map<UUID, KeyContributorStats> memberStats = new HashMap<>();
        
        // Initialize stats for all members
        for (GroupMember member : members) {
            memberStats.put(member.getUserId(), new KeyContributorStats(member.getUserId()));
        }
        
        // Calculate stats for each member
        for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
            for (GroupHabitCompletion completion : completions) {
                UUID userId = completion.getUserId();
                KeyContributorStats stats = memberStats.get(userId);
                
                if (stats != null) {
                    stats.totalAttempts++;
                    if (completion.getCompleted()) {
                        stats.successfulCompletions++;
                    }
                }
            }
        }
        
        // Calculate contribution scores and identify top contributors
        List<KeyContributor> contributors = memberStats.values().stream()
            .filter(stats -> stats.totalAttempts > 0)
            .map(stats -> {
                double completionRate = (double) stats.successfulCompletions / stats.totalAttempts;
                double contributionScore = calculateContributionScore(stats, memberStats.values());
                
                return new KeyContributor(
                    stats.userId,
                    stats.totalAttempts,
                    stats.successfulCompletions,
                    completionRate,
                    contributionScore,
                    determineContributorType(stats, memberStats.values())
                );
            })
            .sorted((c1, c2) -> Double.compare(c2.getContributionScore(), c1.getContributionScore()))
            .collect(Collectors.toList());
        
        return contributors;
    }
    
    /**
     * Calculates participation metrics for the group.
     */
    public ParticipationMetrics calculateParticipationMetrics(AnalyticsDataCollector.GroupCompletionData groupData, List<GroupMember> members) {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupData.getHabitCompletions();
        
        int totalMembers = members.size();
        int activeMembers = 0;
        int totalCompletions = 0;
        int totalAttempts = 0;
        
        Set<UUID> activeMemberIds = new HashSet<>();
        
        for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
            for (GroupHabitCompletion completion : completions) {
                activeMemberIds.add(completion.getUserId());
                totalAttempts++;
                if (completion.getCompleted()) {
                    totalCompletions++;
                }
            }
        }
        
        activeMembers = activeMemberIds.size();
        double participationRate = totalMembers > 0 ? (double) activeMembers / totalMembers : 0.0;
        double completionRate = totalAttempts > 0 ? (double) totalCompletions / totalAttempts : 0.0;
        
        return new ParticipationMetrics(
            totalMembers,
            activeMembers,
            participationRate,
            totalAttempts,
            totalCompletions,
            completionRate
        );
    }
    
    // Private helper methods
    
    private double calculateMemberActivityCorrelation(Map<UUID, List<GroupHabitCompletion>> habitCompletions, 
                                                     UUID member1Id, UUID member2Id) {
        // Get daily activity for both members
        Map<LocalDate, Boolean> member1Activity = new HashMap<>();
        Map<LocalDate, Boolean> member2Activity = new HashMap<>();
        
        for (List<GroupHabitCompletion> completions : habitCompletions.values()) {
            for (GroupHabitCompletion completion : completions) {
                LocalDate date = completion.getCompletionDate();
                
                if (completion.getUserId().equals(member1Id)) {
                    member1Activity.put(date, completion.getCompleted());
                } else if (completion.getUserId().equals(member2Id)) {
                    member2Activity.put(date, completion.getCompleted());
                }
            }
        }
        
        // Find common dates
        Set<LocalDate> commonDates = new HashSet<>(member1Activity.keySet());
        commonDates.retainAll(member2Activity.keySet());
        
        if (commonDates.size() < 3) { // Need at least 3 data points
            return 0.0;
        }
        
        // Calculate correlation
        List<Boolean> member1Values = commonDates.stream()
            .map(member1Activity::get)
            .collect(Collectors.toList());
        
        List<Boolean> member2Values = commonDates.stream()
            .map(member2Activity::get)
            .collect(Collectors.toList());
        
        return calculateBooleanCorrelation(member1Values, member2Values);
    }
    
    private double calculateBooleanCorrelation(List<Boolean> values1, List<Boolean> values2) {
        if (values1.size() != values2.size() || values1.isEmpty()) {
            return 0.0;
        }
        
        // Convert boolean to double
        List<Double> x = values1.stream().map(b -> b ? 1.0 : 0.0).collect(Collectors.toList());
        List<Double> y = values2.stream().map(b -> b ? 1.0 : 0.0).collect(Collectors.toList());
        
        double meanX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double numerator = 0.0;
        double sumXSquared = 0.0;
        double sumYSquared = 0.0;
        
        for (int i = 0; i < x.size(); i++) {
            double xDiff = x.get(i) - meanX;
            double yDiff = y.get(i) - meanY;
            
            numerator += xDiff * yDiff;
            sumXSquared += xDiff * xDiff;
            sumYSquared += yDiff * yDiff;
        }
        
        double denominator = Math.sqrt(sumXSquared * sumYSquared);
        return denominator > 0 ? numerator / denominator : 0.0;
    }
    
    private double calculateContributionScore(KeyContributorStats stats, Collection<KeyContributorStats> allStats) {
        double completionRate = stats.totalAttempts > 0 ? (double) stats.successfulCompletions / stats.totalAttempts : 0.0;
        
        // Calculate relative performance
        double avgCompletionRate = allStats.stream()
            .filter(s -> s.totalAttempts > 0)
            .mapToDouble(s -> (double) s.successfulCompletions / s.totalAttempts)
            .average()
            .orElse(0.0);
        
        double relativePerformance = avgCompletionRate > 0 ? completionRate / avgCompletionRate : 1.0;
        
        // Calculate activity level
        int maxAttempts = allStats.stream()
            .mapToInt(s -> s.totalAttempts)
            .max()
            .orElse(1);
        
        double activityLevel = (double) stats.totalAttempts / maxAttempts;
        
        // Combine metrics (weighted average)
        return (relativePerformance * 0.6) + (activityLevel * 0.4);
    }
    
    private ContributorType determineContributorType(KeyContributorStats stats, Collection<KeyContributorStats> allStats) {
        double completionRate = stats.totalAttempts > 0 ? (double) stats.successfulCompletions / stats.totalAttempts : 0.0;
        
        double avgCompletionRate = allStats.stream()
            .filter(s -> s.totalAttempts > 0)
            .mapToDouble(s -> (double) s.successfulCompletions / s.totalAttempts)
            .average()
            .orElse(0.0);
        
        int avgAttempts = (int) allStats.stream()
            .mapToInt(s -> s.totalAttempts)
            .average()
            .orElse(0);
        
        if (completionRate > avgCompletionRate * 1.2 && stats.totalAttempts > avgAttempts * 1.2) {
            return ContributorType.LEADER;
        } else if (completionRate > avgCompletionRate * 1.1) {
            return ContributorType.HIGH_PERFORMER;
        } else if (stats.totalAttempts > avgAttempts * 1.2) {
            return ContributorType.ACTIVE_PARTICIPANT;
        } else if (completionRate > avgCompletionRate) {
            return ContributorType.CONSISTENT_CONTRIBUTOR;
        } else {
            return ContributorType.CASUAL_PARTICIPANT;
        }
    }
    
    // Data Transfer Objects
    
    public static class GroupDynamicsResult {
        private final UUID groupId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final double momentumScore;
        private final double cohesionScore;
        private final int groupStreak;
        private final double synergisticScore;
        private final List<KeyContributor> keyContributors;
        private final ParticipationMetrics participationMetrics;
        
        public GroupDynamicsResult(UUID groupId, LocalDate startDate, LocalDate endDate,
                                 double momentumScore, double cohesionScore, int groupStreak,
                                 double synergisticScore, List<KeyContributor> keyContributors,
                                 ParticipationMetrics participationMetrics) {
            this.groupId = groupId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.momentumScore = momentumScore;
            this.cohesionScore = cohesionScore;
            this.groupStreak = groupStreak;
            this.synergisticScore = synergisticScore;
            this.keyContributors = keyContributors;
            this.participationMetrics = participationMetrics;
        }
        
        // Getters
        public UUID getGroupId() { return groupId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public double getMomentumScore() { return momentumScore; }
        public double getCohesionScore() { return cohesionScore; }
        public int getGroupStreak() { return groupStreak; }
        public double getSynergisticScore() { return synergisticScore; }
        public List<KeyContributor> getKeyContributors() { return keyContributors; }
        public ParticipationMetrics getParticipationMetrics() { return participationMetrics; }
    }
    
    public static class KeyContributor {
        private final UUID userId;
        private final int totalAttempts;
        private final int successfulCompletions;
        private final double completionRate;
        private final double contributionScore;
        private final ContributorType contributorType;
        
        public KeyContributor(UUID userId, int totalAttempts, int successfulCompletions,
                            double completionRate, double contributionScore, ContributorType contributorType) {
            this.userId = userId;
            this.totalAttempts = totalAttempts;
            this.successfulCompletions = successfulCompletions;
            this.completionRate = completionRate;
            this.contributionScore = contributionScore;
            this.contributorType = contributorType;
        }
        
        // Getters
        public UUID getUserId() { return userId; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getSuccessfulCompletions() { return successfulCompletions; }
        public double getCompletionRate() { return completionRate; }
        public double getContributionScore() { return contributionScore; }
        public ContributorType getContributorType() { return contributorType; }
    }
    
    public static class ParticipationMetrics {
        private final int totalMembers;
        private final int activeMembers;
        private final double participationRate;
        private final int totalAttempts;
        private final int totalCompletions;
        private final double completionRate;
        
        public ParticipationMetrics(int totalMembers, int activeMembers, double participationRate,
                                  int totalAttempts, int totalCompletions, double completionRate) {
            this.totalMembers = totalMembers;
            this.activeMembers = activeMembers;
            this.participationRate = participationRate;
            this.totalAttempts = totalAttempts;
            this.totalCompletions = totalCompletions;
            this.completionRate = completionRate;
        }
        
        // Getters
        public int getTotalMembers() { return totalMembers; }
        public int getActiveMembers() { return activeMembers; }
        public double getParticipationRate() { return participationRate; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getTotalCompletions() { return totalCompletions; }
        public double getCompletionRate() { return completionRate; }
    }
    
    private static class KeyContributorStats {
        final UUID userId;
        int totalAttempts = 0;
        int successfulCompletions = 0;
        
        KeyContributorStats(UUID userId) {
            this.userId = userId;
        }
    }
    
    public enum ContributorType {
        LEADER,
        HIGH_PERFORMER,
        ACTIVE_PARTICIPANT,
        CONSISTENT_CONTRIBUTOR,
        CASUAL_PARTICIPANT
    }
}