package com.example.backend.repository;

import com.example.backend.entity.PrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID> {
    
    // Find privacy settings by user ID
    Optional<PrivacySettings> findByUserId(UUID userId);
    
    // Find users who share analytics
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.shareAnalytics = true")
    List<PrivacySettings> findUsersWhoShareAnalytics();
    
    // Find users who share progress
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.shareProgress = true")
    List<PrivacySettings> findUsersWhoShareProgress();
    
    // Find users by visibility level
    List<PrivacySettings> findByVisibilityLevel(PrivacySettings.VisibilityLevel visibilityLevel);
    
    // Find users who enable social features
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.enableSocialFeatures = true")
    List<PrivacySettings> findUsersWithSocialFeaturesEnabled();
    
    // Find users who enable smart notifications
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.enableSmartNotifications = true")
    List<PrivacySettings> findUsersWithSmartNotificationsEnabled();
    
    // Find users who share correlations
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.shareCorrelations = true")
    List<PrivacySettings> findUsersWhoShareCorrelations();
    
    // Find users who share optimal timing
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.shareOptimalTiming = true")
    List<PrivacySettings> findUsersWhoShareOptimalTiming();
    
    // Find users who anonymize data
    @Query("SELECT ps FROM PrivacySettings ps WHERE ps.anonymizeData = true")
    List<PrivacySettings> findUsersWhoAnonymizeData();
    
    // Check if user exists in privacy settings
    boolean existsByUserId(UUID userId);
    
    // Get privacy level for specific feature
    @Query("SELECT ps.shareAnalytics FROM PrivacySettings ps WHERE ps.userId = :userId")
    Optional<Boolean> getAnalyticsSharingPreference(@Param("userId") UUID userId);
    
    @Query("SELECT ps.shareProgress FROM PrivacySettings ps WHERE ps.userId = :userId")
    Optional<Boolean> getProgressSharingPreference(@Param("userId") UUID userId);
    
    @Query("SELECT ps.enableSocialFeatures FROM PrivacySettings ps WHERE ps.userId = :userId")
    Optional<Boolean> getSocialFeaturesPreference(@Param("userId") UUID userId);
    
    @Query("SELECT ps.enableSmartNotifications FROM PrivacySettings ps WHERE ps.userId = :userId")
    Optional<Boolean> getSmartNotificationsPreference(@Param("userId") UUID userId);
    
    // Count users by privacy preferences
    @Query("SELECT COUNT(ps) FROM PrivacySettings ps WHERE ps.shareAnalytics = true")
    long countUsersWhoShareAnalytics();
    
    @Query("SELECT COUNT(ps) FROM PrivacySettings ps WHERE ps.enableSocialFeatures = true")
    long countUsersWithSocialFeatures();
    
    // Delete privacy settings by user ID
    void deleteByUserId(UUID userId);
}