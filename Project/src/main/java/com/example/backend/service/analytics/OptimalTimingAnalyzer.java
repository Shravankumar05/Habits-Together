package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing optimal timing patterns for habit completion.
 * Identifies best completion times based on historical success data.
 */
@Service
public class OptimalTimingAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimalTimingAnalyzer.class);
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    /**
     * Analyzes optimal timing for a specific habit.
     */
    public OptimalTimingResult analyzeOptimalTiming(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Analyzing optimal timing for habit {} from {} to {}", habitId, startDate, endDate);
        
        // Collect completion data for the habit
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        List<DailyCompletion> completions = completionData.getCompletions().stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .filter(DailyCompletion::getCompleted)
            .collect(Collectors.toList());
        
        if (completions.isEmpty()) {
            return new OptimalTimingResult(habitId, null, null, Collections.emptyMap(), Collections.emptyMap());
        }
        
        // Analyze hourly patterns
        Map<Integer, TimingStats> hourlyStats = analyzeHourlyPatterns(completions);
        
        // Analyze day-of-week patterns
        Map<DayOfWeek, TimingStats> dayOfWeekStats = analyzeDayOfWeekPatterns(completions);
        
        // Find optimal time window
        TimeWindow optimalWindow = findOptimalTimeWindow(hourlyStats);
        
        logger.debug("Found optimal timing for habit {}: {} - {}", habitId, 
                    optimalWindow.getStartTime(), optimalWindow.getEndTime());
        
        return new OptimalTimingResult(habitId, optimalWindow.getStartTime(), optimalWindow.getEndTime(), 
                                     hourlyStats, dayOfWeekStats);
    }
    
    /**
     * Predicts success probability for different time periods.
     */
    public SuccessPredictionResult predictSuccessForTiming(UUID userId, UUID habitId, 
                                                          LocalTime proposedTime, DayOfWeek dayOfWeek,
                                                          LocalDate startDate, LocalDate endDate) {
        logger.debug("Predicting success for habit {} at {} on {}", habitId, proposedTime, dayOfWeek);
        
        OptimalTimingResult timingResult = analyzeOptimalTiming(userId, habitId, startDate, endDate);
        
        // Get stats for the proposed hour
        int proposedHour = proposedTime.getHour();
        TimingStats hourlyStats = timingResult.getHourlyStats().get(proposedHour);
        TimingStats dayStats = timingResult.getDayOfWeekStats().get(dayOfWeek);
        
        double hourlySuccessRate = hourlyStats != null ? hourlyStats.getSuccessRate() : 0.0;
        double daySuccessRate = dayStats != null ? dayStats.getSuccessRate() : 0.0;
        
        // Calculate combined prediction (weighted average)
        double combinedPrediction = (hourlySuccessRate * 0.7) + (daySuccessRate * 0.3);
        
        // Calculate confidence based on sample size
        int hourlySampleSize = hourlyStats != null ? hourlyStats.getTotalAttempts() : 0;
        int daySampleSize = dayStats != null ? dayStats.getTotalAttempts() : 0;
        double confidence = calculateConfidence(hourlySampleSize + daySampleSize);
        
        return new SuccessPredictionResult(proposedTime, dayOfWeek, combinedPrediction, confidence, 
                                         hourlySuccessRate, daySuccessRate);
    }
    
    /**
     * Finds the best time windows for habit completion.
     */
    public List<TimeWindow> findBestTimeWindows(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate, int windowCount) {
        OptimalTimingResult timingResult = analyzeOptimalTiming(userId, habitId, startDate, endDate);
        
        // Sort hours by success rate
        List<Map.Entry<Integer, TimingStats>> sortedHours = timingResult.getHourlyStats().entrySet().stream()
            .filter(entry -> entry.getValue().getTotalAttempts() >= 3) // Minimum sample size
            .sorted((e1, e2) -> Double.compare(e2.getValue().getSuccessRate(), e1.getValue().getSuccessRate()))
            .collect(Collectors.toList());
        
        List<TimeWindow> windows = new ArrayList<>();
        
        // Create time windows from top performing hours
        for (int i = 0; i < Math.min(windowCount, sortedHours.size()); i++) {
            int hour = sortedHours.get(i).getKey();
            TimingStats stats = sortedHours.get(i).getValue();
            
            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = LocalTime.of(hour, 59);
            
            windows.add(new TimeWindow(startTime, endTime, stats.getSuccessRate(), stats.getTotalAttempts()));
        }
        
        return windows;
    }
    
    /**
     * Analyzes timing patterns across different days of the week.
     */
    public WeeklyTimingPattern analyzeWeeklyTimingPattern(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Analyzing weekly timing pattern for habit {}", habitId);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        List<DailyCompletion> completions = completionData.getCompletions().stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .filter(DailyCompletion::getCompleted)
            .collect(Collectors.toList());
        
        Map<DayOfWeek, Map<Integer, TimingStats>> weeklyPattern = new HashMap<>();
        
        // Initialize pattern for all days
        for (DayOfWeek day : DayOfWeek.values()) {
            weeklyPattern.put(day, new HashMap<>());
        }
        
        // Group completions by day of week and hour
        Map<DayOfWeek, List<DailyCompletion>> completionsByDay = completions.stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletionDate().getDayOfWeek()));
        
        for (Map.Entry<DayOfWeek, List<DailyCompletion>> entry : completionsByDay.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<DailyCompletion> dayCompletions = entry.getValue();
            
            Map<Integer, TimingStats> hourlyStats = analyzeHourlyPatterns(dayCompletions);
            weeklyPattern.put(day, hourlyStats);
        }
        
        return new WeeklyTimingPattern(habitId, weeklyPattern);
    }
    
    // Private helper methods
    
    private Map<Integer, TimingStats> analyzeHourlyPatterns(List<DailyCompletion> completions) {
        Map<Integer, List<DailyCompletion>> completionsByHour = completions.stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletedAt().getHour()));
        
        Map<Integer, TimingStats> hourlyStats = new HashMap<>();
        
        for (int hour = 0; hour < 24; hour++) {
            List<DailyCompletion> hourCompletions = completionsByHour.getOrDefault(hour, Collections.emptyList());
            
            int totalAttempts = hourCompletions.size();
            int successfulAttempts = (int) hourCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double successRate = totalAttempts > 0 ? (double) successfulAttempts / totalAttempts : 0.0;
            
            hourlyStats.put(hour, new TimingStats(totalAttempts, successfulAttempts, successRate));
        }
        
        return hourlyStats;
    }
    
    private Map<DayOfWeek, TimingStats> analyzeDayOfWeekPatterns(List<DailyCompletion> completions) {
        Map<DayOfWeek, List<DailyCompletion>> completionsByDay = completions.stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletionDate().getDayOfWeek()));
        
        Map<DayOfWeek, TimingStats> dayStats = new HashMap<>();
        
        for (DayOfWeek day : DayOfWeek.values()) {
            List<DailyCompletion> dayCompletions = completionsByDay.getOrDefault(day, Collections.emptyList());
            
            int totalAttempts = dayCompletions.size();
            int successfulAttempts = (int) dayCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double successRate = totalAttempts > 0 ? (double) successfulAttempts / totalAttempts : 0.0;
            
            dayStats.put(day, new TimingStats(totalAttempts, successfulAttempts, successRate));
        }
        
        return dayStats;
    }
    
    private TimeWindow findOptimalTimeWindow(Map<Integer, TimingStats> hourlyStats) {
        // Find the hour with the highest success rate (minimum 3 attempts)
        Optional<Map.Entry<Integer, TimingStats>> bestHour = hourlyStats.entrySet().stream()
            .filter(entry -> entry.getValue().getTotalAttempts() >= 3)
            .max(Comparator.comparing(entry -> entry.getValue().getSuccessRate()));
        
        if (bestHour.isPresent()) {
            int hour = bestHour.get().getKey();
            TimingStats stats = bestHour.get().getValue();
            
            // Create a 2-hour window centered on the best hour
            int startHour = Math.max(0, hour - 1);
            int endHour = Math.min(23, hour + 1);
            
            return new TimeWindow(
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 59),
                stats.getSuccessRate(),
                stats.getTotalAttempts()
            );
        }
        
        // Default to morning window if no data
        return new TimeWindow(LocalTime.of(8, 0), LocalTime.of(10, 0), 0.0, 0);
    }
    
    private double calculateConfidence(int sampleSize) {
        // Simple confidence calculation based on sample size
        if (sampleSize >= 30) return 0.95;
        if (sampleSize >= 20) return 0.85;
        if (sampleSize >= 10) return 0.75;
        if (sampleSize >= 5) return 0.60;
        return 0.40;
    }
    
    // Data Transfer Objects
    
    public static class OptimalTimingResult {
        private final UUID habitId;
        private final LocalTime optimalStartTime;
        private final LocalTime optimalEndTime;
        private final Map<Integer, TimingStats> hourlyStats;
        private final Map<DayOfWeek, TimingStats> dayOfWeekStats;
        
        public OptimalTimingResult(UUID habitId, LocalTime optimalStartTime, LocalTime optimalEndTime,
                                 Map<Integer, TimingStats> hourlyStats, Map<DayOfWeek, TimingStats> dayOfWeekStats) {
            this.habitId = habitId;
            this.optimalStartTime = optimalStartTime;
            this.optimalEndTime = optimalEndTime;
            this.hourlyStats = hourlyStats;
            this.dayOfWeekStats = dayOfWeekStats;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalTime getOptimalStartTime() { return optimalStartTime; }
        public LocalTime getOptimalEndTime() { return optimalEndTime; }
        public Map<Integer, TimingStats> getHourlyStats() { return hourlyStats; }
        public Map<DayOfWeek, TimingStats> getDayOfWeekStats() { return dayOfWeekStats; }
    }
    
    public static class TimingStats {
        private final int totalAttempts;
        private final int successfulAttempts;
        private final double successRate;
        
        public TimingStats(int totalAttempts, int successfulAttempts, double successRate) {
            this.totalAttempts = totalAttempts;
            this.successfulAttempts = successfulAttempts;
            this.successRate = successRate;
        }
        
        // Getters
        public int getTotalAttempts() { return totalAttempts; }
        public int getSuccessfulAttempts() { return successfulAttempts; }
        public double getSuccessRate() { return successRate; }
    }
    
    public static class TimeWindow {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final double successRate;
        private final int sampleSize;
        
        public TimeWindow(LocalTime startTime, LocalTime endTime, double successRate, int sampleSize) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.successRate = successRate;
            this.sampleSize = sampleSize;
        }
        
        // Getters
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public double getSuccessRate() { return successRate; }
        public int getSampleSize() { return sampleSize; }
    }
    
    public static class SuccessPredictionResult {
        private final LocalTime proposedTime;
        private final DayOfWeek dayOfWeek;
        private final double predictedSuccessRate;
        private final double confidence;
        private final double hourlySuccessRate;
        private final double daySuccessRate;
        
        public SuccessPredictionResult(LocalTime proposedTime, DayOfWeek dayOfWeek, double predictedSuccessRate,
                                     double confidence, double hourlySuccessRate, double daySuccessRate) {
            this.proposedTime = proposedTime;
            this.dayOfWeek = dayOfWeek;
            this.predictedSuccessRate = predictedSuccessRate;
            this.confidence = confidence;
            this.hourlySuccessRate = hourlySuccessRate;
            this.daySuccessRate = daySuccessRate;
        }
        
        // Getters
        public LocalTime getProposedTime() { return proposedTime; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public double getPredictedSuccessRate() { return predictedSuccessRate; }
        public double getConfidence() { return confidence; }
        public double getHourlySuccessRate() { return hourlySuccessRate; }
        public double getDaySuccessRate() { return daySuccessRate; }
    }
    
    public static class WeeklyTimingPattern {
        private final UUID habitId;
        private final Map<DayOfWeek, Map<Integer, TimingStats>> weeklyPattern;
        
        public WeeklyTimingPattern(UUID habitId, Map<DayOfWeek, Map<Integer, TimingStats>> weeklyPattern) {
            this.habitId = habitId;
            this.weeklyPattern = weeklyPattern;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public Map<DayOfWeek, Map<Integer, TimingStats>> getWeeklyPattern() { return weeklyPattern; }
        
        public TimingStats getTimingStats(DayOfWeek day, int hour) {
            return weeklyPattern.getOrDefault(day, Collections.emptyMap()).get(hour);
        }
        
        public Optional<TimeWindow> getBestTimeForDay(DayOfWeek day) {
            Map<Integer, TimingStats> dayStats = weeklyPattern.get(day);
            if (dayStats == null || dayStats.isEmpty()) {
                return Optional.empty();
            }
            
            Optional<Map.Entry<Integer, TimingStats>> bestHour = dayStats.entrySet().stream()
                .filter(entry -> entry.getValue().getTotalAttempts() >= 2)
                .max(Comparator.comparing(entry -> entry.getValue().getSuccessRate()));
            
            if (bestHour.isPresent()) {
                int hour = bestHour.get().getKey();
                TimingStats stats = bestHour.get().getValue();
                
                return Optional.of(new TimeWindow(
                    LocalTime.of(hour, 0),
                    LocalTime.of(hour, 59),
                    stats.getSuccessRate(),
                    stats.getTotalAttempts()
                ));
            }
            
            return Optional.empty();
        }
    }
}