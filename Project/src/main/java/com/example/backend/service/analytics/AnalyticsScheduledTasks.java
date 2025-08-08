package com.example.backend.service.analytics;

import com.example.backend.entity.HabitAnalytics;
import com.example.backend.entity.GroupMetrics;
import com.example.backend.entity.Habit;
import com.example.backend.entity.Group;
import com.example.backend.repository.HabitAnalyticsRepository;
import com.example.backend.repository.GroupMetricsRepository;
import com.example.backend.repository.HabitRepository;
import com.example.backend.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for scheduled analytics data collection and processing tasks.
 * Runs periodic jobs to collect, aggregate, and update analytics data.
 */
@Service
public class AnalyticsScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsScheduledTasks.class);
    
    @Autowired
    private AnalyticsDataCollector dataCollector;
    
    @Autowired
    private CompletionDataAggregator dataAggregator;
    
    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;
    
    @Autowired
    private GroupMetricsRepository groupMetricsRepository;
    
    @Autowired
    private HabitRepository habitRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    /**
     * Runs daily at 2 AM to collect and process habit completion data from the previous day.
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    @Transactional
    public void dailyAnalyticsCollection() {
        logger.info("Starting daily analytics collection task");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate startDate = yesterday.minusDays(30); // Collect last 30 days for analysis
            
            // Process all active habits
            List<Habit> allHabits = habitRepository.findAll();
            int processedHabits = 0;
            
            for (Habit habit : allHabits) {
                try {
                    processHabitAnalytics(habit.getId(), habit.getUserId(), startDate, yesterday);
                    processedHabits++;
                } catch (Exception e) {
                    logger.error("Error processing analytics for habit {}: {}", habit.getId(), e.getMessage());
                }
            }
            
            logger.info("Daily analytics collection completed. Processed {} habits", processedHabits);
            
        } catch (Exception e) {
            logger.error("Error during daily analytics collection: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Runs every hour to collect recent completion data for real-time analytics.
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3600000 ms)
    @Transactional
    public void hourlyDataCollection() {
        logger.debug("Starting hourly data collection task");
        
        try {
            // Collect recent completions for the last 24 hours
            AnalyticsDataCollector.RecentCompletionData recentData = dataCollector.collectRecentCompletions(1);
            
            // Update analytics for habits that had recent activity
            updateRecentHabitAnalytics(recentData);
            
            logger.debug("Hourly data collection completed. Processed {} daily and {} group completions",
                        recentData.getRecentDailyCompletions().size(),
                        recentData.getRecentGroupCompletions().size());
            
        } catch (Exception e) {
            logger.error("Error during hourly data collection: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Runs weekly on Sunday at 3 AM to perform comprehensive analytics updates.
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Weekly on Sunday at 3:00 AM
    @Transactional
    public void weeklyAnalyticsUpdate() {
        logger.info("Starting weekly analytics update task");
        
        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(90); // Analyze last 90 days
            
            // Update all habit analytics with extended data
            List<Habit> allHabits = habitRepository.findAll();
            int updatedHabits = 0;
            
            for (Habit habit : allHabits) {
                try {
                    performComprehensiveHabitAnalysis(habit.getId(), habit.getUserId(), startDate, endDate);
                    updatedHabits++;
                } catch (Exception e) {
                    logger.error("Error in comprehensive analysis for habit {}: {}", habit.getId(), e.getMessage());
                }
            }
            
            // Update group metrics
            updateAllGroupMetrics(startDate, endDate);
            
            logger.info("Weekly analytics update completed. Updated {} habits and group metrics", updatedHabits);
            
        } catch (Exception e) {
            logger.error("Error during weekly analytics update: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Runs monthly on the 1st at 4 AM to perform data cleanup and optimization.
     */
    @Scheduled(cron = "0 0 4 1 * *") // Monthly on 1st at 4:00 AM
    @Transactional
    public void monthlyDataCleanup() {
        logger.info("Starting monthly data cleanup task");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMonths(6);
            
            // Clean up old analytics data that's no longer needed
            cleanupOldAnalyticsData(cutoffTime);
            
            // Optimize analytics calculations for all users
            optimizeAnalyticsData();
            
            logger.info("Monthly data cleanup completed");
            
        } catch (Exception e) {
            logger.error("Error during monthly data cleanup: {}", e.getMessage(), e);
        }
    }
    
    // Private helper methods
    
    private void processHabitAnalytics(UUID habitId, UUID userId, LocalDate startDate, LocalDate endDate) {
        // Collect completion data for the habit
        AnalyticsDataCollector.HabitCompletionData completionData = 
            dataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Aggregate the data
        CompletionDataAggregator.DailyAggregationResult dailyResult = 
            dataAggregator.aggregateDailyCompletions(completionData.getCompletions(), startDate, endDate);
        
        CompletionDataAggregator.StreakAnalysisResult streakResult = 
            dataAggregator.analyzeStreaks(completionData.getCompletions(), habitId);
        
        // Update or create habit analytics
        HabitAnalytics analytics = habitAnalyticsRepository.findByUserIdAndHabitId(userId, habitId)
            .orElse(new HabitAnalytics(userId, habitId));
        
        // Calculate and update analytics metrics
        double successRate = dailyResult.getAverageCompletionRate();
        analytics.setSuccessRate(successRate);
        
        // Calculate consistency score based on daily completion variance
        double consistencyScore = calculateConsistencyScore(dailyResult);
        analytics.setConsistencyScore(consistencyScore);
        
        // Update formation stage based on streak and consistency
        updateFormationStage(analytics, streakResult, consistencyScore);
        
        // Calculate habit strength
        double habitStrength = calculateHabitStrength(successRate, consistencyScore, streakResult.getCurrentStreak());
        analytics.setHabitStrength(habitStrength);
        
        analytics.setLastAnalyzed(LocalDateTime.now());
        
        habitAnalyticsRepository.save(analytics);
        
        logger.debug("Updated analytics for habit {} - Success Rate: {:.2f}, Consistency: {:.2f}, Strength: {:.2f}",
                    habitId, successRate, consistencyScore, habitStrength);
    }
    
    private void updateRecentHabitAnalytics(AnalyticsDataCollector.RecentCompletionData recentData) {
        // Group recent completions by habit
        Map<UUID, List<com.example.backend.entity.DailyCompletion>> completionsByHabit = 
            recentData.getRecentDailyCompletions().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    com.example.backend.entity.DailyCompletion::getHabitId));
        
        // Update analytics for habits with recent activity
        for (Map.Entry<UUID, List<com.example.backend.entity.DailyCompletion>> entry : completionsByHabit.entrySet()) {
            UUID habitId = entry.getKey();
            List<com.example.backend.entity.DailyCompletion> completions = entry.getValue();
            
            if (!completions.isEmpty()) {
                UUID userId = completions.get(0).getUserId();
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7); // Quick update with last week's data
                
                processHabitAnalytics(habitId, userId, startDate, endDate);
            }
        }
    }
    
    private void performComprehensiveHabitAnalysis(UUID habitId, UUID userId, LocalDate startDate, LocalDate endDate) {
        // This is similar to processHabitAnalytics but with more comprehensive analysis
        processHabitAnalytics(habitId, userId, startDate, endDate);
        
        // Additional comprehensive analysis could include:
        // - Time pattern analysis
        // - Correlation analysis with other habits
        // - Optimal timing calculations
        // These will be implemented in subsequent tasks
    }
    
    private void updateAllGroupMetrics(LocalDate startDate, LocalDate endDate) {
        List<Group> allGroups = groupRepository.findAll();
        
        for (Group group : allGroups) {
            try {
                updateGroupMetrics(group.getId(), startDate, endDate);
            } catch (Exception e) {
                logger.error("Error updating metrics for group {}: {}", group.getId(), e.getMessage());
            }
        }
    }
    
    private void updateGroupMetrics(UUID groupId, LocalDate startDate, LocalDate endDate) {
        // Collect group completion data
        AnalyticsDataCollector.GroupCompletionData groupData = 
            dataCollector.collectGroupCompletionData(groupId, startDate, endDate);
        
        // Aggregate group data
        CompletionDataAggregator.GroupAggregationResult groupResult = 
            dataAggregator.aggregateGroupCompletions(groupData.getHabitCompletions(), startDate, endDate);
        
        // Calculate group metrics
        double momentumScore = calculateGroupMomentum(groupResult);
        double cohesionScore = calculateGroupCohesion(groupResult);
        int groupStreak = calculateGroupStreak(groupResult);
        
        // Update or create group metrics
        GroupMetrics metrics = groupMetricsRepository.findTopByGroupIdOrderByCalculatedAtDesc(groupId)
            .orElse(new GroupMetrics(groupId));
        
        metrics.setMomentumScore(momentumScore);
        metrics.setCohesionScore(cohesionScore);
        metrics.setGroupStreak(groupStreak);
        metrics.setCalculatedAt(LocalDateTime.now());
        
        groupMetricsRepository.save(metrics);
        
        logger.debug("Updated group metrics for group {} - Momentum: {:.2f}, Cohesion: {:.2f}, Streak: {}",
                    groupId, momentumScore, cohesionScore, groupStreak);
    }
    
    private void cleanupOldAnalyticsData(LocalDateTime cutoffTime) {
        // Clean up old analytics data
        List<HabitAnalytics> oldAnalytics = habitAnalyticsRepository.findAnalyticsNeedingUpdate(cutoffTime);
        logger.info("Found {} old analytics records to review", oldAnalytics.size());
        
        // Clean up old group metrics
        groupMetricsRepository.deleteOldMetrics(cutoffTime);
        logger.info("Cleaned up old group metrics before {}", cutoffTime);
    }
    
    private void optimizeAnalyticsData() {
        // Perform optimization tasks like recalculating analytics for improved accuracy
        logger.info("Performing analytics data optimization");
        
        // This could include:
        // - Recalculating analytics with improved algorithms
        // - Updating formation stages based on new criteria
        // - Optimizing database indexes
        // - Compacting historical data
    }
    
    // Calculation helper methods
    
    private double calculateConsistencyScore(CompletionDataAggregator.DailyAggregationResult dailyResult) {
        Map<LocalDate, CompletionDataAggregator.CompletionStats> dailyStats = dailyResult.getDailyStats();
        
        if (dailyStats.isEmpty()) {
            return 0.0;
        }
        
        // Calculate variance in daily completion rates
        double mean = dailyStats.values().stream()
            .mapToDouble(CompletionDataAggregator.CompletionStats::getCompletionRate)
            .average()
            .orElse(0.0);
        
        double variance = dailyStats.values().stream()
            .mapToDouble(stats -> Math.pow(stats.getCompletionRate() - mean, 2))
            .average()
            .orElse(0.0);
        
        // Convert variance to consistency score (lower variance = higher consistency)
        return Math.max(0.0, 1.0 - Math.sqrt(variance));
    }
    
    private void updateFormationStage(HabitAnalytics analytics, CompletionDataAggregator.StreakAnalysisResult streakResult, double consistencyScore) {
        int currentStreak = streakResult.getCurrentStreak();
        double successRate = analytics.getSuccessRate();
        
        // Determine formation stage based on streak, success rate, and consistency
        if (currentStreak < 7 || successRate < 0.3) {
            analytics.setFormationStage(HabitAnalytics.FormationStage.INITIATION);
        } else if (currentStreak < 21 || successRate < 0.6 || consistencyScore < 0.5) {
            analytics.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        } else if (currentStreak < 66 || successRate < 0.8 || consistencyScore < 0.7) {
            analytics.setFormationStage(HabitAnalytics.FormationStage.STABILITY);
        } else {
            analytics.setFormationStage(HabitAnalytics.FormationStage.MASTERY);
        }
    }
    
    private double calculateHabitStrength(double successRate, double consistencyScore, int currentStreak) {
        // Weighted combination of success rate, consistency, and streak
        double streakFactor = Math.min(1.0, currentStreak / 66.0); // 66 days is habit formation threshold
        
        return (successRate * 0.4) + (consistencyScore * 0.4) + (streakFactor * 0.2);
    }
    
    private double calculateGroupMomentum(CompletionDataAggregator.GroupAggregationResult groupResult) {
        Map<LocalDate, CompletionDataAggregator.GroupDailyStats> dailyStats = groupResult.getDailyStats();
        
        if (dailyStats.isEmpty()) {
            return 0.0;
        }
        
        // Calculate momentum based on recent completion trends
        List<LocalDate> sortedDates = dailyStats.keySet().stream()
            .sorted()
            .collect(java.util.stream.Collectors.toList());
        
        if (sortedDates.size() < 2) {
            return dailyStats.values().iterator().next().getCompletionRate();
        }
        
        // Calculate trend over time (simple linear trend)
        double recentAverage = sortedDates.stream()
            .skip(Math.max(0, sortedDates.size() - 7)) // Last 7 days
            .mapToDouble(date -> dailyStats.get(date).getCompletionRate())
            .average()
            .orElse(0.0);
        
        return Math.min(1.0, recentAverage);
    }
    
    private double calculateGroupCohesion(CompletionDataAggregator.GroupAggregationResult groupResult) {
        Map<LocalDate, CompletionDataAggregator.GroupDailyStats> dailyStats = groupResult.getDailyStats();
        
        if (dailyStats.isEmpty()) {
            return 0.0;
        }
        
        // Calculate cohesion based on participation consistency across habits
        double averageParticipation = dailyStats.values().stream()
            .flatMap(stats -> stats.getHabitParticipation().values().stream())
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        // Normalize to 0-1 range
        return Math.min(1.0, averageParticipation / 10.0); // Assuming max 10 participants per habit
    }
    
    private int calculateGroupStreak(CompletionDataAggregator.GroupAggregationResult groupResult) {
        Map<LocalDate, CompletionDataAggregator.GroupDailyStats> dailyStats = groupResult.getDailyStats();
        
        if (dailyStats.isEmpty()) {
            return 0;
        }
        
        // Calculate current streak of days with above-average completion rates
        List<LocalDate> sortedDates = dailyStats.keySet().stream()
            .sorted(java.util.Collections.reverseOrder()) // Most recent first
            .collect(java.util.stream.Collectors.toList());
        
        double averageRate = dailyStats.values().stream()
            .mapToDouble(CompletionDataAggregator.GroupDailyStats::getCompletionRate)
            .average()
            .orElse(0.0);
        
        int streak = 0;
        for (LocalDate date : sortedDates) {
            if (dailyStats.get(date).getCompletionRate() >= averageRate) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
}