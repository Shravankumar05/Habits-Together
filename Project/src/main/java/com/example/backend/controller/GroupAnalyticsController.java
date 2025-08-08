package com.example.backend.controller;

import com.example.backend.dto.analytics.GroupDynamicsDTO;
import com.example.backend.dto.analytics.TeamChallengeDTO;
import com.example.backend.entity.GroupMetrics;
import com.example.backend.repository.GroupMetricsRepository;
import com.example.backend.service.analytics.GroupDynamicsEngine;
import com.example.backend.service.analytics.TeamChallengeGenerator;
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
 * REST controller for group analytics endpoints.
 * Provides access to group dynamics, team challenges, and group momentum data.
 */
@RestController
@RequestMapping("/api/group-analytics")
@CrossOrigin(origins = "*")
public class GroupAnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupAnalyticsController.class);
    
    @Autowired
    private GroupDynamicsEngine groupDynamicsEngine;
    
    @Autowired
    private TeamChallengeGenerator teamChallengeGenerator;
    
    @Autowired
    private GroupMetricsRepository groupMetricsRepository;
    
    /**
     * Get group dynamics analysis for a specific group.
     */
    @GetMapping("/groups/{groupId}/dynamics")
    public ResponseEntity<GroupDynamicsDTO> getGroupDynamics(@PathVariable UUID groupId,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Getting group dynamics for group {}", groupId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            GroupDynamicsEngine.GroupDynamicsResult result = 
                groupDynamicsEngine.calculateGroupDynamics(groupId, startDate, endDate);
            
            GroupDynamicsDTO dto = new GroupDynamicsDTO(result);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error getting group dynamics for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get group metrics history.
     */
    @GetMapping("/groups/{groupId}/metrics")
    public ResponseEntity<List<GroupMetricsDTO>> getGroupMetrics(@PathVariable UUID groupId,
                                                                @RequestParam(defaultValue = "10") int limit) {
        logger.debug("Getting group metrics for group {} with limit {}", groupId, limit);
        
        try {
            List<GroupMetrics> metrics = groupMetricsRepository.findByGroupIdOrderByCalculatedAtDesc(groupId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            List<GroupMetricsDTO> metricsDTO = metrics.stream()
                .map(GroupMetricsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(metricsDTO);
        } catch (Exception e) {
            logger.error("Error getting group metrics for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get latest group metrics.
     */
    @GetMapping("/groups/{groupId}/metrics/latest")
    public ResponseEntity<GroupMetricsDTO> getLatestGroupMetrics(@PathVariable UUID groupId) {
        logger.debug("Getting latest group metrics for group {}", groupId);
        
        try {
            Optional<GroupMetrics> metrics = groupMetricsRepository.findTopByGroupIdOrderByCalculatedAtDesc(groupId);
            
            if (metrics.isPresent()) {
                return ResponseEntity.ok(new GroupMetricsDTO(metrics.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting latest group metrics for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get team challenges for a group.
     */
    @GetMapping("/groups/{groupId}/challenges")
    public ResponseEntity<List<TeamChallengeDTO>> getTeamChallenges(@PathVariable UUID groupId,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Getting team challenges for group {}", groupId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            List<TeamChallengeGenerator.TeamChallenge> challenges = 
                teamChallengeGenerator.generateChallengesForGroup(groupId, startDate, endDate);
            
            List<TeamChallengeDTO> challengesDTO = challenges.stream()
                .map(TeamChallengeDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(challengesDTO);
        } catch (Exception e) {
            logger.error("Error getting team challenges for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Generate new challenges for a group.
     */
    @PostMapping("/groups/{groupId}/challenges/generate")
    public ResponseEntity<List<TeamChallengeDTO>> generateTeamChallenges(@PathVariable UUID groupId,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Generating new team challenges for group {}", groupId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            List<TeamChallengeGenerator.TeamChallenge> challenges = 
                teamChallengeGenerator.generateChallengesForGroup(groupId, startDate, endDate);
            
            List<TeamChallengeDTO> challengesDTO = challenges.stream()
                .map(TeamChallengeDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(challengesDTO);
        } catch (Exception e) {
            logger.error("Error generating team challenges for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Track challenge progress.
     */
    @GetMapping("/groups/{groupId}/challenges/{challengeId}/progress")
    public ResponseEntity<ChallengeProgressDTO> getChallengeProgress(@PathVariable UUID groupId,
                                                                    @PathVariable UUID challengeId) {
        logger.debug("Getting challenge progress for challenge {} in group {}", challengeId, groupId);
        
        try {
            TeamChallengeGenerator.ChallengeProgress progress = 
                teamChallengeGenerator.trackChallengeProgress(challengeId, groupId, LocalDate.now());
            
            ChallengeProgressDTO dto = new ChallengeProgressDTO(progress);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error getting challenge progress for challenge {} in group {}: {}", challengeId, groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get group momentum trends.
     */
    @GetMapping("/groups/{groupId}/momentum")
    public ResponseEntity<GroupMomentumDTO> getGroupMomentum(@PathVariable UUID groupId,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Getting group momentum for group {}", groupId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            GroupDynamicsEngine.GroupDynamicsResult dynamics = 
                groupDynamicsEngine.calculateGroupDynamics(groupId, startDate, endDate);
            
            GroupMomentumDTO dto = new GroupMomentumDTO(
                groupId,
                dynamics.getMomentumScore(),
                dynamics.getGroupStreak(),
                dynamics.getParticipationMetrics().getParticipationRate(),
                startDate,
                endDate
            );
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error getting group momentum for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get key contributors for a group.
     */
    @GetMapping("/groups/{groupId}/contributors")
    public ResponseEntity<List<GroupDynamicsDTO.KeyContributorDTO>> getKeyContributors(@PathVariable UUID groupId,
                                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                                       @RequestParam(defaultValue = "10") int limit) {
        logger.debug("Getting key contributors for group {}", groupId);
        
        try {
            // Default to last 30 days if dates not provided
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            GroupDynamicsEngine.GroupDynamicsResult dynamics = 
                groupDynamicsEngine.calculateGroupDynamics(groupId, startDate, endDate);
            
            List<GroupDynamicsDTO.KeyContributorDTO> contributors = dynamics.getKeyContributors().stream()
                .limit(limit)
                .map(GroupDynamicsDTO.KeyContributorDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(contributors);
        } catch (Exception e) {
            logger.error("Error getting key contributors for group {}: {}", groupId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get groups with high momentum.
     */
    @GetMapping("/groups/high-momentum")
    public ResponseEntity<List<GroupMetricsDTO>> getHighMomentumGroups(@RequestParam(defaultValue = "0.7") double threshold,
                                                                       @RequestParam(defaultValue = "10") int limit) {
        logger.debug("Getting high momentum groups with threshold {}", threshold);
        
        try {
            List<GroupMetrics> highMomentumGroups = groupMetricsRepository.findByMomentumScoreGreaterThanEqual(threshold)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            List<GroupMetricsDTO> groupsDTO = highMomentumGroups.stream()
                .map(GroupMetricsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(groupsDTO);
        } catch (Exception e) {
            logger.error("Error getting high momentum groups: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get groups with high cohesion.
     */
    @GetMapping("/groups/high-cohesion")
    public ResponseEntity<List<GroupMetricsDTO>> getHighCohesionGroups(@RequestParam(defaultValue = "0.7") double threshold,
                                                                       @RequestParam(defaultValue = "10") int limit) {
        logger.debug("Getting high cohesion groups with threshold {}", threshold);
        
        try {
            List<GroupMetrics> highCohesionGroups = groupMetricsRepository.findByCohesionScoreGreaterThanEqual(threshold)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            List<GroupMetricsDTO> groupsDTO = highCohesionGroups.stream()
                .map(GroupMetricsDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(groupsDTO);
        } catch (Exception e) {
            logger.error("Error getting high cohesion groups: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // DTOs
    
    public static class GroupMetricsDTO {
        private UUID id;
        private UUID groupId;
        private Integer groupStreak;
        private Double momentumScore;
        private Double synergisticScore;
        private Double cohesionScore;
        private LocalDate calculatedAt;
        
        public GroupMetricsDTO() {}
        
        public GroupMetricsDTO(GroupMetrics metrics) {
            this.id = metrics.getId();
            this.groupId = metrics.getGroupId();
            this.groupStreak = metrics.getGroupStreak();
            this.momentumScore = metrics.getMomentumScore();
            this.synergisticScore = metrics.getSynergisticScore();
            this.cohesionScore = metrics.getCohesionScore();
            this.calculatedAt = metrics.getCalculatedAt().toLocalDate();
        }
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }
        
        public Integer getGroupStreak() { return groupStreak; }
        public void setGroupStreak(Integer groupStreak) { this.groupStreak = groupStreak; }
        
        public Double getMomentumScore() { return momentumScore; }
        public void setMomentumScore(Double momentumScore) { this.momentumScore = momentumScore; }
        
        public Double getSynergisticScore() { return synergisticScore; }
        public void setSynergisticScore(Double synergisticScore) { this.synergisticScore = synergisticScore; }
        
        public Double getCohesionScore() { return cohesionScore; }
        public void setCohesionScore(Double cohesionScore) { this.cohesionScore = cohesionScore; }
        
        public LocalDate getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(LocalDate calculatedAt) { this.calculatedAt = calculatedAt; }
    }
    
    public static class ChallengeProgressDTO {
        private UUID challengeId;
        private UUID groupId;
        private double progressPercentage;
        private String status;
        private LocalDate lastUpdated;
        private int rewardPoints;
        
        public ChallengeProgressDTO() {}
        
        public ChallengeProgressDTO(TeamChallengeGenerator.ChallengeProgress progress) {
            this.challengeId = progress.getChallengeId();
            this.groupId = progress.getGroupId();
            this.progressPercentage = progress.getProgressPercentage();
            this.status = progress.getStatus().name();
            this.lastUpdated = progress.getLastUpdated();
            this.rewardPoints = progress.getRewardPoints();
        }
        
        // Getters and Setters
        public UUID getChallengeId() { return challengeId; }
        public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }
        
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }
        
        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDate getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
        
        public int getRewardPoints() { return rewardPoints; }
        public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
    }
    
    public static class GroupMomentumDTO {
        private UUID groupId;
        private double momentumScore;
        private int groupStreak;
        private double participationRate;
        private LocalDate startDate;
        private LocalDate endDate;
        
        public GroupMomentumDTO() {}
        
        public GroupMomentumDTO(UUID groupId, double momentumScore, int groupStreak, 
                               double participationRate, LocalDate startDate, LocalDate endDate) {
            this.groupId = groupId;
            this.momentumScore = momentumScore;
            this.groupStreak = groupStreak;
            this.participationRate = participationRate;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters and Setters
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }
        
        public double getMomentumScore() { return momentumScore; }
        public void setMomentumScore(double momentumScore) { this.momentumScore = momentumScore; }
        
        public int getGroupStreak() { return groupStreak; }
        public void setGroupStreak(int groupStreak) { this.groupStreak = groupStreak; }
        
        public double getParticipationRate() { return participationRate; }
        public void setParticipationRate(double participationRate) { this.participationRate = participationRate; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}