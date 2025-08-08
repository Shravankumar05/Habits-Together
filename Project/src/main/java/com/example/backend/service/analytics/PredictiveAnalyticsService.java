package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.HabitAnalytics;
import com.example.backend.repository.HabitAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for predictive analytics including forecasting and anomaly detection.
 * Provides future performance insights and identifies unusual patterns.
 */
@Service
public class PredictiveAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(PredictiveAnalyticsService.class);
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;
    
    /**
     * Predicts future habit success probability.
     */
    public HabitForecast predictHabitSuccess(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate, int forecastDays) {
        logger.debug("Predicting habit success for habit {} for {} days", habitId, forecastDays);
        
        // Collect historical data
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Calculate historical success rate
        double historicalSuccessRate = calculateHistoricalSuccessRate(completionData.getCompletions());
        
        // Calculate trend
        double trend = calculateTrend(completionData.getCompletions());
        
        // Generate predictions
        List<ForecastPoint> predictions = generateForecastPoints(
            endDate, forecastDays, historicalSuccessRate, trend);
        
        // Calculate confidence
        double confidence = calculateForecastConfidence(completionData.getCompletions(), trend);
        
        return new HabitForecast(habitId, endDate, forecastDays, predictions, confidence, trend);
    }
    
    /**
     * Detects anomalies in habit patterns.
     */
    public AnomalyDetectionResult detectAnomalies(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Detecting anomalies for habit {} from {} to {}", habitId, startDate, endDate);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        List<Anomaly> anomalies = new ArrayList<>();
        
        // Detect completion rate anomalies
        anomalies.addAll(detectCompletionRateAnomalies(completionData.getCompletions()));
        
        // Detect timing anomalies
        anomalies.addAll(detectTimingAnomalies(completionData.getCompletions()));
        
        // Detect streak anomalies
        anomalies.addAll(detectStreakAnomalies(completionData.getCompletions()));
        
        return new AnomalyDetectionResult(habitId, startDate, endDate, anomalies);
    }
    
    /**
     * Predicts optimal habit formation timeline.
     */
    public FormationPrediction predictFormationTimeline(UUID userId, UUID habitId) {
        logger.debug("Predicting formation timeline for habit {}", habitId);
        
        Optional<HabitAnalytics> analyticsOpt = habitAnalyticsRepository.findByUserIdAndHabitId(userId, habitId);
        if (analyticsOpt.isEmpty()) {
            return new FormationPrediction(habitId, FormationStage.UNKNOWN, 0, 0.0);
        }
        
        HabitAnalytics analytics = analyticsOpt.get();
        FormationStage currentStage = mapFormationStage(analytics.getFormationStage());
        
        // Predict days to next stage
        int daysToNextStage = predictDaysToNextStage(analytics);
        
        // Calculate formation probability
        double formationProbability = calculateFormationProbability(analytics);
        
        return new FormationPrediction(habitId, currentStage, daysToNextStage, formationProbability);
    }
    
    // Private helper methods
    
    private double calculateHistoricalSuccessRate(List<DailyCompletion> completions) {
        if (completions.isEmpty()) return 0.0;
        
        long successfulCompletions = completions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        return (double) successfulCompletions / completions.size();
    }
    
    private double calculateTrend(List<DailyCompletion> completions) {
        if (completions.size() < 7) return 0.0; // Need at least a week of data
        
        // Sort by date
        List<DailyCompletion> sortedCompletions = completions.stream()
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        // Calculate weekly success rates
        List<Double> weeklyRates = new ArrayList<>();
        for (int i = 0; i < sortedCompletions.size(); i += 7) {
            int endIndex = Math.min(i + 7, sortedCompletions.size());
            List<DailyCompletion> weekCompletions = sortedCompletions.subList(i, endIndex);
            
            double weeklyRate = calculateHistoricalSuccessRate(weekCompletions);
            weeklyRates.add(weeklyRate);
        }
        
        if (weeklyRates.size() < 2) return 0.0;
        
        // Simple linear regression to calculate trend
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = weeklyRates.size();
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = weeklyRates.get(i);
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }
    
    private List<ForecastPoint> generateForecastPoints(LocalDate startDate, int forecastDays, 
                                                      double baseRate, double trend) {
        List<ForecastPoint> points = new ArrayList<>();
        
        for (int i = 1; i <= forecastDays; i++) {
            LocalDate forecastDate = startDate.plusDays(i);
            
            // Apply trend with diminishing effect over time
            double trendEffect = trend * Math.exp(-i / 30.0); // Trend diminishes over time
            double predictedRate = Math.max(0.0, Math.min(1.0, baseRate + trendEffect));
            
            // Add day-of-week adjustment (simplified)
            double dayOfWeekAdjustment = getDayOfWeekAdjustment(forecastDate.getDayOfWeek());
            predictedRate = Math.max(0.0, Math.min(1.0, predictedRate * dayOfWeekAdjustment));
            
            // Calculate confidence (decreases over time)
            double confidence = Math.max(0.1, 0.9 - (i / (double) forecastDays) * 0.5);
            
            points.add(new ForecastPoint(forecastDate, predictedRate, confidence));
        }
        
        return points;
    }
    
    private double calculateForecastConfidence(List<DailyCompletion> completions, double trend) {
        if (completions.size() < 14) return 0.3; // Low confidence with little data
        
        // Base confidence on data amount and trend stability
        double dataConfidence = Math.min(0.8, completions.size() / 30.0);
        double trendConfidence = Math.max(0.2, 1.0 - Math.abs(trend) * 2.0);
        
        return (dataConfidence + trendConfidence) / 2.0;
    }
    
    private double getDayOfWeekAdjustment(DayOfWeek dayOfWeek) {
        // Simplified day-of-week adjustment (weekends typically lower)
        switch (dayOfWeek) {
            case SATURDAY:
            case SUNDAY:
                return 0.85;
            case MONDAY:
                return 0.9;
            default:
                return 1.0;
        }
    }   
 
    private List<Anomaly> detectCompletionRateAnomalies(List<DailyCompletion> completions) {
        List<Anomaly> anomalies = new ArrayList<>();
        
        if (completions.size() < 14) return anomalies; // Need at least 2 weeks of data
        
        // Calculate rolling average
        double overallRate = calculateHistoricalSuccessRate(completions);
        
        // Group by week and detect anomalous weeks
        Map<LocalDate, List<DailyCompletion>> weeklyCompletions = completions.stream()
            .collect(Collectors.groupingBy(completion -> 
                completion.getCompletionDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
        
        for (Map.Entry<LocalDate, List<DailyCompletion>> entry : weeklyCompletions.entrySet()) {
            LocalDate weekStart = entry.getKey();
            List<DailyCompletion> weekCompletions = entry.getValue();
            
            double weeklyRate = calculateHistoricalSuccessRate(weekCompletions);
            double deviation = Math.abs(weeklyRate - overallRate);
            
            if (deviation > 0.3) { // Significant deviation
                AnomalyType type = weeklyRate > overallRate ? AnomalyType.UNUSUALLY_HIGH : AnomalyType.UNUSUALLY_LOW;
                anomalies.add(new Anomaly(weekStart, type, deviation, 
                    String.format("Weekly completion rate %.1f%% vs average %.1f%%", 
                                weeklyRate * 100, overallRate * 100)));
            }
        }
        
        return anomalies;
    }
    
    private List<Anomaly> detectTimingAnomalies(List<DailyCompletion> completions) {
        List<Anomaly> anomalies = new ArrayList<>();
        
        // Filter completions with timing data
        List<DailyCompletion> timedCompletions = completions.stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .filter(DailyCompletion::getCompleted)
            .collect(Collectors.toList());
        
        if (timedCompletions.size() < 10) return anomalies;
        
        // Calculate average completion hour
        double avgHour = timedCompletions.stream()
            .mapToInt(completion -> completion.getCompletedAt().getHour())
            .average()
            .orElse(12.0);
        
        // Detect completions at unusual times
        for (DailyCompletion completion : timedCompletions) {
            int hour = completion.getCompletedAt().getHour();
            double deviation = Math.abs(hour - avgHour);
            
            if (deviation > 6) { // More than 6 hours from average
                anomalies.add(new Anomaly(completion.getCompletionDate(), AnomalyType.UNUSUAL_TIMING, deviation,
                    String.format("Completed at %d:00, average is %.1f:00", hour, avgHour)));
            }
        }
        
        return anomalies;
    }
    
    private List<Anomaly> detectStreakAnomalies(List<DailyCompletion> completions) {
        List<Anomaly> anomalies = new ArrayList<>();
        
        // Calculate streaks
        CompletionDataAggregator aggregator = new CompletionDataAggregator();
        CompletionDataAggregator.StreakAnalysisResult streakResult = 
            aggregator.analyzeStreaks(completions, completions.isEmpty() ? UUID.randomUUID() : completions.get(0).getHabitId());
        
        // Detect unusually long streaks
        for (CompletionDataAggregator.StreakPeriod streak : streakResult.getAllStreaks()) {
            if (streak.getLength() > 21) { // Streaks longer than 3 weeks
                anomalies.add(new Anomaly(streak.getStartDate(), AnomalyType.EXCEPTIONAL_STREAK, streak.getLength(),
                    String.format("Exceptional streak of %d days", streak.getLength())));
            }
        }
        
        return anomalies;
    }
    
    private FormationStage mapFormationStage(HabitAnalytics.FormationStage stage) {
        if (stage == null) return FormationStage.UNKNOWN;
        
        switch (stage) {
            case INITIATION: return FormationStage.INITIATION;
            case LEARNING: return FormationStage.LEARNING;
            case STABILITY: return FormationStage.STABILITY;
            case MASTERY: return FormationStage.MASTERY;
            default: return FormationStage.UNKNOWN;
        }
    }
    
    private int predictDaysToNextStage(HabitAnalytics analytics) {
        if (analytics.getFormationStage() == null) return 0;
        
        double successRate = analytics.getSuccessRate() != null ? analytics.getSuccessRate() : 0.0;
        double consistencyScore = analytics.getConsistencyScore() != null ? analytics.getConsistencyScore() : 0.0;
        
        switch (analytics.getFormationStage()) {
            case INITIATION:
                // Need 7-21 days to reach learning stage
                return (int) (21 - (successRate * 14));
            case LEARNING:
                // Need 21-66 days to reach stability
                return (int) (45 - (consistencyScore * 24));
            case STABILITY:
                // Need 66+ days to reach mastery
                return (int) (30 - (successRate * consistencyScore * 20));
            case MASTERY:
                return 0; // Already at final stage
            default:
                return 0;
        }
    }
    
    private double calculateFormationProbability(HabitAnalytics analytics) {
        if (analytics.getFormationStage() == null) return 0.0;
        
        double successRate = analytics.getSuccessRate() != null ? analytics.getSuccessRate() : 0.0;
        double consistencyScore = analytics.getConsistencyScore() != null ? analytics.getConsistencyScore() : 0.0;
        double habitStrength = analytics.getHabitStrength() != null ? analytics.getHabitStrength() : 0.0;
        
        // Weighted combination of factors
        return (successRate * 0.4) + (consistencyScore * 0.4) + (habitStrength * 0.2);
    }
    
    // Data Transfer Objects
    
    public static class HabitForecast {
        private final UUID habitId;
        private final LocalDate forecastStartDate;
        private final int forecastDays;
        private final List<ForecastPoint> predictions;
        private final double confidence;
        private final double trend;
        
        public HabitForecast(UUID habitId, LocalDate forecastStartDate, int forecastDays, 
                           List<ForecastPoint> predictions, double confidence, double trend) {
            this.habitId = habitId;
            this.forecastStartDate = forecastStartDate;
            this.forecastDays = forecastDays;
            this.predictions = predictions;
            this.confidence = confidence;
            this.trend = trend;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getForecastStartDate() { return forecastStartDate; }
        public int getForecastDays() { return forecastDays; }
        public List<ForecastPoint> getPredictions() { return predictions; }
        public double getConfidence() { return confidence; }
        public double getTrend() { return trend; }
    }
    
    public static class ForecastPoint {
        private final LocalDate date;
        private final double predictedSuccessRate;
        private final double confidence;
        
        public ForecastPoint(LocalDate date, double predictedSuccessRate, double confidence) {
            this.date = date;
            this.predictedSuccessRate = predictedSuccessRate;
            this.confidence = confidence;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public double getPredictedSuccessRate() { return predictedSuccessRate; }
        public double getConfidence() { return confidence; }
    }
    
    public static class AnomalyDetectionResult {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<Anomaly> anomalies;
        
        public AnomalyDetectionResult(UUID habitId, LocalDate startDate, LocalDate endDate, List<Anomaly> anomalies) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.anomalies = anomalies;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<Anomaly> getAnomalies() { return anomalies; }
    }
    
    public static class Anomaly {
        private final LocalDate date;
        private final AnomalyType type;
        private final double severity;
        private final String description;
        
        public Anomaly(LocalDate date, AnomalyType type, double severity, String description) {
            this.date = date;
            this.type = type;
            this.severity = severity;
            this.description = description;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public AnomalyType getType() { return type; }
        public double getSeverity() { return severity; }
        public String getDescription() { return description; }
    }
    
    public static class FormationPrediction {
        private final UUID habitId;
        private final FormationStage currentStage;
        private final int daysToNextStage;
        private final double formationProbability;
        
        public FormationPrediction(UUID habitId, FormationStage currentStage, int daysToNextStage, double formationProbability) {
            this.habitId = habitId;
            this.currentStage = currentStage;
            this.daysToNextStage = daysToNextStage;
            this.formationProbability = formationProbability;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public FormationStage getCurrentStage() { return currentStage; }
        public int getDaysToNextStage() { return daysToNextStage; }
        public double getFormationProbability() { return formationProbability; }
    }
    
    // Enums
    
    public enum AnomalyType {
        UNUSUALLY_HIGH,
        UNUSUALLY_LOW,
        UNUSUAL_TIMING,
        EXCEPTIONAL_STREAK,
        PATTERN_BREAK
    }
    
    public enum FormationStage {
        UNKNOWN,
        INITIATION,
        LEARNING,
        STABILITY,
        MASTERY
    }
}