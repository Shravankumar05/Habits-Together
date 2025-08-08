package com.example.backend.controller;

import com.example.backend.dto.analytics.HabitAnalyticsDTO;
import com.example.backend.dto.analytics.HabitCorrelationDTO;
import com.example.backend.dto.analytics.OptimalTimingDTO;
import com.example.backend.entity.HabitAnalytics;
import com.example.backend.entity.HabitCorrelation;
import com.example.backend.repository.HabitAnalyticsRepository;
import com.example.backend.repository.HabitCorrelationRepository;
import com.example.backend.service.analytics.HabitCorrelationService;
import com.example.backend.service.analytics.OptimalTimingAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for habit analytics endpoints.
 * Provides access to habit analytics, correlations, and optimal timing data.
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    
    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;
    
    @Autowired
    private HabitCorrelationRepository habitCorrelationRepository;
    
    @Autowired
    private HabitCorrelationService habitCorrelationService;
    
    @Autowired
    private OptimalTimingAnalyzer optimalTimingAnalyzer;
    
    /**
     * Get analytics for all habits of a user.
     */
    @GetMapping("/users/{userId}/habits")
    public ResponseEntity<List<HabitAnalyticsDTO>> getUserHabitAnalytics(@PathVariable UUID userId) {
        logger.debug("Getting habit analytics for user {}", userId);
        
        try {
            List<HabitAnalytics> analytics = habitAnalyticsRepository.findByUserId(userId);
            List<HabitAnalyticsDTO> analyticsDTO = analytics.stream()
                .map(HabitAnalyticsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(analyticsDTO);
        } catch (Exception e) {
            logger.error("Error getting habit analytics for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get analytics for a specific habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}")
    public ResponseEntity<HabitAnalyticsDTO> getHabitAnalytics(@PathVariable UUID userId, @PathVariable UUID habitId) {
        logger.debug("Getting analytics for habit {} of user {}", habitId, userId);
        
        try {
            Optional<HabitAnalytics> analytics = habitAnalyticsRepository.findByUserIdAndHabitId(userId, habitId);
            
            if (analytics.isPresent()) {
                return ResponseEntity.ok(new HabitAnalyticsDTO(analytics.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting analytics for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get habits by formation stage.
     */
    @GetMapping("/users/{userId}/habits/formation-stage/{stage}")
    public ResponseEntity<List<HabitAnalyticsDTO>> getHabitsByFormationStage(@PathVariable UUID userId, 
                                                                            @PathVariable String stage) {
        logger.debug("Getting habits in formation stage {} for user {}", stage, userId);
        
        try {
            HabitAnalytics.FormationStage formationStage = HabitAnalytics.FormationStage.valueOf(stage.toUpperCase());
            List<HabitAnalytics> analytics = habitAnalyticsRepository.findByUserIdAndFormationStage(userId, formationStage);
            
            List<HabitAnalyticsDTO> analyticsDTO = analytics.stream()
                .map(HabitAnalyticsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(analyticsDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid formation stage: {}", stage);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error getting habits by formation stage for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get high-performing habits (above success rate threshold).
     */
    @GetMapping("/users/{userId}/habits/high-performing")
    public ResponseEntity<List<HabitAnalyticsDTO>> getHighPerformingHabits(@PathVariable UUID userId,
                                                                          @RequestParam(defaultValue = "0.7") double threshold) {
        logger.debug("Getting high-performing habits for user {} with threshold {}", userId, threshold);
        
        try {
            List<HabitAnalytics> analytics = habitAnalyticsRepository.findByUserIdAndSuccessRateGreaterThanEqual(userId, threshold);
            
            List<HabitAnalyticsDTO> analyticsDTO = analytics.stream()
                .map(HabitAnalyticsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(analyticsDTO);
        } catch (Exception e) {
            logger.error("Error getting high-performing habits for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get consistent habits (above consistency score threshold).
     */
    @GetMapping("/users/{userId}/habits/consistent")
    public ResponseEntity<List<HabitAnalyticsDTO>> getConsistentHabits(@PathVariable UUID userId,
                                                                      @RequestParam(defaultValue = "0.7") double threshold) {
        logger.debug("Getting consistent habits for user {} with threshold {}", userId, threshold);
        
        try {
            List<HabitAnalytics> analytics = habitAnalyticsRepository.findByUserIdAndConsistencyScoreGreaterThanEqual(userId, threshold);
            
            List<HabitAnalyticsDTO> analyticsDTO = analytics.stream()
                .map(HabitAnalyticsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(analyticsDTO);
        } catch (Exception e) {
            logger.error("Error getting consistent habits for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get habit correlations for a user.
     */
    @GetMapping("/users/{userId}/correlations")
    public ResponseEntity<List<HabitCorrelationDTO>> getHabitCorrelations(@PathVariable UUID userId) {
        logger.debug("Getting habit correlations for user {}", userId);
        
        try {
            List<HabitCorrelation> correlations = habitCorrelationRepository.findByUserId(userId);
            
            List<HabitCorrelationDTO> correlationsDTO = correlations.stream()
                .map(HabitCorrelationDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(correlationsDTO);
        } catch (Exception e) {
            logger.error("Error getting habit correlations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get positive correlations above threshold.
     */
    @GetMapping("/users/{userId}/correlations/positive")
    public ResponseEntity<List<HabitCorrelationDTO>> getPositiveCorrelations(@PathVariable UUID userId,
                                                                            @RequestParam(defaultValue = "0.5") double threshold) {
        logger.debug("Getting positive correlations for user {} with threshold {}", userId, threshold);
        
        try {
            List<HabitCorrelation> correlations = habitCorrelationService.findPositiveCorrelations(userId, threshold);
            
            List<HabitCorrelationDTO> correlationsDTO = correlations.stream()
                .map(HabitCorrelationDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(correlationsDTO);
        } catch (Exception e) {
            logger.error("Error getting positive correlations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get negative correlations below threshold.
     */
    @GetMapping("/users/{userId}/correlations/negative")
    public ResponseEntity<List<HabitCorrelationDTO>> getNegativeCorrelations(@PathVariable UUID userId,
                                                                            @RequestParam(defaultValue = "-0.5") double threshold) {
        logger.debug("Getting negative correlations for user {} with threshold {}", userId, threshold);
        
        try {
            List<HabitCorrelation> correlations = habitCorrelationService.findNegativeCorrelations(userId, threshold);
            
            List<HabitCorrelationDTO> correlationsDTO = correlations.stream()
                .map(HabitCorrelationDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(correlationsDTO);
        } catch (Exception e) {
            logger.error("Error getting negative correlations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get correlations for a specific habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/correlations")
    public ResponseEntity<List<HabitCorrelationDTO>> getHabitCorrelations(@PathVariable UUID userId, @PathVariable UUID habitId) {
        logger.debug("Getting correlations for habit {} of user {}", habitId, userId);
        
        try {
            List<HabitCorrelation> correlations = habitCorrelationRepository.findCorrelationsForHabit(userId, habitId);
            
            List<HabitCorrelationDTO> correlationsDTO = correlations.stream()
                .map(HabitCorrelationDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(correlationsDTO);
        } catch (Exception e) {
            logger.error("Error getting correlations for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get optimal timing analysis for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/optimal-timing")
    public ResponseEntity<OptimalTimingDTO> getOptimalTiming(@PathVariable UUID userId, 
                                                           @PathVariable UUID habitId,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Getting optimal timing for habit {} of user {}", habitId, userId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            OptimalTimingAnalyzer.OptimalTimingResult result = 
                optimalTimingAnalyzer.analyzeOptimalTiming(userId, habitId, startDate, endDate);
            
            // Also get best time windows
            List<OptimalTimingAnalyzer.TimeWindow> timeWindows = 
                optimalTimingAnalyzer.findBestTimeWindows(userId, habitId, startDate, endDate, 3);
            
            OptimalTimingDTO dto = new OptimalTimingDTO(result, timeWindows);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error getting optimal timing for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get best time windows for a habit.
     */
    @GetMapping("/users/{userId}/habits/{habitId}/best-times")
    public ResponseEntity<List<OptimalTimingDTO.TimeWindowDTO>> getBestTimeWindows(@PathVariable UUID userId,
                                                                                  @PathVariable UUID habitId,
                                                                                  @RequestParam(defaultValue = "3") int count,
                                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Getting best time windows for habit {} of user {}", habitId, userId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            List<OptimalTimingAnalyzer.TimeWindow> timeWindows = 
                optimalTimingAnalyzer.findBestTimeWindows(userId, habitId, startDate, endDate, count);
            
            List<OptimalTimingDTO.TimeWindowDTO> windowDTOs = timeWindows.stream()
                .map(OptimalTimingDTO.TimeWindowDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(windowDTOs);
        } catch (Exception e) {
            logger.error("Error getting best time windows for habit {} of user {}: {}", habitId, userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Trigger correlation analysis for a user.
     */
    @PostMapping("/users/{userId}/correlations/analyze")
    public ResponseEntity<String> analyzeCorrelations(@PathVariable UUID userId,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Triggering correlation analysis for user {}", userId);
        
        try {
            // Default to last 60 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(60);
            if (endDate == null) endDate = LocalDate.now();
            
            List<HabitCorrelationService.CorrelationResult> results = 
                habitCorrelationService.analyzeHabitCorrelations(userId, startDate, endDate);
            
            habitCorrelationService.storeCorrelationResults(userId, results);
            
            return ResponseEntity.ok("Correlation analysis completed. Found " + results.size() + " correlations.");
        } catch (Exception e) {
            logger.error("Error analyzing correlations for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error analyzing correlations: " + e.getMessage());
        }
    }
    
    /**
     * Get analytics summary for a user.
     */
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<AnalyticsSummaryDTO> getAnalyticsSummary(@PathVariable UUID userId) {
        logger.debug("Getting analytics summary for user {}", userId);
        
        try {
            List<HabitAnalytics> allAnalytics = habitAnalyticsRepository.findByUserId(userId);
            List<HabitCorrelation> allCorrelations = habitCorrelationRepository.findByUserId(userId);
            
            AnalyticsSummaryDTO summary = new AnalyticsSummaryDTO();
            summary.setUserId(userId);
            summary.setTotalHabits(allAnalytics.size());
            summary.setTotalCorrelations(allCorrelations.size());
            
            if (!allAnalytics.isEmpty()) {
                double avgSuccessRate = allAnalytics.stream()
                    .filter(a -> a.getSuccessRate() != null)
                    .mapToDouble(HabitAnalytics::getSuccessRate)
                    .average()
                    .orElse(0.0);
                
                double avgConsistencyScore = allAnalytics.stream()
                    .filter(a -> a.getConsistencyScore() != null)
                    .mapToDouble(HabitAnalytics::getConsistencyScore)
                    .average()
                    .orElse(0.0);
                
                summary.setAverageSuccessRate(avgSuccessRate);
                summary.setAverageConsistencyScore(avgConsistencyScore);
                
                // Count habits by formation stage
                Map<String, Long> stageCount = allAnalytics.stream()
                    .filter(a -> a.getFormationStage() != null)
                    .collect(Collectors.groupingBy(
                        a -> a.getFormationStage().name(),
                        Collectors.counting()
                    ));
                summary.setHabitsByFormationStage(stageCount);
            }
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting analytics summary for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Summary DTO
    public static class AnalyticsSummaryDTO {
        private UUID userId;
        private int totalHabits;
        private int totalCorrelations;
        private double averageSuccessRate;
        private double averageConsistencyScore;
        private Map<String, Long> habitsByFormationStage;
        
        // Getters and Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public int getTotalHabits() { return totalHabits; }
        public void setTotalHabits(int totalHabits) { this.totalHabits = totalHabits; }
        
        public int getTotalCorrelations() { return totalCorrelations; }
        public void setTotalCorrelations(int totalCorrelations) { this.totalCorrelations = totalCorrelations; }
        
        public double getAverageSuccessRate() { return averageSuccessRate; }
        public void setAverageSuccessRate(double averageSuccessRate) { this.averageSuccessRate = averageSuccessRate; }
        
        public double getAverageConsistencyScore() { return averageConsistencyScore; }
        public void setAverageConsistencyScore(double averageConsistencyScore) { this.averageConsistencyScore = averageConsistencyScore; }
        
        public Map<String, Long> getHabitsByFormationStage() { return habitsByFormationStage; }
        public void setHabitsByFormationStage(Map<String, Long> habitsByFormationStage) { this.habitsByFormationStage = habitsByFormationStage; }
    }
}