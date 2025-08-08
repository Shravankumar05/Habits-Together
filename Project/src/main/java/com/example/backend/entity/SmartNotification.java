package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "smart_notifications", schema = "public")
public class SmartNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "habit_id")
    private UUID habitId;
    
    @Column(name = "optimal_timing")
    private LocalDateTime optimalTiming;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "motivation_level")
    private Integer motivationLevel;
    
    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "response")
    @Enumerated(EnumType.STRING)
    private NotificationResponse response;
    
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime updatedAt;
    
    public enum NotificationType {
        HABIT_REMINDER,
        MOTIVATIONAL,
        GROUP_ENCOURAGEMENT,
        STREAK_CELEBRATION,
        OPTIMAL_TIMING,
        FORMATION_MILESTONE
    }
    
    public enum NotificationResponse {
        OPENED,
        DISMISSED,
        COMPLETED_HABIT,
        SNOOZED,
        NO_RESPONSE
    }
    
    public SmartNotification() {}
    
    public SmartNotification(UUID userId, UUID habitId, String message, NotificationType notificationType) {
        this.userId = userId;
        this.habitId = habitId;
        this.message = message;
        this.notificationType = notificationType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getHabitId() {
        return habitId;
    }
    
    public void setHabitId(UUID habitId) {
        this.habitId = habitId;
    }
    
    public LocalDateTime getOptimalTiming() {
        return optimalTiming;
    }
    
    public void setOptimalTiming(LocalDateTime optimalTiming) {
        this.optimalTiming = optimalTiming;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getMotivationLevel() {
        return motivationLevel;
    }
    
    public void setMotivationLevel(Integer motivationLevel) {
        this.motivationLevel = motivationLevel;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public NotificationResponse getResponse() {
        return response;
    }
    
    public void setResponse(NotificationResponse response) {
        this.response = response;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}