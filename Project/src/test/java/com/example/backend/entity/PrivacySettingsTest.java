package com.example.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
class PrivacySettingsTest {

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void testPrivacySettingsCreation() {
        // Given
        PrivacySettings settings = new PrivacySettings(userId);
        settings.setShareAnalytics(true);
        settings.setShareProgress(false);
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.ANONYMOUS);
        settings.setAnonymizeData(true);
        settings.setEnableSocialFeatures(false);
        settings.setShareCorrelations(true);
        settings.setShareOptimalTiming(true);
        settings.setEnableSmartNotifications(false);

        // Then
        assertThat(settings.getUserId()).isEqualTo(userId);
        assertThat(settings.getShareAnalytics()).isTrue();
        assertThat(settings.getShareProgress()).isFalse();
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.ANONYMOUS);
        assertThat(settings.getAnonymizeData()).isTrue();
        assertThat(settings.getEnableSocialFeatures()).isFalse();
        assertThat(settings.getShareCorrelations()).isTrue();
        assertThat(settings.getShareOptimalTiming()).isTrue();
        assertThat(settings.getEnableSmartNotifications()).isFalse();
        assertThat(settings.getCreatedAt()).isNotNull();
        assertThat(settings.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDefaultConstructor() {
        // Given
        PrivacySettings settings = new PrivacySettings();

        // Then
        assertThat(settings.getId()).isNull();
        assertThat(settings.getUserId()).isNull();
        assertThat(settings.getShareAnalytics()).isEqualTo(false);
        assertThat(settings.getShareProgress()).isEqualTo(true);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.GROUP_ONLY);
        assertThat(settings.getAnonymizeData()).isEqualTo(false);
        assertThat(settings.getEnableSocialFeatures()).isEqualTo(true);
        assertThat(settings.getShareCorrelations()).isEqualTo(false);
        assertThat(settings.getShareOptimalTiming()).isEqualTo(false);
        assertThat(settings.getEnableSmartNotifications()).isEqualTo(true);
    }

    @Test
    void testVisibilityLevelEnum() {
        // Test all visibility levels
        PrivacySettings settings = new PrivacySettings();

        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.PRIVATE);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PRIVATE);

        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.GROUP_ONLY);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.GROUP_ONLY);

        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.ANONYMOUS);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.ANONYMOUS);

        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PUBLIC);
    }

    @Test
    void testPrePersistCallback() {
        // Given
        PrivacySettings settings = new PrivacySettings();
        settings.setUserId(userId);

        // When
        settings.onCreate();

        // Then
        assertThat(settings.getCreatedAt()).isNotNull();
        assertThat(settings.getUpdatedAt()).isNotNull();
        assertThat(settings.getCreatedAt()).isEqualTo(settings.getUpdatedAt());
    }

    @Test
    void testPreUpdateCallback() {
        // Given
        PrivacySettings settings = new PrivacySettings(userId);
        LocalDateTime originalUpdatedAt = settings.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        settings.onUpdate();

        // Then
        assertThat(settings.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        PrivacySettings settings = new PrivacySettings();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        settings.setId(id);
        settings.setUserId(userId);
        settings.setShareAnalytics(true);
        settings.setShareProgress(false);
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);
        settings.setAnonymizeData(true);
        settings.setEnableSocialFeatures(false);
        settings.setShareCorrelations(true);
        settings.setShareOptimalTiming(false);
        settings.setEnableSmartNotifications(true);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);

        // Then
        assertThat(settings.getId()).isEqualTo(id);
        assertThat(settings.getUserId()).isEqualTo(userId);
        assertThat(settings.getShareAnalytics()).isTrue();
        assertThat(settings.getShareProgress()).isFalse();
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PUBLIC);
        assertThat(settings.getAnonymizeData()).isTrue();
        assertThat(settings.getEnableSocialFeatures()).isFalse();
        assertThat(settings.getShareCorrelations()).isTrue();
        assertThat(settings.getShareOptimalTiming()).isFalse();
        assertThat(settings.getEnableSmartNotifications()).isTrue();
        assertThat(settings.getCreatedAt()).isEqualTo(now);
        assertThat(settings.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testDefaultValues() {
        // Test that default values are set correctly
        PrivacySettings settings = new PrivacySettings();

        // Verify default values match the design requirements
        assertThat(settings.getShareAnalytics()).isFalse(); // Default: false for privacy
        assertThat(settings.getShareProgress()).isTrue(); // Default: true for group functionality
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.GROUP_ONLY); // Default: group only
        assertThat(settings.getAnonymizeData()).isFalse(); // Default: false
        assertThat(settings.getEnableSocialFeatures()).isTrue(); // Default: true for engagement
        assertThat(settings.getShareCorrelations()).isFalse(); // Default: false for privacy
        assertThat(settings.getShareOptimalTiming()).isFalse(); // Default: false for privacy
        assertThat(settings.getEnableSmartNotifications()).isTrue(); // Default: true for user experience
    }

    @Test
    void testPrivacyLevelsHierarchy() {
        // Test that privacy levels represent increasing levels of sharing
        PrivacySettings settings = new PrivacySettings();

        // PRIVATE - most restrictive
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.PRIVATE);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PRIVATE);

        // GROUP_ONLY - moderate sharing
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.GROUP_ONLY);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.GROUP_ONLY);

        // ANONYMOUS - shared but anonymized
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.ANONYMOUS);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.ANONYMOUS);

        // PUBLIC - least restrictive
        settings.setVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);
        assertThat(settings.getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PUBLIC);
    }
}