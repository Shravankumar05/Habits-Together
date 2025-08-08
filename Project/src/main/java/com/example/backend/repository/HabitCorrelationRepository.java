package com.example.backend.repository;

import com.example.backend.entity.HabitCorrelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitCorrelationRepository extends JpaRepository<HabitCorrelation, UUID> {
    
    // Find correlations by user ID
    List<HabitCorrelation> findByUserId(UUID userId);
    
    // Find correlation between two specific habits
    Optional<HabitCorrelation> findByUserIdAndHabit1IdAndHabit2Id(UUID userId, UUID habit1Id, UUID habit2Id);
    
    // Find correlations involving a specific habit
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId AND (hc.habit1Id = :habitId OR hc.habit2Id = :habitId)")
    List<HabitCorrelation> findCorrelationsForHabit(@Param("userId") UUID userId, @Param("habitId") UUID habitId);
    
    // Find correlations by type
    List<HabitCorrelation> findByUserIdAndCorrelationType(UUID userId, HabitCorrelation.CorrelationType correlationType);
    
    // Find strong correlations (above threshold)
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId AND ABS(hc.correlationCoefficient) >= :threshold ORDER BY ABS(hc.correlationCoefficient) DESC")
    List<HabitCorrelation> findStrongCorrelations(@Param("userId") UUID userId, @Param("threshold") Double threshold);
    
    // Find positive correlations above threshold
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId AND hc.correlationCoefficient >= :threshold ORDER BY hc.correlationCoefficient DESC")
    List<HabitCorrelation> findPositiveCorrelations(@Param("userId") UUID userId, @Param("threshold") Double threshold);
    
    // Find negative correlations below threshold
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId AND hc.correlationCoefficient <= :threshold ORDER BY hc.correlationCoefficient ASC")
    List<HabitCorrelation> findNegativeCorrelations(@Param("userId") UUID userId, @Param("threshold") Double threshold);
    
    // Find correlations with high confidence
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId AND hc.confidenceLevel >= :minConfidence ORDER BY hc.confidenceLevel DESC")
    List<HabitCorrelation> findHighConfidenceCorrelations(@Param("userId") UUID userId, @Param("minConfidence") Double minConfidence);
    
    // Find correlations that need recalculation
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.calculatedAt < :cutoffTime")
    List<HabitCorrelation> findCorrelationsNeedingRecalculation(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Check if correlation exists between two habits (bidirectional)
    @Query("SELECT COUNT(hc) > 0 FROM HabitCorrelation hc WHERE hc.userId = :userId AND ((hc.habit1Id = :habit1Id AND hc.habit2Id = :habit2Id) OR (hc.habit1Id = :habit2Id AND hc.habit2Id = :habit1Id))")
    boolean existsCorrelationBetweenHabits(@Param("userId") UUID userId, @Param("habit1Id") UUID habit1Id, @Param("habit2Id") UUID habit2Id);
    
    // Get correlation matrix data for user
    @Query("SELECT hc FROM HabitCorrelation hc WHERE hc.userId = :userId ORDER BY ABS(hc.correlationCoefficient) DESC")
    List<HabitCorrelation> getCorrelationMatrixForUser(@Param("userId") UUID userId);
    
    // Delete correlations by habit ID (for cleanup)
    @Query("DELETE FROM HabitCorrelation hc WHERE hc.habit1Id = :habitId OR hc.habit2Id = :habitId")
    void deleteCorrelationsByHabitId(@Param("habitId") UUID habitId);
    
    // Count correlations for user
    long countByUserId(UUID userId);
    
    // Get average correlation strength for user
    @Query("SELECT AVG(ABS(hc.correlationCoefficient)) FROM HabitCorrelation hc WHERE hc.userId = :userId")
    Double getAverageCorrelationStrengthForUser(@Param("userId") UUID userId);
}