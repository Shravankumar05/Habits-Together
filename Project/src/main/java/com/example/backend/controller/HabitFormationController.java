package com.example.backend.controller;

import com.example.backend.service.analytics.HabitFormationAnalyzer;
import com.example.backend.service.analytics.ReinforcementStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for habit formation analysis endpoints.
 * Provides formation analysis, reinforcement strategies, and progress tracking.
 */
@RestController
@RequestMapping("/api/habit-formation")
@CrossOrigin(origins = "*")
public class HabitFormationController {
    
    private static final Logger logger = LoggerFactory.getLogger(HabitFormationController.class);
    
    @Autowired
    private HabitFormationAnalyzer habitFormationAnalyzer;
    
    @Autowired
    private ReinforcementStrategyService reinforcementStrategyService;
    
    /**
     * Get formation stage analysis for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/formation-analysis")
    public ResponseEntity<HabitFormationAnalyzer.FormationAnalysisResult> getFormationAnalysis(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting formation analysis for habit {} of user {}", habitId, userId);
        
        try {
            // Default to last 90 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            HabitFormationAnalyzer.FormationAnalysisResult analysis = 
                habitFormationAnalyzer.analyzeFormationStage(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error analyzing formation for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get habit strength analysis.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/strength-analysis")
    public ResponseEntity<HabitFormationAnalyzer.HabitStrengthAnalysis> getHabitStrengthAnalysis(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting habit strength analysis for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            HabitFormationAnalyzer.HabitStrengthAnalysis analysis = 
                habitFormationAnalyzer.calculateHabitStrength(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error analyzing habit strength for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get trigger effectiveness analysis.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/trigger-analysis")
    public ResponseEntity<HabitFormationAnalyzer.TriggerEffectivenessAnalysis> getTriggerEffectivenessAnalysis(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting trigger effectiveness analysis for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            HabitFormationAnalyzer.TriggerEffectivenessAnalysis analysis = 
                habitFormationAnalyzer.analyzeTriggerEffectiveness(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error analyzing trigger effectiveness for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get reinforcement strategy recommendations.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/reinforcement-strategies")
    public ResponseEntity<ReinforcementStrategyService.StrategyRecommendationResult> getReinforcementStrategies(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting reinforcement strategies for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            ReinforcementStrategyService.StrategyRecommendationResult strategies = 
                reinforcementStrategyService.recommendStrategies(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(strategies);
        } catch (Exception e) {
            logger.error("Error getting reinforcement strategies for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get formation progress tracking.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/formation-progress")
    public ResponseEntity<ReinforcementStrategyService.FormationProgressResult> getFormationProgress(
            @PathVariable UUID userId,
            @PathVariable UUID habitId) {
        
        logger.debug("Getting formation progress for habit {} of user {}", habitId, userId);
        
        try {
            ReinforcementStrategyService.FormationProgressResult progress = 
                reinforcementStrategyService.trackFormationProgress(userId, habitId);
            
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            logger.error("Error tracking formation progress for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get personalized strategy recommendations.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/personalized-strategies")
    public ResponseEntity<ReinforcementStrategyService.PersonalizedStrategyResult> getPersonalizedStrategies(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting personalized strategies for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            ReinforcementStrategyService.PersonalizedStrategyResult strategies = 
                reinforcementStrategyService.getPersonalizedStrategies(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(strategies);
        } catch (Exception e) {
            logger.error("Error getting personalized strategies for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get comprehensive habit formation report.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/formation-report")
    public ResponseEntity<HabitFormationReport> getFormationReport(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting comprehensive formation report for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            // Gather all formation-related data
            HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis = 
                habitFormationAnalyzer.analyzeFormationStage(userId, habitId, startDate, endDate);
            
            HabitFormationAnalyzer.HabitStrengthAnalysis strengthAnalysis = 
                habitFormationAnalyzer.calculateHabitStrength(userId, habitId, startDate, endDate);
            
            HabitFormationAnalyzer.TriggerEffectivenessAnalysis triggerAnalysis = 
                habitFormationAnalyzer.analyzeTriggerEffectiveness(userId, habitId, startDate, endDate);
            
            ReinforcementStrategyService.StrategyRecommendationResult strategies = 
                reinforcementStrategyService.recommendStrategies(userId, habitId, startDate, endDate);
            
            ReinforcementStrategyService.FormationProgressResult progress = 
                reinforcementStrategyService.trackFormationProgress(userId, habitId);
            
            HabitFormationReport report = new HabitFormationReport(
                habitId, formationAnalysis, strengthAnalysis, triggerAnalysis, strategies, progress);
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error generating formation report for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Comprehensive report DTO
    public static class HabitFormationReport {
        private final UUID habitId;
        private final HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis;
        private final HabitFormationAnalyzer.HabitStrengthAnalysis strengthAnalysis;
        private final HabitFormationAnalyzer.TriggerEffectivenessAnalysis triggerAnalysis;
        private final ReinforcementStrategyService.StrategyRecommendationResult strategies;
        private final ReinforcementStrategyService.FormationProgressResult progress;
        
        public HabitFormationReport(UUID habitId,
                                  HabitFormationAnalyzer.FormationAnalysisResult formationAnalysis,
                                  HabitFormationAnalyzer.HabitStrengthAnalysis strengthAnalysis,
                                  HabitFormationAnalyzer.TriggerEffectivenessAnalysis triggerAnalysis,
                                  ReinforcementStrategyService.StrategyRecommendationResult strategies,
                                  ReinforcementStrategyService.FormationProgressResult progress) {
            this.habitId = habitId;
            this.formationAnalysis = formationAnalysis;
            this.strengthAnalysis = strengthAnalysis;
            this.triggerAnalysis = triggerAnalysis;
            this.strategies = strategies;
            this.progress = progress;
        }
        
        // Getters
        public UUID getHabitId() { return habitId; }
        public HabitFormationAnalyzer.FormationAnalysisResult getFormationAnalysis() { return formationAnalysis; }
        public HabitFormationAnalyzer.HabitStrengthAnalysis getStrengthAnalysis() { return strengthAnalysis; }
        public HabitFormationAnalyzer.TriggerEffectivenessAnalysis getTriggerAnalysis() { return triggerAnalysis; }
        public ReinforcementStrategyService.StrategyRecommendationResult getStrategies() { return strategies; }
        public ReinforcementStrategyService.FormationProgressResult getProgress() { return progress; }
    }
}