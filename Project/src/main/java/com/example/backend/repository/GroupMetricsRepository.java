package com.example.backend.repository;

import com.example.backend.entity.GroupMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMetricsRepository extends JpaRepository<GroupMetrics, UUID> {
    
    // Find latest metrics for a group
    Optional<GroupMetrics> findTopByGroupIdOrderByCalculatedAtDesc(UUID groupId);
    
    // Find all metrics for a group ordered by calculation time
    List<GroupMetrics> findByGroupIdOrderByCalculatedAtDesc(UUID groupId);
    
    // Find metrics calculated within a time range
    @Query("SELECT gm FROM GroupMetrics gm WHERE gm.groupId = :groupId AND gm.calculatedAt BETWEEN :startTime AND :endTime ORDER BY gm.calculatedAt DESC")
    List<GroupMetrics> findByGroupIdAndCalculatedAtBetween(@Param("groupId") UUID groupId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // Find groups with momentum score above threshold
    @Query("SELECT gm FROM GroupMetrics gm WHERE gm.momentumScore >= :threshold ORDER BY gm.momentumScore DESC")
    List<GroupMetrics> findByMomentumScoreGreaterThanEqual(@Param("threshold") Double threshold);
    
    // Find groups with cohesion score above threshold
    @Query("SELECT gm FROM GroupMetrics gm WHERE gm.cohesionScore >= :threshold ORDER BY gm.cohesionScore DESC")
    List<GroupMetrics> findByCohesionScoreGreaterThanEqual(@Param("threshold") Double threshold);
    
    // Find metrics that need recalculation (older than specified time)
    @Query("SELECT gm FROM GroupMetrics gm WHERE gm.calculatedAt < :cutoffTime")
    List<GroupMetrics> findMetricsNeedingRecalculation(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Get average momentum score across all groups
    @Query("SELECT AVG(gm.momentumScore) FROM GroupMetrics gm WHERE gm.calculatedAt >= :since")
    Double getAverageMomentumScoreSince(@Param("since") LocalDateTime since);
    
    // Get average cohesion score across all groups
    @Query("SELECT AVG(gm.cohesionScore) FROM GroupMetrics gm WHERE gm.calculatedAt >= :since")
    Double getAverageCohesionScoreSince(@Param("since") LocalDateTime since);
    
    // Check if metrics exist for group
    boolean existsByGroupId(UUID groupId);
    
    // Delete old metrics (for cleanup)
    @Query("DELETE FROM GroupMetrics gm WHERE gm.calculatedAt < :cutoffTime")
    void deleteOldMetrics(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Count metrics by group
    long countByGroupId(UUID groupId);
}