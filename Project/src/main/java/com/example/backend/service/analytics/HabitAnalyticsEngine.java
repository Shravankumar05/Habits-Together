package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core analytics calculation engine for habit analysis.
 * Provides methods for calculating success rates, consistency scores, and pattern detection.
 */
@Service
public class HabitAnalyticsEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(HabitAnalyticsEngine.class);
    
    /**
     * Calculates the success rate for a habit based on completion data.
     */
    public double calculateSuccessRate(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        if (completions.isEmpty()) {
            return 0.0;
        }
        
        // Count total expected days
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // Count completed days
        long completedDays = completions.stream()
            .filter(completion -> completion.getCompleted())
            .filter(completion -> !completion.getCompletionDate().isBefore(startDate))
            .filter(completion -> !completion.getCompletionDate().isAfter(endDate))
            .count();
        
        double successRate = totalDays > 0 ? (double) completedDays / totalDays : 0.0;
        
        logger.debug("Calculated success rate: {}/{} = {:.2f}", completedDays, totalDays, successRate);
        return Math.min(1.0, successRate);
    }
    
    /**
     * Calculates consistency score based on completion patterns.
     * Higher scores indicate more consistent habit performance.
     */
    public double calculateConsistencyScore(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        if (completions.isEmpty()) {
            return 0.0;
        }
        
        // Group completions by week
        Map<LocalDate, List<DailyCompletion>> weeklyCompletions = groupCompletionsByWeek(completions, startDate, endDate);
        
        if (weeklyCompletions.size() < 2) {
            // Need at least 2 weeks for consistency calculation
            return calculateBasicConsistency(completions, startDate, endDate);
        }
        
        // Calculate weekly completion rates
        List<Double> weeklyRates = weeklyCompletions.values().stream()
            .map(weekCompletions -> {
                long completed = weekCompletions.stream()
                    .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                    .sum();
                return weekCompletions.size() > 0 ? (double) completed / weekCompletions.size() : 0.0;
            })
            .collect(Collectors.toList());
        
        // Calculate coefficient of variation (lower = more consistent)
        double mean = weeklyRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (mean == 0.0) {
            return 0.0;
        }
        
        double variance = weeklyRates.stream()
            .mapToDouble(rate -> Math.pow(rate - mean, 2))
            .average()
            .orElse(0.0);
        
        double standardDeviation = Math.sqrt(variance);
        double coefficientOfVariation = standardDeviation / mean;
        
        // Convert to consistency score (0-1, where 1 is most consistent)
        double consistencyScore = Math.max(0.0, 1.0 - coefficientOfVariation);
        
        logger.debug("Calculated consistency score: {:.2f} (CV: {:.2f})", consistencyScore, coefficientOfVariation);
        return consistencyScore;
    }
    
    /**
     * Detects completion trends and patterns in habit data.
     */
    public CompletionTrendAnalysis detectCompletionTrends(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        logger.debug("Detecting completion trends from {} to {}", startDate, endDate);
        
        if (completions.isEmpty()) {
            return new CompletionTrendAnalysis(TrendDirection.STABLE, 0.0, Collections.emptyMap(), Collections.emptyList());
        }
        
        // Analyze overall trend
        TrendDirection overallTrend = calculateOverallTrend(completions, startDate, endDate);
        double trendStrength = calculateTrendStrength(completions, startDate, endDate);
        
        // Analyze day-of-week patterns
        Map<DayOfWeek, Double> dayOfWeekPatterns = analyzeDayOfWeekPatterns(completions);
        
        // Detect significant patterns
        List<CompletionPattern> patterns = detectSignificantPatterns(completions, startDate, endDate);
        
        return new CompletionTrendAnalysis(overallTrend, trendStrength, dayOfWeekPatterns, patterns);
    }
    
    /**
     * Calculates habit formation progress based on completion data and behavioral science.
     */
    public HabitFormationAnalysis analyzeHabitFormation(List<DailyCompletion> completions, LocalDate habitStartDate) {
        logger.debug("Analyzing habit formation progress since {}", habitStartDate);
        
        if (completions.isEmpty()) {
            return new HabitFormationAnalysis(FormationStage.INITIATION, 0.0, 0, Collections.emptyList());
        }
        
        // Calculate days since habit started
        long daysSinceStart = ChronoUnit.DAYS.between(habitStartDate, LocalDate.now());
        
        // Calculate current streak
        int currentStreak = calculateCurrentStreak(completions);
        
        // Determine formation stage
        FormationStage stage = determineFormationStage(completions, daysSinceStart, currentStreak);
        
        // Calculate formation progress (0-1)
        double progress = calculateFormationProgress(completions, daysSinceStart, currentStreak);
        
        // Identify formation milestones reached
        List<FormationMilestone> milestones = identifyReachedMilestones(completions, daysSinceStart, currentStreak);
        
        return new HabitFormationAnalysis(stage, progress, currentStreak, milestones);
    }
    
    /**
     * Performs advanced pattern recognition on completion data.
     */
    public PatternRecognitionResult recognizePatterns(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        logger.debug("Performing pattern recognition analysis");
        
        if (completions.isEmpty()) {
            return new PatternRecognitionResult(Collections.emptyList(), Collections.emptyMap());
        }
        
        List<RecognizedPattern> patterns = new ArrayList<>();
        Map<String, Double> patternConfidence = new HashMap<>();
        
        // Detect weekly patterns
        WeeklyPattern weeklyPattern = detectWeeklyPattern(completions);
        if (weeklyPattern.getConfidence() > 0.7) {
            patterns.add(new RecognizedPattern("WEEKLY_CYCLE", weeklyPattern.getDescription(), weeklyPattern.getConfidence()));
            patternConfidence.put("WEEKLY_CYCLE", weeklyPattern.getConfidence());
        }
        
        // Detect streak patterns
        StreakPattern streakPattern = detectStreakPattern(completions);
        if (streakPattern.getConfidence() > 0.6) {
            patterns.add(new RecognizedPattern("STREAK_BEHAVIOR", streakPattern.getDescription(), streakPattern.getConfidence()));
            patternConfidence.put("STREAK_BEHAVIOR", streakPattern.getConfidence());
        }
        
        // Detect seasonal patterns
        SeasonalPattern seasonalPattern = detectSeasonalPattern(completions, startDate, endDate);
        if (seasonalPattern.getConfidence() > 0.5) {
            patterns.add(new RecognizedPattern("SEASONAL_VARIATION", seasonalPattern.getDescription(), seasonalPattern.getConfidence()));
            patternConfidence.put("SEASONAL_VARIATION", seasonalPattern.getConfidence());
        }
        
        // Detect recovery patterns after breaks
        RecoveryPattern recoveryPattern = detectRecoveryPattern(completions);
        if (recoveryPattern.getConfidence() > 0.6) {
            patterns.add(new RecognizedPattern("RECOVERY_BEHAVIOR", recoveryPattern.getDescription(), recoveryPattern.getConfidence()));
            patternConfidence.put("RECOVERY_BEHAVIOR", recoveryPattern.getConfidence());
        }
        
        return new PatternRecognitionResult(patterns, patternConfidence);
    }
    
    // Private helper methods
    
    private Map<LocalDate, List<DailyCompletion>> groupCompletionsByWeek(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        return completions.stream()
            .filter(completion -> !completion.getCompletionDate().isBefore(startDate))
            .filter(completion -> !completion.getCompletionDate().isAfter(endDate))
            .collect(Collectors.groupingBy(completion -> 
                completion.getCompletionDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
    }
    
    private double calculateBasicConsistency(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        // For short periods, calculate consistency based on gap analysis
        List<LocalDate> completionDates = completions.stream()
            .filter(DailyCompletion::getCompleted)
            .map(DailyCompletion::getCompletionDate)
            .sorted()
            .collect(Collectors.toList());
        
        if (completionDates.size() < 2) {
            return completionDates.size() > 0 ? 0.5 : 0.0;
        }
        
        // Calculate average gap between completions
        List<Long> gaps = new ArrayList<>();
        for (int i = 1; i < completionDates.size(); i++) {
            long gap = ChronoUnit.DAYS.between(completionDates.get(i-1), completionDates.get(i));
            gaps.add(gap);
        }
        
        double averageGap = gaps.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double gapVariance = gaps.stream()
            .mapToDouble(gap -> Math.pow(gap - averageGap, 2))
            .average()
            .orElse(0.0);
        
        // Lower variance in gaps = higher consistency
        return Math.max(0.0, 1.0 - (Math.sqrt(gapVariance) / 7.0)); // Normalize by week
    }
    
    private TrendDirection calculateOverallTrend(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        // Split period in half and compare completion rates
        LocalDate midPoint = startDate.plusDays(ChronoUnit.DAYS.between(startDate, endDate) / 2);
        
        List<DailyCompletion> firstHalf = completions.stream()
            .filter(completion -> !completion.getCompletionDate().isAfter(midPoint))
            .collect(Collectors.toList());
        
        List<DailyCompletion> secondHalf = completions.stream()
            .filter(completion -> completion.getCompletionDate().isAfter(midPoint))
            .collect(Collectors.toList());
        
        double firstHalfRate = calculateSuccessRate(firstHalf, startDate, midPoint);
        double secondHalfRate = calculateSuccessRate(secondHalf, midPoint.plusDays(1), endDate);
        
        double difference = secondHalfRate - firstHalfRate;
        
        if (Math.abs(difference) < 0.1) {
            return TrendDirection.STABLE;
        } else if (difference > 0) {
            return TrendDirection.IMPROVING;
        } else {
            return TrendDirection.DECLINING;
        }
    }
    
    private double calculateTrendStrength(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        // Use linear regression to calculate trend strength
        List<DailyCompletion> sortedCompletions = completions.stream()
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        if (sortedCompletions.size() < 3) {
            return 0.0;
        }
        
        // Simple linear regression calculation
        double n = sortedCompletions.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < sortedCompletions.size(); i++) {
            double x = i; // Day index
            double y = sortedCompletions.get(i).getCompleted() ? 1.0 : 0.0;
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return Math.abs(slope); // Strength is absolute value of slope
    }
    
    private Map<DayOfWeek, Double> analyzeDayOfWeekPatterns(List<DailyCompletion> completions) {
        Map<DayOfWeek, List<DailyCompletion>> dayGroups = completions.stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletionDate().getDayOfWeek()));
        
        Map<DayOfWeek, Double> patterns = new HashMap<>();
        
        for (DayOfWeek day : DayOfWeek.values()) {
            List<DailyCompletion> dayCompletions = dayGroups.getOrDefault(day, Collections.emptyList());
            long completed = dayCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double rate = dayCompletions.size() > 0 ? (double) completed / dayCompletions.size() : 0.0;
            patterns.put(day, rate);
        }
        
        return patterns;
    }
    
    private List<CompletionPattern> detectSignificantPatterns(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        List<CompletionPattern> patterns = new ArrayList<>();
        
        // Detect weekend vs weekday patterns
        Map<Boolean, List<DailyCompletion>> weekendSplit = completions.stream()
            .collect(Collectors.partitioningBy(completion -> {
                DayOfWeek day = completion.getCompletionDate().getDayOfWeek();
                return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
            }));
        
        double weekdayRate = calculateCompletionRate(weekendSplit.get(false));
        double weekendRate = calculateCompletionRate(weekendSplit.get(true));
        
        if (Math.abs(weekdayRate - weekendRate) > 0.2) {
            String description = weekdayRate > weekendRate ? 
                "Better performance on weekdays" : "Better performance on weekends";
            patterns.add(new CompletionPattern("WEEKEND_WEEKDAY", description, Math.abs(weekdayRate - weekendRate)));
        }
        
        return patterns;
    }
    
    private double calculateCompletionRate(List<DailyCompletion> completions) {
        if (completions.isEmpty()) {
            return 0.0;
        }
        
        long completed = completions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        return (double) completed / completions.size();
    }
    
    private int calculateCurrentStreak(List<DailyCompletion> completions) {
        List<DailyCompletion> sortedCompletions = completions.stream()
            .filter(DailyCompletion::getCompleted)
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate).reversed())
            .collect(Collectors.toList());
        
        if (sortedCompletions.isEmpty()) {
            return 0;
        }
        
        int streak = 0;
        LocalDate expectedDate = LocalDate.now();
        
        for (DailyCompletion completion : sortedCompletions) {
            if (completion.getCompletionDate().equals(expectedDate) || 
                completion.getCompletionDate().equals(expectedDate.minusDays(1))) {
                streak++;
                expectedDate = completion.getCompletionDate().minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    private FormationStage determineFormationStage(List<DailyCompletion> completions, long daysSinceStart, int currentStreak) {
        double successRate = calculateCompletionRate(completions);
        
        if (daysSinceStart < 7 || successRate < 0.3 || currentStreak < 3) {
            return FormationStage.INITIATION;
        } else if (daysSinceStart < 21 || successRate < 0.6 || currentStreak < 7) {
            return FormationStage.LEARNING;
        } else if (daysSinceStart < 66 || successRate < 0.8 || currentStreak < 21) {
            return FormationStage.STABILITY;
        } else {
            return FormationStage.MASTERY;
        }
    }
    
    private double calculateFormationProgress(List<DailyCompletion> completions, long daysSinceStart, int currentStreak) {
        double timeProgress = Math.min(1.0, daysSinceStart / 66.0); // 66 days for habit formation
        double streakProgress = Math.min(1.0, currentStreak / 21.0); // 21 days for initial formation
        double successProgress = calculateCompletionRate(completions);
        
        return (timeProgress * 0.3) + (streakProgress * 0.4) + (successProgress * 0.3);
    }
    
    private List<FormationMilestone> identifyReachedMilestones(List<DailyCompletion> completions, long daysSinceStart, int currentStreak) {
        List<FormationMilestone> milestones = new ArrayList<>();
        
        if (currentStreak >= 3) {
            milestones.add(new FormationMilestone("FIRST_STREAK", "Completed 3 days in a row", 3));
        }
        if (currentStreak >= 7) {
            milestones.add(new FormationMilestone("WEEK_STREAK", "Completed 1 week consistently", 7));
        }
        if (currentStreak >= 21) {
            milestones.add(new FormationMilestone("HABIT_FORMING", "Completed 21 days - habit forming", 21));
        }
        if (currentStreak >= 66) {
            milestones.add(new FormationMilestone("HABIT_FORMED", "Completed 66 days - habit formed", 66));
        }
        
        return milestones;
    }
    
    // Pattern detection methods
    
    private WeeklyPattern detectWeeklyPattern(List<DailyCompletion> completions) {
        Map<DayOfWeek, Double> dayRates = analyzeDayOfWeekPatterns(completions);
        
        // Check for consistent weekly patterns
        double variance = dayRates.values().stream()
            .mapToDouble(rate -> rate)
            .map(rate -> Math.pow(rate - dayRates.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0), 2))
            .average()
            .orElse(0.0);
        
        double confidence = Math.max(0.0, 1.0 - Math.sqrt(variance));
        
        String description = "Weekly completion pattern detected";
        if (confidence > 0.7) {
            DayOfWeek bestDay = dayRates.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DayOfWeek.MONDAY);
            description = "Best performance on " + bestDay.toString().toLowerCase();
        }
        
        return new WeeklyPattern(description, confidence);
    }
    
    private StreakPattern detectStreakPattern(List<DailyCompletion> completions) {
        // Analyze streak behavior
        List<Integer> streaks = calculateAllStreaks(completions);
        
        if (streaks.isEmpty()) {
            return new StreakPattern("No streak pattern detected", 0.0);
        }
        
        double averageStreak = streaks.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double confidence = Math.min(1.0, averageStreak / 7.0); // Normalize by week
        
        String description = String.format("Average streak length: %.1f days", averageStreak);
        
        return new StreakPattern(description, confidence);
    }
    
    private SeasonalPattern detectSeasonalPattern(List<DailyCompletion> completions, LocalDate startDate, LocalDate endDate) {
        // Simple seasonal analysis - would need more data for accurate detection
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        if (totalDays < 90) {
            return new SeasonalPattern("Insufficient data for seasonal analysis", 0.0);
        }
        
        // Group by month and analyze
        Map<Integer, List<DailyCompletion>> monthlyCompletions = completions.stream()
            .collect(Collectors.groupingBy(completion -> completion.getCompletionDate().getMonthValue()));
        
        if (monthlyCompletions.size() < 3) {
            return new SeasonalPattern("Insufficient months for seasonal analysis", 0.0);
        }
        
        // Calculate monthly completion rates
        Map<Integer, Double> monthlyRates = monthlyCompletions.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> calculateCompletionRate(entry.getValue())
            ));
        
        double variance = monthlyRates.values().stream()
            .mapToDouble(rate -> rate)
            .map(rate -> Math.pow(rate - monthlyRates.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0), 2))
            .average()
            .orElse(0.0);
        
        double confidence = Math.sqrt(variance); // Higher variance = more seasonal variation
        
        return new SeasonalPattern("Seasonal variation detected", Math.min(1.0, confidence));
    }
    
    private RecoveryPattern detectRecoveryPattern(List<DailyCompletion> completions) {
        // Detect how quickly user recovers after missing days
        List<DailyCompletion> sortedCompletions = completions.stream()
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        List<Integer> recoveryTimes = new ArrayList<>();
        boolean inBreak = false;
        int breakLength = 0;
        
        for (int i = 1; i < sortedCompletions.size(); i++) {
            DailyCompletion current = sortedCompletions.get(i);
            DailyCompletion previous = sortedCompletions.get(i - 1);
            
            long daysBetween = ChronoUnit.DAYS.between(previous.getCompletionDate(), current.getCompletionDate());
            
            if (daysBetween > 1 && !inBreak) {
                inBreak = true;
                breakLength = (int) daysBetween - 1;
            } else if (daysBetween == 1 && inBreak && current.getCompleted()) {
                recoveryTimes.add(breakLength);
                inBreak = false;
                breakLength = 0;
            }
        }
        
        if (recoveryTimes.isEmpty()) {
            return new RecoveryPattern("No recovery pattern data", 0.0);
        }
        
        double averageRecovery = recoveryTimes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double confidence = Math.max(0.0, 1.0 - (averageRecovery / 7.0)); // Better recovery = higher confidence
        
        String description = String.format("Average recovery time: %.1f days", averageRecovery);
        
        return new RecoveryPattern(description, confidence);
    }
    
    private List<Integer> calculateAllStreaks(List<DailyCompletion> completions) {
        List<DailyCompletion> sortedCompletions = completions.stream()
            .filter(DailyCompletion::getCompleted)
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        List<Integer> streaks = new ArrayList<>();
        int currentStreak = 0;
        LocalDate previousDate = null;
        
        for (DailyCompletion completion : sortedCompletions) {
            if (previousDate == null) {
                currentStreak = 1;
            } else if (ChronoUnit.DAYS.between(previousDate, completion.getCompletionDate()) == 1) {
                currentStreak++;
            } else {
                if (currentStreak > 0) {
                    streaks.add(currentStreak);
                }
                currentStreak = 1;
            }
            previousDate = completion.getCompletionDate();
        }
        
        if (currentStreak > 0) {
            streaks.add(currentStreak);
        }
        
        return streaks;
    }
    
    // Data classes
    
    public enum TrendDirection {
        IMPROVING, DECLINING, STABLE
    }
    
    public enum FormationStage {
        INITIATION, LEARNING, STABILITY, MASTERY
    }
    
    public static class CompletionTrendAnalysis {
        private final TrendDirection overallTrend;
        private final double trendStrength;
        private final Map<DayOfWeek, Double> dayOfWeekPatterns;
        private final List<CompletionPattern> patterns;
        
        public CompletionTrendAnalysis(TrendDirection overallTrend, double trendStrength, 
                                     Map<DayOfWeek, Double> dayOfWeekPatterns, List<CompletionPattern> patterns) {
            this.overallTrend = overallTrend;
            this.trendStrength = trendStrength;
            this.dayOfWeekPatterns = dayOfWeekPatterns;
            this.patterns = patterns;
        }
        
        // Getters
        public TrendDirection getOverallTrend() { return overallTrend; }
        public double getTrendStrength() { return trendStrength; }
        public Map<DayOfWeek, Double> getDayOfWeekPatterns() { return dayOfWeekPatterns; }
        public List<CompletionPattern> getPatterns() { return patterns; }
    }
    
    public static class HabitFormationAnalysis {
        private final FormationStage stage;
        private final double progress;
        private final int currentStreak;
        private final List<FormationMilestone> milestones;
        
        public HabitFormationAnalysis(FormationStage stage, double progress, int currentStreak, List<FormationMilestone> milestones) {
            this.stage = stage;
            this.progress = progress;
            this.currentStreak = currentStreak;
            this.milestones = milestones;
        }
        
        // Getters
        public FormationStage getStage() { return stage; }
        public double getProgress() { return progress; }
        public int getCurrentStreak() { return currentStreak; }
        public List<FormationMilestone> getMilestones() { return milestones; }
    }
    
    public static class PatternRecognitionResult {
        private final List<RecognizedPattern> patterns;
        private final Map<String, Double> patternConfidence;
        
        public PatternRecognitionResult(List<RecognizedPattern> patterns, Map<String, Double> patternConfidence) {
            this.patterns = patterns;
            this.patternConfidence = patternConfidence;
        }
        
        // Getters
        public List<RecognizedPattern> getPatterns() { return patterns; }
        public Map<String, Double> getPatternConfidence() { return patternConfidence; }
    }
    
    public static class CompletionPattern {
        private final String type;
        private final String description;
        private final double significance;
        
        public CompletionPattern(String type, String description, double significance) {
            this.type = type;
            this.description = description;
            this.significance = significance;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public double getSignificance() { return significance; }
    }
    
    public static class FormationMilestone {
        private final String type;
        private final String description;
        private final int daysRequired;
        
        public FormationMilestone(String type, String description, int daysRequired) {
            this.type = type;
            this.description = description;
            this.daysRequired = daysRequired;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public int getDaysRequired() { return daysRequired; }
    }
    
    public static class RecognizedPattern {
        private final String type;
        private final String description;
        private final double confidence;
        
        public RecognizedPattern(String type, String description, double confidence) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
    
    // Pattern classes
    public static class WeeklyPattern {
        private final String description;
        private final double confidence;
        
        public WeeklyPattern(String description, double confidence) {
            this.description = description;
            this.confidence = confidence;
        }
        
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
    
    public static class StreakPattern {
        private final String description;
        private final double confidence;
        
        public StreakPattern(String description, double confidence) {
            this.description = description;
            this.confidence = confidence;
        }
        
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
    
    public static class SeasonalPattern {
        private final String description;
        private final double confidence;
        
        public SeasonalPattern(String description, double confidence) {
            this.description = description;
            this.confidence = confidence;
        }
        
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
    
    public static class RecoveryPattern {
        private final String description;
        private final double confidence;
        
        public RecoveryPattern(String description, double confidence) {
            this.description = description;
            this.confidence = confidence;
        }
        
        public String getDescription() { return description; }
        public double getConfidence() { return confidence; }
    }
}