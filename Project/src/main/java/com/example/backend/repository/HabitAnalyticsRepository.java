package com.example.backend.repository;

import com.example.backend.entity.HabitAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitAnalyticsRepository extends JpaRepository<HabitAnalytics, UUID> {
    
    // Find analytics by user ID
    List<HabitAnalytics> findByUserId(UUID userId);
    
    // Find analytics by habit ID
    Optional<HabitAnalytics> findByUserIdAndHabitId(UUID userId, UUID habitId);
    
    // Find analytics by formation stage
    List<HabitAnalytics> findByUserIdAndFormationStage(UUID userId, HabitAnalytics.FormationStage formationStage);
    
    // Find analytics that need updating (older than specified time)
    @Query("SELECT ha FROM HabitAnalytics ha WHERE ha.lastAnalyzed < :cutoffTime")
    List<HabitAnalytics> findAnalyticsNeedingUpdate(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find analytics with success rate above threshold
    @Query("SELECT ha FROM HabitAnalytics ha WHERE ha.userId = :userId AND ha.successRate >= :threshold")
    List<HabitAnalytics> findByUserIdAndSuccessRateGreaterThanEqual(@Param("userId") UUID userId, @Param("threshold") Double threshold);
    
    // Find analytics with consistency score above threshold
    @Query("SELECT ha FROM HabitAnalytics ha WHERE ha.userId = :userId AND ha.consistencyScore >= :threshold")
    List<HabitAnalytics> findByUserIdAndConsistencyScoreGreaterThanEqual(@Param("userId") UUID userId, @Param("threshold") Double threshold);
    
    // Check if analytics exists for user and habit
    boolean existsByUserIdAndHabitId(UUID userId, UUID habitId);
    
    // Delete analytics by habit ID (for cleanup)
    void deleteByHabitId(UUID habitId);
    
    // Count analytics by user
    long countByUserId(UUID userId);
}