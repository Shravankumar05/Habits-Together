package com.example.backend.repository;

import com.example.backend.entity.SmartNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SmartNotificationRepository extends JpaRepository<SmartNotification, UUID> {
    
    // Find notifications by user ID
    List<SmartNotification> findByUserId(UUID userId);
    
    // Find notifications by user and habit
    List<SmartNotification> findByUserIdAndHabitId(UUID userId, UUID habitId);
    
    // Find notifications by type
    List<SmartNotification> findByUserIdAndNotificationType(UUID userId, SmartNotification.NotificationType notificationType);
    
    // Find pending notifications (not yet sent)
    @Query("SELECT sn FROM SmartNotification sn WHERE sn.userId = :userId AND sn.sentAt IS NULL AND sn.optimalTiming <= :currentTime ORDER BY sn.optimalTiming ASC")
    List<SmartNotification> findPendingNotifications(@Param("userId") UUID userId, @Param("currentTime") LocalDateTime currentTime);
    
    // Find sent notifications with no response
    @Query("SELECT sn FROM SmartNotification sn WHERE sn.userId = :userId AND sn.sentAt IS NOT NULL AND sn.response IS NULL")
    List<SmartNotification> findSentNotificationsWithoutResponse(@Param("userId") UUID userId);
    
    // Find notifications by response type
    List<SmartNotification> findByUserIdAndResponse(UUID userId, SmartNotification.NotificationResponse response);
    
    // Find notifications within time range
    @Query("SELECT sn FROM SmartNotification sn WHERE sn.userId = :userId AND sn.sentAt BETWEEN :startTime AND :endTime ORDER BY sn.sentAt DESC")
    List<SmartNotification> findByUserIdAndSentAtBetween(@Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // Find notifications scheduled for optimal timing
    @Query("SELECT sn FROM SmartNotification sn WHERE sn.optimalTiming BETWEEN :startTime AND :endTime AND sn.sentAt IS NULL ORDER BY sn.optimalTiming ASC")
    List<SmartNotification> findScheduledNotifications(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // Get notification response rate for user
    @Query("SELECT COUNT(sn) * 100.0 / (SELECT COUNT(sn2) FROM SmartNotification sn2 WHERE sn2.userId = :userId AND sn2.sentAt IS NOT NULL) FROM SmartNotification sn WHERE sn.userId = :userId AND sn.response IS NOT NULL")
    Double getResponseRateForUser(@Param("userId") UUID userId);
    
    // Find notifications by motivation level
    @Query("SELECT sn FROM SmartNotification sn WHERE sn.userId = :userId AND sn.motivationLevel >= :minLevel ORDER BY sn.motivationLevel DESC")
    List<SmartNotification> findByUserIdAndMotivationLevelGreaterThanEqual(@Param("userId") UUID userId, @Param("minLevel") Integer minLevel);
    
    // Count notifications by type for user
    @Query("SELECT COUNT(sn) FROM SmartNotification sn WHERE sn.userId = :userId AND sn.notificationType = :type")
    long countByUserIdAndNotificationType(@Param("userId") UUID userId, @Param("type") SmartNotification.NotificationType type);
    
    // Delete old notifications (for cleanup)
    @Query("DELETE FROM SmartNotification sn WHERE sn.createdAt < :cutoffTime")
    void deleteOldNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Check if user has pending notifications
    @Query("SELECT COUNT(sn) > 0 FROM SmartNotification sn WHERE sn.userId = :userId AND sn.sentAt IS NULL")
    boolean hasPendingNotifications(@Param("userId") UUID userId);
}