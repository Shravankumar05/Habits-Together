package com.example.backend.service.analytics;

import com.example.backend.entity.HabitAnalytics;
import com.example.backend.repository.HabitAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for recommending evidence-based reinforcement strategies for habit formation.
 * Provides personalized interventions based on formation stage and user patterns.
 */
@Service
public class ReinforcementStrategyService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReinforcementStrategyService.class);
    
    @Autowired
    private HabitFormationAnalyzer habitFormationAnalyzer;
    
    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;
    
    @Autowired
    private OptimalTimingAnalyzer optimalTimingAnalyzer;
    
    /**
     * Recommends reinforcement strategies based on habit formation analysis.
     */
    public StrategyRecommendationResult recommendStrategies(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Recommending reinforcement strategies for habit {}", habitId);
        
        // Analyze current formation state
        HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis = 
            habitFormationAnalyzer.analyzeFormationStage(userId, habitId, startDate, endDate);
        
        // Analyze habit strength
        HabitFormationAnalyzer.HabitStrengthAnalysis strengthAnalysis = 
            habitFormationAnalyzer.calculateHabitStrength(userId, habitId, startDate, endDate);
        
        // Get optimal timing
        OptimalTimingAnalyzer.OptimalTimingResult timingAnalysis = 
            optimalTimingAnalyzer.analyzeOptimalTiming(userId, habitId, startDate, endDate);
        
        // Generate personalized strategies
        List<ReinforcementStrategy> strategies = generateStrategies(formationAnalysis, strengthAnalysis, timingAnalysis);
        
        // Prioritize strategies
        strategies = prioritizeStrategies(strategies, formationAnalysis);
        
        return new StrategyRecommendationResult(habitId, formationAnalysis.getStage(), strategies);
    }
    
    /**
     * Tracks formation progress and suggests milestone celebrations.
     */
    public FormationProgressResult trackFormationProgress(UUID userId, UUID habitId) {
        logger.debug("Tracking formation progress for habit {}", habitId);
        
        Optional<HabitAnalytics> analyticsOpt = habitAnalyticsRepository.findByUserIdAndHabitId(userId, habitId);
        if (analyticsOpt.isEmpty()) {
            return new FormationProgressResult(habitId, HabitFormationAnalyzer.FormationStage.INITIATION, 
                                             0.0, Collections.emptyList());
        }
        
        HabitAnalytics analytics = analyticsOpt.get();
        HabitFormationAnalyzer.FormationStage currentStage = mapFormationStage(analytics.getFormationStage());
        
        // Calculate progress within current stage
        double stageProgress = calculateStageProgress(analytics);
        
        // Identify milestones
        List<FormationMilestone> milestones = identifyMilestones(analytics, currentStage);
        
        return new FormationProgressResult(habitId, currentStage, stageProgress, milestones);
    }
    
    /**
     * Provides personalized strategy recommendations based on user patterns.
     */
    public PersonalizedStrategyResult getPersonalizedStrategies(UUID userId, UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Getting personalized strategies for habit {}", habitId);
        
        // Analyze formation barriers
        HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis = 
            habitFormationAnalyzer.analyzeFormationStage(userId, habitId, startDate, endDate);
        
        // Generate targeted interventions for each barrier
        List<TargetedIntervention> interventions = new ArrayList<>();
        
        for (HabitFormationAnalyzer.FormationBarrier barrier : formationAnalysis.getBarriers()) {
            interventions.addAll(generateInterventionsForBarrier(barrier, formationAnalysis.getStage()));
        }
        
        // Add general stage-appropriate strategies
        interventions.addAll(generateStageSpecificInterventions(formationAnalysis.getStage()));
        
        // Sort by effectiveness
        interventions.sort((i1, i2) -> Double.compare(i2.getEffectiveness(), i1.getEffectiveness()));
        
        return new PersonalizedStrategyResult(habitId, interventions);
    }
    
    // Private helper methods
    
    private List<ReinforcementStrategy> generateStrategies(HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis,
                                                          HabitFormationAnalyzer.HabitStrengthAnalysis strengthAnalysis,
                                                          OptimalTimingAnalyzer.OptimalTimingResult timingAnalysis) {
        List<ReinforcementStrategy> strategies = new ArrayList<>();
        
        // Stage-specific strategies
        switch (formationAnalysis.getStage()) {
            case INITIATION:
                strategies.addAll(getInitiationStrategies());
                break;
            case LEARNING:
                strategies.addAll(getLearningStrategies());
                break;
            case STABILITY:
                strategies.addAll(getStabilityStrategies());
                break;
            case MASTERY:
                strategies.addAll(getMasteryStrategies());
                break;
        }
        
        // Strength-based strategies
        if (strengthAnalysis.getOverallStrength() < 0.5) {
            strategies.addAll(getStrengthBuildingStrategies());
        }
        
        // Timing-based strategies
        if (timingAnalysis.getOptimalStartTime() != null) {
            strategies.add(new ReinforcementStrategy(
                StrategyType.TIMING_OPTIMIZATION,
                "Optimal Timing",
                String.format("Complete your habit between %s and %s for best results", 
                            timingAnalysis.getOptimalStartTime(), timingAnalysis.getOptimalEndTime()),
                StrategyCategory.ENVIRONMENTAL,
                0.8,
                EvidenceLevel.HIGH
            ));
        }
        
        return strategies;
    }
    
    private List<ReinforcementStrategy> getInitiationStrategies() {
        return Arrays.asList(
            new ReinforcementStrategy(
                StrategyType.HABIT_STACKING,
                "Habit Stacking",
                "Link your new habit to an existing routine. After I [existing habit], I will [new habit].",
                StrategyCategory.BEHAVIORAL,
                0.85,
                EvidenceLevel.HIGH
            ),
            new ReinforcementStrategy(
                StrategyType.IMPLEMENTATION_INTENTION,
                "Implementation Intention",
                "Create specific if-then plans: 'If it's 8 AM, then I will do my habit.'",
                StrategyCategory.COGNITIVE,
                0.80,
                EvidenceLevel.HIGH
            ),
            new ReinforcementStrategy(
                StrategyType.START_SMALL,
                "Start Small",
                "Begin with the smallest possible version of your habit to build momentum.",
                StrategyCategory.BEHAVIORAL,
                0.90,
                EvidenceLevel.HIGH
            )
        );
    }
    
    private List<ReinforcementStrategy> getLearningStrategies() {
        return Arrays.asList(
            new ReinforcementStrategy(
                StrategyType.CONSISTENCY_FOCUS,
                "Consistency Over Intensity",
                "Focus on doing the habit every day rather than doing it perfectly.",
                StrategyCategory.BEHAVIORAL,
                0.85,
                EvidenceLevel.HIGH
            ),
            new ReinforcementStrategy(
                StrategyType.ENVIRONMENTAL_DESIGN,
                "Environmental Design",
                "Modify your environment to make the habit easier and more obvious.",
                StrategyCategory.ENVIRONMENTAL,
                0.80,
                EvidenceLevel.HIGH
            ),
            new ReinforcementStrategy(
                StrategyType.TRACKING_REINFORCEMENT,
                "Visual Progress Tracking",
                "Use a habit tracker or calendar to visualize your progress and maintain motivation.",
                StrategyCategory.MOTIVATIONAL,
                0.75,
                EvidenceLevel.MEDIUM
            )
        );
    }
    
    private List<ReinforcementStrategy> getStabilityStrategies() {
        return Arrays.asList(
            new ReinforcementStrategy(
                StrategyType.CONTEXT_CONSISTENCY,
                "Context Consistency",
                "Perform your habit in the same location and time to strengthen automatic responses.",
                StrategyCategory.ENVIRONMENTAL,
                0.85,
                EvidenceLevel.HIGH
            ),
            new ReinforcementStrategy(
                StrategyType.IDENTITY_REINFORCEMENT,
                "Identity Reinforcement",
                "Focus on becoming the type of person who does this habit naturally.",
                StrategyCategory.COGNITIVE,
                0.80,
                EvidenceLevel.MEDIUM
            ),
            new ReinforcementStrategy(
                StrategyType.TEMPTATION_BUNDLING,
                "Temptation Bundling",
                "Pair your habit with something you enjoy to increase motivation.",
                StrategyCategory.MOTIVATIONAL,
                0.70,
                EvidenceLevel.MEDIUM
            )
        );
    }
    
    private List<ReinforcementStrategy> getMasteryStrategies() {
        return Arrays.asList(
            new ReinforcementStrategy(
                StrategyType.HABIT_EVOLUTION,
                "Habit Evolution",
                "Gradually increase the complexity or intensity of your well-established habit.",
                StrategyCategory.BEHAVIORAL,
                0.75,
                EvidenceLevel.MEDIUM
            ),
            new ReinforcementStrategy(
                StrategyType.MAINTENANCE_FOCUS,
                "Maintenance Focus",
                "Focus on maintaining consistency and preventing habit decay during disruptions.",
                StrategyCategory.BEHAVIORAL,
                0.80,
                EvidenceLevel.HIGH
            )
        );
    }
    
    private List<ReinforcementStrategy> getStrengthBuildingStrategies() {
        return Arrays.asList(
            new ReinforcementStrategy(
                StrategyType.FREQUENCY_INCREASE,
                "Increase Frequency",
                "Gradually increase how often you perform the habit to build strength.",
                StrategyCategory.BEHAVIORAL,
                0.70,
                EvidenceLevel.MEDIUM
            ),
            new ReinforcementStrategy(
                StrategyType.REWARD_SYSTEM,
                "Immediate Rewards",
                "Give yourself small, immediate rewards after completing the habit.",
                StrategyCategory.MOTIVATIONAL,
                0.65,
                EvidenceLevel.MEDIUM
            )
        );
    }
    
    private List<ReinforcementStrategy> prioritizeStrategies(List<ReinforcementStrategy> strategies, 
                                                           HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis) {
        // Sort by effectiveness, evidence level, and stage appropriateness
        return strategies.stream()
            .sorted((s1, s2) -> {
                // Primary sort: effectiveness
                int effectivenessCompare = Double.compare(s2.getEffectiveness(), s1.getEffectiveness());
                if (effectivenessCompare != 0) return effectivenessCompare;
                
                // Secondary sort: evidence level
                return s2.getEvidenceLevel().compareTo(s1.getEvidenceLevel());
            })
            .limit(5) // Return top 5 strategies
            .collect(Collectors.toList());
    }    

    private HabitFormationAnalyzer.FormationStage mapFormationStage(HabitAnalytics.FormationStage stage) {
        if (stage == null) return HabitFormationAnalyzer.FormationStage.INITIATION;
        
        switch (stage) {
            case INITIATION: return HabitFormationAnalyzer.FormationStage.INITIATION;
            case LEARNING: return HabitFormationAnalyzer.FormationStage.LEARNING;
            case STABILITY: return HabitFormationAnalyzer.FormationStage.STABILITY;
            case MASTERY: return HabitFormationAnalyzer.FormationStage.MASTERY;
            default: return HabitFormationAnalyzer.FormationStage.INITIATION;
        }
    }
    
    private double calculateStageProgress(HabitAnalytics analytics) {
        double successRate = analytics.getSuccessRate() != null ? analytics.getSuccessRate() : 0.0;
        double consistencyScore = analytics.getConsistencyScore() != null ? analytics.getConsistencyScore() : 0.0;
        double habitStrength = analytics.getHabitStrength() != null ? analytics.getHabitStrength() : 0.0;
        
        // Weighted combination based on formation stage
        if (analytics.getFormationStage() == null) return 0.0;
        
        switch (analytics.getFormationStage()) {
            case INITIATION:
                return successRate; // Focus on basic completion
            case LEARNING:
                return (successRate * 0.6) + (consistencyScore * 0.4); // Add consistency
            case STABILITY:
                return (successRate * 0.4) + (consistencyScore * 0.4) + (habitStrength * 0.2); // All factors
            case MASTERY:
                return habitStrength; // Focus on strength maintenance
            default:
                return 0.0;
        }
    }
    
    private List<FormationMilestone> identifyMilestones(HabitAnalytics analytics, HabitFormationAnalyzer.FormationStage currentStage) {
        List<FormationMilestone> milestones = new ArrayList<>();
        
        double successRate = analytics.getSuccessRate() != null ? analytics.getSuccessRate() : 0.0;
        double consistencyScore = analytics.getConsistencyScore() != null ? analytics.getConsistencyScore() : 0.0;
        
        // Stage-specific milestones
        switch (currentStage) {
            case INITIATION:
                if (successRate >= 0.5) {
                    milestones.add(new FormationMilestone(MilestoneType.FIRST_WEEK, 
                        "First Week Complete", "You've successfully completed your first week!", true));
                }
                milestones.add(new FormationMilestone(MilestoneType.CONSISTENCY_STREAK, 
                    "3-Day Streak", "Complete 3 days in a row", successRate >= 0.6));
                break;
                
            case LEARNING:
                milestones.add(new FormationMilestone(MilestoneType.CONSISTENCY_MILESTONE, 
                    "Consistency Building", "Achieve 70% consistency", consistencyScore >= 0.7));
                milestones.add(new FormationMilestone(MilestoneType.HABIT_STRENGTH, 
                    "Habit Strength", "Build habit strength to 60%", 
                    analytics.getHabitStrength() != null && analytics.getHabitStrength() >= 0.6));
                break;
                
            case STABILITY:
                milestones.add(new FormationMilestone(MilestoneType.AUTOMATICITY, 
                    "Automatic Response", "Habit becomes more automatic", consistencyScore >= 0.8));
                break;
                
            case MASTERY:
                milestones.add(new FormationMilestone(MilestoneType.MASTERY_ACHIEVED, 
                    "Habit Mastery", "Congratulations! You've mastered this habit.", true));
                break;
        }
        
        return milestones;
    }
    
    private List<TargetedIntervention> generateInterventionsForBarrier(HabitFormationAnalyzer.FormationBarrier barrier, 
                                                                      HabitFormationAnalyzer.FormationStage stage) {
        List<TargetedIntervention> interventions = new ArrayList<>();
        
        switch (barrier.getType()) {
            case LOW_SUCCESS_RATE:
                interventions.add(new TargetedIntervention(
                    InterventionType.DIFFICULTY_REDUCTION,
                    "Reduce Habit Difficulty",
                    "Make your habit easier by reducing the required effort or time commitment.",
                    0.85,
                    "Start with just 2 minutes per day or the smallest possible version of your habit."
                ));
                break;
                
            case INCONSISTENT_TIMING:
                interventions.add(new TargetedIntervention(
                    InterventionType.TIMING_CONSISTENCY,
                    "Establish Consistent Timing",
                    "Choose a specific time each day and stick to it to build automaticity.",
                    0.80,
                    "Set a daily alarm or link your habit to an existing routine."
                ));
                break;
                
            case EXECUTION_GAPS:
                interventions.add(new TargetedIntervention(
                    InterventionType.GAP_PREVENTION,
                    "Prevent Execution Gaps",
                    "Create backup plans for maintaining your habit during disruptions.",
                    0.75,
                    "Identify potential obstacles and create 'if-then' plans for each scenario."
                ));
                break;
                
            case CONTEXT_INSTABILITY:
                interventions.add(new TargetedIntervention(
                    InterventionType.CONTEXT_STABILIZATION,
                    "Stabilize Context",
                    "Perform your habit in the same location and circumstances each time.",
                    0.70,
                    "Choose a dedicated space and time for your habit practice."
                ));
                break;
        }
        
        return interventions;
    }
    
    private List<TargetedIntervention> generateStageSpecificInterventions(HabitFormationAnalyzer.FormationStage stage) {
        List<TargetedIntervention> interventions = new ArrayList<>();
        
        switch (stage) {
            case INITIATION:
                interventions.add(new TargetedIntervention(
                    InterventionType.MOTIVATION_BOOST,
                    "Motivation Boost",
                    "Focus on your 'why' and the benefits you'll gain from this habit.",
                    0.70,
                    "Write down 3 specific benefits you'll experience from maintaining this habit."
                ));
                break;
                
            case LEARNING:
                interventions.add(new TargetedIntervention(
                    InterventionType.PROGRESS_TRACKING,
                    "Enhanced Progress Tracking",
                    "Use detailed tracking to identify patterns and maintain motivation.",
                    0.65,
                    "Track not just completion but also how you feel and what helps you succeed."
                ));
                break;
                
            case STABILITY:
                interventions.add(new TargetedIntervention(
                    InterventionType.AUTOMATION_FOCUS,
                    "Automation Focus",
                    "Work on making the habit more automatic and less effortful.",
                    0.75,
                    "Focus on consistent context and timing to build automatic responses."
                ));
                break;
                
            case MASTERY:
                interventions.add(new TargetedIntervention(
                    InterventionType.MAINTENANCE_STRATEGY,
                    "Maintenance Strategy",
                    "Develop strategies to maintain your habit during life changes.",
                    0.80,
                    "Create contingency plans for travel, illness, or schedule changes."
                ));
                break;
        }
        
        return interventions;
    }
    
    // Data Transfer Objects
    
    public static class StrategyRecommendationResult {
        private final UUID habitId;
        private final HabitFormationAnalyzer.FormationStage currentStage;
        private final List<ReinforcementStrategy> strategies;
        
        public StrategyRecommendationResult(UUID habitId, HabitFormationAnalyzer.FormationStage currentStage, 
                                          List<ReinforcementStrategy> strategies) {
            this.habitId = habitId;
            this.currentStage = currentStage;
            this.strategies = strategies;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public HabitFormationAnalyzer.FormationStage getCurrentStage() { return currentStage; }
        public List<ReinforcementStrategy> getStrategies() { return strategies; }
    }
    
    public static class ReinforcementStrategy {
        private final StrategyType type;
        private final String name;
        private final String description;
        private final StrategyCategory category;
        private final double effectiveness;
        private final EvidenceLevel evidenceLevel;
        
        public ReinforcementStrategy(StrategyType type, String name, String description, 
                                   StrategyCategory category, double effectiveness, EvidenceLevel evidenceLevel) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.category = category;
            this.effectiveness = effectiveness;
            this.evidenceLevel = evidenceLevel;
        }
        
        // Getters
        public StrategyType getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public StrategyCategory getCategory() { return category; }
        public double getEffectiveness() { return effectiveness; }
        public EvidenceLevel getEvidenceLevel() { return evidenceLevel; }
    }
    
    public static class FormationProgressResult {
        private final UUID habitId;
        private final HabitFormationAnalyzer.FormationStage currentStage;
        private final double stageProgress;
        private final List<FormationMilestone> milestones;
        
        public FormationProgressResult(UUID habitId, HabitFormationAnalyzer.FormationStage currentStage, 
                                     double stageProgress, List<FormationMilestone> milestones) {
            this.habitId = habitId;
            this.currentStage = currentStage;
            this.stageProgress = stageProgress;
            this.milestones = milestones;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public HabitFormationAnalyzer.FormationStage getCurrentStage() { return currentStage; }
        public double getStageProgress() { return stageProgress; }
        public List<FormationMilestone> getMilestones() { return milestones; }
    }
    
    public static class FormationMilestone {
        private final MilestoneType type;
        private final String name;
        private final String description;
        private final boolean achieved;
        
        public FormationMilestone(MilestoneType type, String name, String description, boolean achieved) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.achieved = achieved;
        }
        
        // Getters
        public MilestoneType getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isAchieved() { return achieved; }
    }
    
    public static class PersonalizedStrategyResult {
        private final UUID habitId;
        private final List<TargetedIntervention> interventions;
        
        public PersonalizedStrategyResult(UUID habitId, List<TargetedIntervention> interventions) {
            this.habitId = habitId;
            this.interventions = interventions;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public List<TargetedIntervention> getInterventions() { return interventions; }
    }
    
    public static class TargetedIntervention {
        private final InterventionType type;
        private final String name;
        private final String description;
        private final double effectiveness;
        private final String actionStep;
        
        public TargetedIntervention(InterventionType type, String name, String description, 
                                  double effectiveness, String actionStep) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.effectiveness = effectiveness;
            this.actionStep = actionStep;
        }
        
        // Getters
        public InterventionType getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getEffectiveness() { return effectiveness; }
        public String getActionStep() { return actionStep; }
    }
    
    // Enums
    
    public enum StrategyType {
        HABIT_STACKING,
        IMPLEMENTATION_INTENTION,
        START_SMALL,
        CONSISTENCY_FOCUS,
        ENVIRONMENTAL_DESIGN,
        TRACKING_REINFORCEMENT,
        CONTEXT_CONSISTENCY,
        IDENTITY_REINFORCEMENT,
        TEMPTATION_BUNDLING,
        HABIT_EVOLUTION,
        MAINTENANCE_FOCUS,
        FREQUENCY_INCREASE,
        REWARD_SYSTEM,
        TIMING_OPTIMIZATION
    }
    
    public enum StrategyCategory {
        BEHAVIORAL,
        COGNITIVE,
        ENVIRONMENTAL,
        MOTIVATIONAL,
        SOCIAL
    }
    
    public enum EvidenceLevel {
        LOW,
        MEDIUM,
        HIGH
    }
    
    public enum MilestoneType {
        FIRST_WEEK,
        CONSISTENCY_STREAK,
        CONSISTENCY_MILESTONE,
        HABIT_STRENGTH,
        AUTOMATICITY,
        MASTERY_ACHIEVED
    }
    
    public enum InterventionType {
        DIFFICULTY_REDUCTION,
        TIMING_CONSISTENCY,
        GAP_PREVENTION,
        CONTEXT_STABILIZATION,
        MOTIVATION_BOOST,
        PROGRESS_TRACKING,
        AUTOMATION_FOCUS,
        MAINTENANCE_STRATEGY
    }
}