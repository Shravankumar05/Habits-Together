package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "privacy_settings", schema = "public")
public class PrivacySettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    
    @Column(name = "share_analytics")
    private Boolean shareAnalytics = false;
    
    @Column(name = "share_progress")
    private Boolean shareProgress = true;
    
    @Column(name = "visibility_level")
    @Enumerated(EnumType.STRING)
    private VisibilityLevel visibilityLevel = VisibilityLevel.GROUP_ONLY;
    
    @Column(name = "anonymize_data")
    private Boolean anonymizeData = false;
    
    @Column(name = "enable_social_features")
    private Boolean enableSocialFeatures = true;
    
    @Column(name = "share_correlations")
    private Boolean shareCorrelations = false;
    
    @Column(name = "share_optimal_timing")
    private Boolean shareOptimalTiming = false;
    
    @Column(name = "enable_smart_notifications")
    private Boolean enableSmartNotifications = true;
    
    @Column(name = "created_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime updatedAt;
    
    public enum VisibilityLevel {
        PRIVATE,
        GROUP_ONLY,
        ANONYMOUS,
        PUBLIC
    }
    
    public PrivacySettings() {}
    
    public PrivacySettings(UUID userId) {
        this.userId = userId;
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
    
    public Boolean getShareAnalytics() {
        return shareAnalytics;
    }
    
    public void setShareAnalytics(Boolean shareAnalytics) {
        this.shareAnalytics = shareAnalytics;
    }
    
    public Boolean getShareProgress() {
        return shareProgress;
    }
    
    public void setShareProgress(Boolean shareProgress) {
        this.shareProgress = shareProgress;
    }
    
    public VisibilityLevel getVisibilityLevel() {
        return visibilityLevel;
    }
    
    public void setVisibilityLevel(VisibilityLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }
    
    public Boolean getAnonymizeData() {
        return anonymizeData;
    }
    
    public void setAnonymizeData(Boolean anonymizeData) {
        this.anonymizeData = anonymizeData;
    }
    
    public Boolean getEnableSocialFeatures() {
        return enableSocialFeatures;
    }
    
    public void setEnableSocialFeatures(Boolean enableSocialFeatures) {
        this.enableSocialFeatures = enableSocialFeatures;
    }
    
    public Boolean getShareCorrelations() {
        return shareCorrelations;
    }
    
    public void setShareCorrelations(Boolean shareCorrelations) {
        this.shareCorrelations = shareCorrelations;
    }
    
    public Boolean getShareOptimalTiming() {
        return shareOptimalTiming;
    }
    
    public void setShareOptimalTiming(Boolean shareOptimalTiming) {
        this.shareOptimalTiming = shareOptimalTiming;
    }
    
    public Boolean getEnableSmartNotifications() {
        return enableSmartNotifications;
    }
    
    public void setEnableSmartNotifications(Boolean enableSmartNotifications) {
        this.enableSmartNotifications = enableSmartNotifications;
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