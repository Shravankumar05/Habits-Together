package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.HabitCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating data visualization structures for habit analytics.
 * Creates heatmaps, trend analysis, and correlation matrices for frontend visualization.
 */
@Service
public class VisualizationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualizationEngine.class);
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    @Autowired
    private HabitCorrelationService habitCorrelationService;
    
    /**
     * Generates heatmap data for habit completion patterns.
     */
    public HeatmapData generateHabitHeatmap(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating heatmap for habit {} from {} to {}", habitId, startDate, endDate);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        Map<String, HeatmapCell> heatmapCells = new HashMap<>();
        
        // Create completion map for quick lookup
        Map<LocalDate, Boolean> completionMap = completionData.getCompletions().stream()
            .collect(Collectors.toMap(
                DailyCompletion::getCompletionDate,
                DailyCompletion::getCompleted,
                (existing, replacement) -> existing // Keep first if duplicates
            ));
        
        // Generate heatmap cells for each day in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            boolean completed = completionMap.getOrDefault(currentDate, false);
            
            HeatmapCell cell = new HeatmapCell(
                currentDate,
                completed ? 1.0 : 0.0,
                completed ? "completed" : "missed",
                calculateIntensity(completed)
            );
            
            heatmapCells.put(dateKey, cell);
            currentDate = currentDate.plusDays(1);
        }
        
        return new HeatmapData(habitId, startDate, endDate, heatmapCells);
    }
    
    /**
     * Generates weekly heatmap showing day-of-week patterns.
     */
    public WeeklyHeatmapData generateWeeklyHeatmap(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating weekly heatmap for habit {} from {} to {}", habitId, startDate, endDate);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Group completions by day of week
        Map<DayOfWeek, List<DailyCompletion>> completionsByDay = completionData.getCompletions().stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletionDate().getDayOfWeek()));
        
        Map<DayOfWeek, WeeklyHeatmapCell> weeklyData = new HashMap<>();
        
        for (DayOfWeek day : DayOfWeek.values()) {
            List<DailyCompletion> dayCompletions = completionsByDay.getOrDefault(day, Collections.emptyList());
            
            long totalAttempts = dayCompletions.size();
            long completedAttempts = dayCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double completionRate = totalAttempts > 0 ? (double) completedAttempts / totalAttempts : 0.0;
            
            WeeklyHeatmapCell cell = new WeeklyHeatmapCell(
                day,
                completionRate,
                (int) totalAttempts,
                (int) completedAttempts,
                calculateIntensity(completionRate)
            );
            
            weeklyData.put(day, cell);
        }
        
        return new WeeklyHeatmapData(habitId, startDate, endDate, weeklyData);
    }
    
    /**
     * Generates trend analysis data for habit progress over time.
     */
    public TrendAnalysisData generateTrendAnalysis(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating trend analysis for habit {} from {} to {}", habitId, startDate, endDate);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Calculate weekly completion rates
        List<TrendDataPoint> trendPoints = new ArrayList<>();
        LocalDate weekStart = startDate;
        
        while (!weekStart.isAfter(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }
            
            List<DailyCompletion> weekCompletions = completionData.getCompletions().stream()
                .filter(completion -> !completion.getCompletionDate().isBefore(weekStart))
                .filter(completion -> !completion.getCompletionDate().isAfter(weekEnd))
                .collect(Collectors.toList());
            
            long totalAttempts = weekCompletions.size();
            long completedAttempts = weekCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double completionRate = totalAttempts > 0 ? (double) completedAttempts / totalAttempts : 0.0;
            
            TrendDataPoint point = new TrendDataPoint(
                weekStart,
                completionRate,
                (int) totalAttempts,
                (int) completedAttempts
            );
            
            trendPoints.add(point);
            weekStart = weekStart.plusDays(7);
        }
        
        // Calculate trend direction
        TrendDirection direction = calculateTrendDirection(trendPoints);
        double trendSlope = calculateTrendSlope(trendPoints);
        
        return new TrendAnalysisData(habitId, startDate, endDate, trendPoints, direction, trendSlope);
    }
    
    /**
     * Generates correlation matrix for multiple habits.
     */
    public CorrelationMatrixData generateCorrelationMatrix(UUID userId, List<UUID> habitIds, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating correlation matrix for {} habits", habitIds.size());
        
        if (habitIds.size() < 2) {
            return new CorrelationMatrixData(habitIds, Collections.emptyMap());
        }
        
        HabitCorrelationService.CorrelationMatrix matrix = 
            habitCorrelationService.getCorrelationMatrix(userId, habitIds);
        
        Map<String, CorrelationCell> correlationCells = new HashMap<>();
        
        for (int i = 0; i < habitIds.size(); i++) {
            for (int j = 0; j < habitIds.size(); j++) {
                UUID habit1 = habitIds.get(i);
                UUID habit2 = habitIds.get(j);
                
                double correlation = matrix.getCorrelationValue(habit1, habit2);
                String cellKey = habit1.toString() + ":" + habit2.toString();
                
                CorrelationCell cell = new CorrelationCell(
                    habit1,
                    habit2,
                    correlation,
                    determineCorrelationStrength(correlation),
                    calculateCorrelationIntensity(correlation)
                );
                
                correlationCells.put(cellKey, cell);
            }
        }
        
        return new CorrelationMatrixData(habitIds, correlationCells);
    }
    
    /**
     * Generates multi-habit comparison data.
     */
    public MultiHabitComparisonData generateMultiHabitComparison(UUID userId, List<UUID> habitIds, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating multi-habit comparison for {} habits", habitIds.size());
        
        List<HabitComparisonData> habitComparisons = new ArrayList<>();
        
        for (UUID habitId : habitIds) {
            AnalyticsDataCollector.HabitCompletionData completionData = 
                analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
            
            long totalAttempts = completionData.getCompletions().size();
            long completedAttempts = completionData.getCompletions().stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double completionRate = totalAttempts > 0 ? (double) completedAttempts / totalAttempts : 0.0;
            
            // Calculate streak information
            CompletionDataAggregator.StreakAnalysisResult streakResult = 
                new CompletionDataAggregator().analyzeStreaks(completionData.getCompletions(), habitId);
            
            HabitComparisonData comparison = new HabitComparisonData(
                habitId,
                completionRate,
                (int) totalAttempts,
                (int) completedAttempts,
                streakResult.getCurrentStreak(),
                streakResult.getMaxStreak()
            );
            
            habitComparisons.add(comparison);
        }
        
        return new MultiHabitComparisonData(userId, startDate, endDate, habitComparisons);
    }
    
    /**
     * Generates time-based activity patterns.
     */
    public ActivityPatternData generateActivityPattern(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Generating activity pattern for habit {}", habitId);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Group by hour of day
        Map<Integer, List<DailyCompletion>> completionsByHour = completionData.getCompletions().stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .collect(Collectors.groupingBy(completion -> completion.getCompletedAt().getHour()));
        
        Map<Integer, ActivityPatternCell> hourlyActivity = new HashMap<>();
        
        for (int hour = 0; hour < 24; hour++) {
            List<DailyCompletion> hourCompletions = completionsByHour.getOrDefault(hour, Collections.emptyList());
            
            int totalAttempts = hourCompletions.size();
            int completedAttempts = (int) hourCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double activityLevel = totalAttempts > 0 ? (double) completedAttempts / totalAttempts : 0.0;
            
            ActivityPatternCell cell = new ActivityPatternCell(
                hour,
                activityLevel,
                totalAttempts,
                completedAttempts,
                calculateIntensity(activityLevel)
            );
            
            hourlyActivity.put(hour, cell);
        }
        
        return new ActivityPatternData(habitId, startDate, endDate, hourlyActivity);
    }
    
    // Helper methods
    
    private double calculateIntensity(boolean completed) {
        return completed ? 1.0 : 0.0;
    }
    
    private double calculateIntensity(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
    
    private double calculateCorrelationIntensity(double correlation) {
        return Math.abs(correlation); // Intensity based on absolute correlation value
    }
    
    private TrendDirection calculateTrendDirection(List<TrendDataPoint> points) {
        if (points.size() < 2) return TrendDirection.STABLE;
        
        double firstValue = points.get(0).getValue();
        double lastValue = points.get(points.size() - 1).getValue();
        double difference = lastValue - firstValue;
        
        if (Math.abs(difference) < 0.05) return TrendDirection.STABLE;
        return difference > 0 ? TrendDirection.IMPROVING : TrendDirection.DECLINING;
    }
    
    private double calculateTrendSlope(List<TrendDataPoint> points) {
        if (points.size() < 2) return 0.0;
        
        // Simple linear regression slope calculation
        int n = points.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i; // Use index as x value
            double y = points.get(i).getValue();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return slope;
    }
    
    private CorrelationStrength determineCorrelationStrength(double correlation) {
        double abs = Math.abs(correlation);
        if (abs >= 0.7) return CorrelationStrength.STRONG;
        if (abs >= 0.4) return CorrelationStrength.MODERATE;
        if (abs >= 0.1) return CorrelationStrength.WEAK;
        return CorrelationStrength.NONE;
    }
    
    // Data Transfer Objects
    
    public static class HeatmapData {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<String, HeatmapCell> cells;
        
        public HeatmapData(UUID habitId, LocalDate startDate, LocalDate endDate, Map<String, HeatmapCell> cells) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.cells = cells;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<String, HeatmapCell> getCells() { return cells; }
    }
    
    public static class HeatmapCell {
        private final LocalDate date;
        private final double value;
        private final String status;
        private final double intensity;
        
        public HeatmapCell(LocalDate date, double value, String status, double intensity) {
            this.date = date;
            this.value = value;
            this.status = status;
            this.intensity = intensity;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public double getValue() { return value; }
        public String getStatus() { return status; }
        public double getIntensity() { return intensity; }
    }
    
    public static class WeeklyHeatmapData {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<DayOfWeek, WeeklyHeatmapCell> weeklyData;
        
        public WeeklyHeatmapData(UUID habitId, LocalDate startDate, LocalDate endDate, Map<DayOfWeek, WeeklyHeatmapCell> weeklyData) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.weeklyData = weeklyData;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<DayOfWeek, WeeklyHeatmapCell> getWeeklyData() { return weeklyData; }
    }
    
    public static class WeeklyHeatmapCell {
        private final DayOfWeek dayOfWeek;
        private final double completionRate;
        private final int totalAttempts;
        private final int completedAttempts;
        private final double intensity;
        
        public WeeklyHeatmapCell(DayOfWeek dayOfWeek, double completionRate, int totalAttempts, int completedAttempts, double intensity) {
            this.dayOfWeek = dayOfWeek;
            this.completionRate = completionRate;
            this.totalAttempts = totalAttempts;
            this.completedAttempts = completedAttempts;
            this.intensity = intensity;
        }
        
        // Getters
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public double getCompletionRate() { return completionRate; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCompletedAttempts() { return completedAttempts; }
        public double getIntensity() { return intensity; }
    }
    
    public static class TrendAnalysisData {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<TrendDataPoint> dataPoints;
        private final TrendDirection direction;
        private final double slope;
        
        public TrendAnalysisData(UUID habitId, LocalDate startDate, LocalDate endDate, List<TrendDataPoint> dataPoints, TrendDirection direction, double slope) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.dataPoints = dataPoints;
            this.direction = direction;
            this.slope = slope;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<TrendDataPoint> getDataPoints() { return dataPoints; }
        public TrendDirection getDirection() { return direction; }
        public double getSlope() { return slope; }
    }
    
    public static class TrendDataPoint {
        private final LocalDate date;
        private final double value;
        private final int totalAttempts;
        private final int completedAttempts;
        
        public TrendDataPoint(LocalDate date, double value, int totalAttempts, int completedAttempts) {
            this.date = date;
            this.value = value;
            this.totalAttempts = totalAttempts;
            this.completedAttempts = completedAttempts;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public double getValue() { return value; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCompletedAttempts() { return completedAttempts; }
    }
    
    public static class CorrelationMatrixData {
        private final List<UUID> habitIds;
        private final Map<String, CorrelationCell> correlations;
        
        public CorrelationMatrixData(List<UUID> habitIds, Map<String, CorrelationCell> correlations) {
            this.habitIds = habitIds;
            this.correlations = correlations;
        }
        
        // Getters
        public List<UUID> getHabitIds() { return habitIds; }
        public Map<String, CorrelationCell> getCorrelations() { return correlations; }
    }
    
    public static class CorrelationCell {
        private final UUID habit1Id;
        private final UUID habit2Id;
        private final double correlation;
        private final CorrelationStrength strength;
        private final double intensity;
        
        public CorrelationCell(UUID habit1Id, UUID habit2Id, double correlation, CorrelationStrength strength, double intensity) {
            this.habit1Id = habit1Id;
            this.habit2Id = habit2Id;
            this.correlation = correlation;
            this.strength = strength;
            this.intensity = intensity;
        }
        
        // Getters
        public UUID getHabit1Id() { return habit1Id; }
        public UUID getHabit2Id() { return habit2Id; }
        public double getCorrelation() { return correlation; }
        public CorrelationStrength getStrength() { return strength; }
        public double getIntensity() { return intensity; }
    }
    
    public static class MultiHabitComparisonData {
        private final UUID userId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<HabitComparisonData> habitComparisons;
        
        public MultiHabitComparisonData(UUID userId, LocalDate startDate, LocalDate endDate, List<HabitComparisonData> habitComparisons) {
            this.userId = userId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.habitComparisons = habitComparisons;
        }
        
        // Getters
        public UUID getUserId() { return userId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<HabitComparisonData> getHabitComparisons() { return habitComparisons; }
    }
    
    public static class HabitComparisonData {
        private final UUID habitId;
        private final double completionRate;
        private final int totalAttempts;
        private final int completedAttempts;
        private final int currentStreak;
        private final int maxStreak;
        
        public HabitComparisonData(UUID habitId, double completionRate, int totalAttempts, int completedAttempts, int currentStreak, int maxStreak) {
            this.habitId = habitId;
            this.completionRate = completionRate;
            this.totalAttempts = totalAttempts;
            this.completedAttempts = completedAttempts;
            this.currentStreak = currentStreak;
            this.maxStreak = maxStreak;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public double getCompletionRate() { return completionRate; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCompletedAttempts() { return completedAttempts; }
        public int getCurrentStreak() { return currentStreak; }
        public int getMaxStreak() { return maxStreak; }
    }
    
    public static class ActivityPatternData {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<Integer, ActivityPatternCell> hourlyActivity;
        
        public ActivityPatternData(UUID habitId, LocalDate startDate, LocalDate endDate, Map<Integer, ActivityPatternCell> hourlyActivity) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.hourlyActivity = hourlyActivity;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<Integer, ActivityPatternCell> getHourlyActivity() { return hourlyActivity; }
    }
    
    public static class ActivityPatternCell {
        private final int hour;
        private final double activityLevel;
        private final int totalAttempts;
        private final int completedAttempts;
        private final double intensity;
        
        public ActivityPatternCell(int hour, double activityLevel, int totalAttempts, int completedAttempts, double intensity) {
            this.hour = hour;
            this.activityLevel = activityLevel;
            this.totalAttempts = totalAttempts;
            this.completedAttempts = completedAttempts;
            this.intensity = intensity;
        }
        
        // Getters
        public int getHour() { return hour; }
        public double getActivityLevel() { return activityLevel; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getCompletedAttempts() { return completedAttempts; }
        public double getIntensity() { return intensity; }
    }
    
    // Enums
    
    public enum TrendDirection {
        IMPROVING,
        DECLINING,
        STABLE
    }
    
    public enum CorrelationStrength {
        STRONG,
        MODERATE,
        WEAK,
        NONE
    }
}