package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.HabitAnalytics;
import com.example.backend.repository.HabitAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing habit formation based on behavioral science principles.
 * Assesses formation stages, habit strength, and trigger effectiveness.
 */
@Service
public class HabitFormationAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(HabitFormationAnalyzer.class);
    
    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;
    
    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;
    
    /**
     * Analyzes habit formation stage based on behavioral science.
     */
    public FormationAnalysisResult analyzeFormationStage(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Analyzing formation stage for habit {} from {} to {}", habitId, startDate, endDate);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Calculate key metrics
        double automaticity = calculateAutomaticity(completionData.getCompletions());
        double consistency = calculateConsistency(completionData.getCompletions());
        int daysSinceStart = (int) ChronoUnit.DAYS.between(startDate, endDate);
        double successRate = calculateSuccessRate(completionData.getCompletions());
        
        // Determine formation stage
        FormationStage stage = determineFormationStage(automaticity, consistency, daysSinceStart, successRate);
        
        // Calculate formation progress
        double progress = calculateFormationProgress(stage, automaticity, consistency, daysSinceStart);
        
        // Identify formation barriers
        List<FormationBarrier> barriers = identifyFormationBarriers(completionData.getCompletions(), stage);
        
        return new FormationAnalysisResult(habitId, stage, progress, automaticity, consistency, barriers);
    }
    
    /**
     * Calculates habit strength based on multiple behavioral factors.
     */
    public HabitStrengthAnalysis calculateHabitStrength(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Calculating habit strength for habit {}", habitId);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Calculate strength components
        double frequency = calculateFrequency(completionData.getCompletions());
        double consistency = calculateConsistency(completionData.getCompletions());
        double automaticity = calculateAutomaticity(completionData.getCompletions());
        double contextStability = calculateContextStability(completionData.getCompletions());
        
        // Calculate overall strength (weighted combination)
        double overallStrength = (frequency * 0.3) + (consistency * 0.3) + (automaticity * 0.25) + (contextStability * 0.15);
        
        // Determine strength category
        StrengthCategory category = categorizeStrength(overallStrength);
        
        return new HabitStrengthAnalysis(habitId, overallStrength, frequency, consistency, 
                                       automaticity, contextStability, category);
    }
    
    /**
     * Analyzes trigger effectiveness for habit formation.
     */
    public TriggerEffectivenessAnalysis analyzeTriggerEffectiveness(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Analyzing trigger effectiveness for habit {}", habitId);
        
        AnalyticsDataCollector.HabitCompletionData completionData = 
            analyticsDataCollector.collectHabitCompletionData(habitId, startDate, endDate);
        
        // Analyze timing patterns (proxy for trigger effectiveness)
        Map<Integer, List<DailyCompletion>> completionsByHour = completionData.getCompletions().stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .collect(Collectors.groupingBy(completion -> completion.getCompletedAt().getHour()));
        
        List<TriggerWindow> triggerWindows = new ArrayList<>();
        
        for (Map.Entry<Integer, List<DailyCompletion>> entry : completionsByHour.entrySet()) {
            int hour = entry.getKey();
            List<DailyCompletion> hourCompletions = entry.getValue();
            
            double effectiveness = calculateTriggerEffectiveness(hourCompletions);
            double consistency = calculateTimingConsistency(hourCompletions);
            
            if (hourCompletions.size() >= 3) { // Minimum threshold for analysis
                triggerWindows.add(new TriggerWindow(hour, effectiveness, consistency, hourCompletions.size()));
            }
        }
        
        // Sort by effectiveness
        triggerWindows.sort((w1, w2) -> Double.compare(w2.getEffectiveness(), w1.getEffectiveness()));
        
        // Calculate overall trigger strength
        double overallTriggerStrength = triggerWindows.stream()
            .mapToDouble(TriggerWindow::getEffectiveness)
            .average()
            .orElse(0.0);
        
        return new TriggerEffectivenessAnalysis(habitId, overallTriggerStrength, triggerWindows);
    }
    
    // Private helper methods
    
    private double calculateAutomaticity(List<DailyCompletion> completions) {
        if (completions.isEmpty()) return 0.0;
        
        // Automaticity increases with consistent timing and reduced cognitive load
        Map<Integer, Long> hourCounts = completions.stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .collect(Collectors.groupingBy(
                completion -> completion.getCompletedAt().getHour(),
                Collectors.counting()
            ));
        
        if (hourCounts.isEmpty()) return 0.0;
        
        // Calculate timing consistency (higher consistency = higher automaticity)
        double maxCount = hourCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        double totalCount = hourCounts.values().stream().mapToLong(Long::longValue).sum();
        
        return maxCount / totalCount; // Concentration in specific time windows
    }
    
    private double calculateConsistency(List<DailyCompletion> completions) {
        if (completions.size() < 7) return 0.0; // Need at least a week of data
        
        // Calculate weekly consistency
        Map<LocalDate, List<DailyCompletion>> weeklyCompletions = completions.stream()
            .collect(Collectors.groupingBy(completion -> 
                completion.getCompletionDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))));
        
        List<Double> weeklyRates = weeklyCompletions.values().stream()
            .map(this::calculateSuccessRate)
            .collect(Collectors.toList());
        
        if (weeklyRates.size() < 2) return calculateSuccessRate(completions);
        
        // Calculate variance (lower variance = higher consistency)
        double mean = weeklyRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = weeklyRates.stream()
            .mapToDouble(rate -> Math.pow(rate - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.max(0.0, 1.0 - Math.sqrt(variance));
    }
    
    private double calculateSuccessRate(List<DailyCompletion> completions) {
        if (completions.isEmpty()) return 0.0;
        
        long successfulCompletions = completions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        return (double) successfulCompletions / completions.size();
    }
    
    private FormationStage determineFormationStage(double automaticity, double consistency, int daysSinceStart, double successRate) {
        // Based on behavioral science research (Lally et al., 2010)
        
        if (daysSinceStart < 7 || successRate < 0.3) {
            return FormationStage.INITIATION;
        } else if (daysSinceStart < 21 || consistency < 0.6 || automaticity < 0.4) {
            return FormationStage.LEARNING;
        } else if (daysSinceStart < 66 || consistency < 0.8 || automaticity < 0.7) {
            return FormationStage.STABILITY;
        } else {
            return FormationStage.MASTERY;
        }
    }
    
    private double calculateFormationProgress(FormationStage stage, double automaticity, double consistency, int daysSinceStart) {
        switch (stage) {
            case INITIATION:
                return Math.min(1.0, daysSinceStart / 7.0);
            case LEARNING:
                return Math.min(1.0, (consistency + automaticity) / 2.0);
            case STABILITY:
                return Math.min(1.0, (consistency * 0.6) + (automaticity * 0.4));
            case MASTERY:
                return 1.0;
            default:
                return 0.0;
        }
    }
    
    private List<FormationBarrier> identifyFormationBarriers(List<DailyCompletion> completions, FormationStage stage) {
        List<FormationBarrier> barriers = new ArrayList<>();
        
        double successRate = calculateSuccessRate(completions);
        double consistency = calculateConsistency(completions);
        
        // Identify common barriers based on stage and metrics
        if (successRate < 0.5) {
            barriers.add(new FormationBarrier(BarrierType.LOW_SUCCESS_RATE, 
                "Success rate below 50% indicates difficulty with habit execution", 1.0 - successRate));
        }
        
        if (consistency < 0.6) {
            barriers.add(new FormationBarrier(BarrierType.INCONSISTENT_TIMING, 
                "Inconsistent execution patterns may hinder automaticity", 1.0 - consistency));
        }
        
        // Check for long gaps in completion
        List<DailyCompletion> sortedCompletions = completions.stream()
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        for (int i = 1; i < sortedCompletions.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(
                sortedCompletions.get(i-1).getCompletionDate(),
                sortedCompletions.get(i).getCompletionDate()
            );
            
            if (daysBetween > 3) {
                barriers.add(new FormationBarrier(BarrierType.EXECUTION_GAPS, 
                    String.format("Gap of %d days detected in habit execution", daysBetween), daysBetween / 7.0));
                break; // Only report the first significant gap
            }
        }
        
        return barriers;
    }
    
    private double calculateFrequency(List<DailyCompletion> completions) {
        if (completions.isEmpty()) return 0.0;
        
        // Calculate completions per week
        long totalDays = completions.stream()
            .map(DailyCompletion::getCompletionDate)
            .distinct()
            .count();
        
        long successfulDays = completions.stream()
            .filter(DailyCompletion::getCompleted)
            .map(DailyCompletion::getCompletionDate)
            .distinct()
            .count();
        
        return totalDays > 0 ? (double) successfulDays / totalDays : 0.0;
    }
    
    private double calculateContextStability(List<DailyCompletion> completions) {
        // Analyze timing stability as a proxy for context stability
        Map<Integer, Long> hourCounts = completions.stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .filter(DailyCompletion::getCompleted)
            .collect(Collectors.groupingBy(
                completion -> completion.getCompletedAt().getHour(),
                Collectors.counting()
            ));
        
        if (hourCounts.isEmpty()) return 0.0;
        
        // Calculate entropy (lower entropy = higher stability)
        double totalCount = hourCounts.values().stream().mapToLong(Long::longValue).sum();
        double entropy = hourCounts.values().stream()
            .mapToDouble(count -> {
                double probability = count / totalCount;
                return -probability * Math.log(probability);
            })
            .sum();
        
        // Normalize entropy to 0-1 scale (lower entropy = higher stability)
        double maxEntropy = Math.log(24); // Maximum possible entropy for 24 hours
        return Math.max(0.0, 1.0 - (entropy / maxEntropy));
    }
    
    private StrengthCategory categorizeStrength(double strength) {
        if (strength >= 0.8) return StrengthCategory.VERY_STRONG;
        if (strength >= 0.6) return StrengthCategory.STRONG;
        if (strength >= 0.4) return StrengthCategory.MODERATE;
        if (strength >= 0.2) return StrengthCategory.WEAK;
        return StrengthCategory.VERY_WEAK;
    }
    
    private double calculateTriggerEffectiveness(List<DailyCompletion> completions) {
        if (completions.isEmpty()) return 0.0;
        
        // Effectiveness based on success rate at this trigger time
        long successful = completions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        return (double) successful / completions.size();
    }
    
    private double calculateTimingConsistency(List<DailyCompletion> completions) {
        if (completions.size() < 2) return 1.0;
        
        // Calculate variance in completion times within the hour
        List<Integer> minutes = completions.stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .map(completion -> completion.getCompletedAt().getMinute())
            .collect(Collectors.toList());
        
        if (minutes.size() < 2) return 1.0;
        
        double mean = minutes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = minutes.stream()
            .mapToDouble(minute -> Math.pow(minute - mean, 2))
            .average()
            .orElse(0.0);
        
        // Convert variance to consistency score (lower variance = higher consistency)
        return Math.max(0.0, 1.0 - (Math.sqrt(variance) / 30.0)); // Normalize by 30 minutes
    }    
    
// Data Transfer Objects
    
    public static class FormationAnalysisResult {
        private final UUID habitId;
        private final FormationStage stage;
        private final double progress;
        private final double automaticity;
        private final double consistency;
        private final List<FormationBarrier> barriers;
        
        public FormationAnalysisResult(UUID habitId, FormationStage stage, double progress, 
                                     double automaticity, double consistency, List<FormationBarrier> barriers) {
            this.habitId = habitId;
            this.stage = stage;
            this.progress = progress;
            this.automaticity = automaticity;
            this.consistency = consistency;
            this.barriers = barriers;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public FormationStage getStage() { return stage; }
        public double getProgress() { return progress; }
        public double getAutomaticity() { return automaticity; }
        public double getConsistency() { return consistency; }
        public List<FormationBarrier> getBarriers() { return barriers; }
    }
    
    public static class HabitStrengthAnalysis {
        private final UUID habitId;
        private final double overallStrength;
        private final double frequency;
        private final double consistency;
        private final double automaticity;
        private final double contextStability;
        private final StrengthCategory category;
        
        public HabitStrengthAnalysis(UUID habitId, double overallStrength, double frequency, 
                                   double consistency, double automaticity, double contextStability, 
                                   StrengthCategory category) {
            this.habitId = habitId;
            this.overallStrength = overallStrength;
            this.frequency = frequency;
            this.consistency = consistency;
            this.automaticity = automaticity;
            this.contextStability = contextStability;
            this.category = category;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public double getOverallStrength() { return overallStrength; }
        public double getFrequency() { return frequency; }
        public double getConsistency() { return consistency; }
        public double getAutomaticity() { return automaticity; }
        public double getContextStability() { return contextStability; }
        public StrengthCategory getCategory() { return category; }
    }
    
    public static class TriggerEffectivenessAnalysis {
        private final UUID habitId;
        private final double overallTriggerStrength;
        private final List<TriggerWindow> triggerWindows;
        
        public TriggerEffectivenessAnalysis(UUID habitId, double overallTriggerStrength, List<TriggerWindow> triggerWindows) {
            this.habitId = habitId;
            this.overallTriggerStrength = overallTriggerStrength;
            this.triggerWindows = triggerWindows;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public double getOverallTriggerStrength() { return overallTriggerStrength; }
        public List<TriggerWindow> getTriggerWindows() { return triggerWindows; }
    }
    
    public static class FormationBarrier {
        private final BarrierType type;
        private final String description;
        private final double severity;
        
        public FormationBarrier(BarrierType type, String description, double severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        
        // Getters
        public BarrierType getType() { return type; }
        public String getDescription() { return description; }
        public double getSeverity() { return severity; }
    }
    
    public static class TriggerWindow {
        private final int hour;
        private final double effectiveness;
        private final double consistency;
        private final int sampleSize;
        
        public TriggerWindow(int hour, double effectiveness, double consistency, int sampleSize) {
            this.hour = hour;
            this.effectiveness = effectiveness;
            this.consistency = consistency;
            this.sampleSize = sampleSize;
        }
        
        // Getters
        public int getHour() { return hour; }
        public double getEffectiveness() { return effectiveness; }
        public double getConsistency() { return consistency; }
        public int getSampleSize() { return sampleSize; }
    }
    
    // Enums
    
    public enum FormationStage {
        INITIATION,
        LEARNING,
        STABILITY,
        MASTERY
    }
    
    public enum StrengthCategory {
        VERY_WEAK,
        WEAK,
        MODERATE,
        STRONG,
        VERY_STRONG
    }
    
    public enum BarrierType {
        LOW_SUCCESS_RATE,
        INCONSISTENT_TIMING,
        EXECUTION_GAPS,
        CONTEXT_INSTABILITY,
        MOTIVATION_DECLINE
    }
}