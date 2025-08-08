package com.example.backend.controller;

import com.example.backend.service.analytics.VisualizationEngine;
import com.example.backend.service.analytics.PredictiveAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for data visualization endpoints.
 * Provides heatmaps, trends, correlations, and predictive insights.
 */
@RestController
@RequestMapping("/api/visualization")
@CrossOrigin(origins = "*")
public class VisualizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualizationController.class);
    
    @Autowired
    private VisualizationEngine visualizationEngine;
    
    @Autowired
    private PredictiveAnalyticsService predictiveAnalyticsService;
    
    /**
     * Get completion heatmap for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/heatmap")
    public ResponseEntity<VisualizationEngine.HeatmapData> getHabitHeatmap(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting heatmap for habit {} of user {}", habitId, userId);
        
        try {
            // Default to last 90 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.HeatmapData heatmap = 
                visualizationEngine.generateHabitHeatmap(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(heatmap);
        } catch (Exception e) {
            logger.error("Error generating heatmap for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get weekly heatmap showing day-of-week patterns.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/weekly-heatmap")
    public ResponseEntity<VisualizationEngine.WeeklyHeatmapData> getWeeklyHeatmap(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting weekly heatmap for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.WeeklyHeatmapData weeklyHeatmap = 
                visualizationEngine.generateWeeklyHeatmap(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(weeklyHeatmap);
        } catch (Exception e) {
            logger.error("Error generating weekly heatmap for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get trend analysis for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/trends")
    public ResponseEntity<VisualizationEngine.TrendAnalysisData> getTrendAnalysis(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting trend analysis for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.TrendAnalysisData trends = 
                visualizationEngine.generateTrendAnalysis(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            logger.error("Error generating trend analysis for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get correlation matrix for multiple habits.
     */
    @GetMapping("/users/{userId}/correlation-matrix")
    public ResponseEntity<VisualizationEngine.CorrelationMatrixData> getCorrelationMatrix(
            @PathVariable UUID userId,
            @RequestParam List<UUID> habitIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting correlation matrix for {} habits of user {}", habitIds.size(), userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.CorrelationMatrixData matrix = 
                visualizationEngine.generateCorrelationMatrix(userId, habitIds, startDate, endDate);
            
            return ResponseEntity.ok(matrix);
        } catch (Exception e) {
            logger.error("Error generating correlation matrix for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get multi-habit comparison data.
     */
    @GetMapping("/users/{userId}/habit-comparison")
    public ResponseEntity<VisualizationEngine.MultiHabitComparisonData> getHabitComparison(
            @PathVariable UUID userId,
            @RequestParam List<UUID> habitIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting habit comparison for {} habits of user {}", habitIds.size(), userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.MultiHabitComparisonData comparison = 
                visualizationEngine.generateMultiHabitComparison(userId, habitIds, startDate, endDate);
            
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            logger.error("Error generating habit comparison for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get activity pattern (hourly) for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/activity-pattern")
    public ResponseEntity<VisualizationEngine.ActivityPatternData> getActivityPattern(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting activity pattern for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            VisualizationEngine.ActivityPatternData pattern = 
                visualizationEngine.generateActivityPattern(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(pattern);
        } catch (Exception e) {
            logger.error("Error generating activity pattern for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get habit success forecast.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/forecast")
    public ResponseEntity<PredictiveAnalyticsService.HabitForecast> getHabitForecast(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(defaultValue = "30") int forecastDays,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting forecast for habit {} of user {} for {} days", habitId, userId, forecastDays);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            PredictiveAnalyticsService.HabitForecast forecast = 
                predictiveAnalyticsService.predictHabitSuccess(userId, habitId, startDate, endDate, forecastDays);
            
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            logger.error("Error generating forecast for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get anomaly detection results.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/anomalies")
    public ResponseEntity<PredictiveAnalyticsService.AnomalyDetectionResult> getAnomalies(
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Getting anomalies for habit {} of user {}", habitId, userId);
        
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(90);
            if (endDate == null) endDate = LocalDate.now();
            
            PredictiveAnalyticsService.AnomalyDetectionResult anomalies = 
                predictiveAnalyticsService.detectAnomalies(userId, habitId, startDate, endDate);
            
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            logger.error("Error detecting anomalies for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get formation prediction for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/formation-prediction")
    public ResponseEntity<PredictiveAnalyticsService.FormationPrediction> getFormationPrediction(
            @PathVariable UUID userId,
            @PathVariable UUID habitId) {
        
        logger.debug("Getting formation prediction for habit {} of user {}", habitId, userId);
        
        try {
            PredictiveAnalyticsService.FormationPrediction prediction = 
                predictiveAnalyticsService.predictFormationTimeline(userId, habitId);
            
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            logger.error("Error predicting formation for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}