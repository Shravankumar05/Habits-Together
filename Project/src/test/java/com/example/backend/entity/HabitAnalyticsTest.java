package com.example.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
class HabitAnalyticsTest {

    private UUID userId;
    private UUID habitId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
    }

    @Test
    void testHabitAnalyticsCreation() {
        // Given
        HabitAnalytics analytics = new HabitAnalytics(userId, habitId);
        analytics.setSuccessRate(0.85);
        analytics.setConsistencyScore(0.75);
        analytics.setOptimalTimeStart(LocalTime.of(8, 0));
        analytics.setOptimalTimeEnd(LocalTime.of(9, 0));
        analytics.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        analytics.setHabitStrength(0.65);

        // Then
        assertThat(analytics.getUserId()).isEqualTo(userId);
        assertThat(analytics.getHabitId()).isEqualTo(habitId);
        assertThat(analytics.getSuccessRate()).isEqualTo(0.85);
        assertThat(analytics.getConsistencyScore()).isEqualTo(0.75);
        assertThat(analytics.getOptimalTimeStart()).isEqualTo(LocalTime.of(8, 0));
        assertThat(analytics.getOptimalTimeEnd()).isEqualTo(LocalTime.of(9, 0));
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.LEARNING);
        assertThat(analytics.getHabitStrength()).isEqualTo(0.65);
        assertThat(analytics.getCreatedAt()).isNotNull();
        assertThat(analytics.getUpdatedAt()).isNotNull();
        assertThat(analytics.getLastAnalyzed()).isNotNull();
    }

    @Test
    void testDefaultConstructor() {
        // Given
        HabitAnalytics analytics = new HabitAnalytics();

        // Then
        assertThat(analytics.getId()).isNull();
        assertThat(analytics.getUserId()).isNull();
        assertThat(analytics.getHabitId()).isNull();
        assertThat(analytics.getSuccessRate()).isNull();
        assertThat(analytics.getConsistencyScore()).isNull();
        assertThat(analytics.getFormationStage()).isNull();
        assertThat(analytics.getHabitStrength()).isNull();
    }

    @Test
    void testFormationStageEnum() {
        // Test all formation stages
        HabitAnalytics analytics = new HabitAnalytics(userId, habitId);

        analytics.setFormationStage(HabitAnalytics.FormationStage.INITIATION);
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.INITIATION);

        analytics.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.LEARNING);

        analytics.setFormationStage(HabitAnalytics.FormationStage.STABILITY);
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.STABILITY);

        analytics.setFormationStage(HabitAnalytics.FormationStage.MASTERY);
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.MASTERY);
    }

    @Test
    void testPrePersistCallback() {
        // Given
        HabitAnalytics analytics = new HabitAnalytics();
        analytics.setUserId(userId);
        analytics.setHabitId(habitId);

        // When
        analytics.onCreate();

        // Then
        assertThat(analytics.getCreatedAt()).isNotNull();
        assertThat(analytics.getUpdatedAt()).isNotNull();
        assertThat(analytics.getLastAnalyzed()).isNotNull();
        assertThat(analytics.getCreatedAt()).isEqualTo(analytics.getUpdatedAt());
    }

    @Test
    void testPreUpdateCallback() {
        // Given
        HabitAnalytics analytics = new HabitAnalytics(userId, habitId);
        LocalDateTime originalUpdatedAt = analytics.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        analytics.onUpdate();

        // Then
        assertThat(analytics.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        HabitAnalytics analytics = new HabitAnalytics();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = LocalTime.of(7, 30);
        LocalTime endTime = LocalTime.of(8, 30);

        // When
        analytics.setId(id);
        analytics.setUserId(userId);
        analytics.setHabitId(habitId);
        analytics.setSuccessRate(0.9);
        analytics.setConsistencyScore(0.8);
        analytics.setOptimalTimeStart(startTime);
        analytics.setOptimalTimeEnd(endTime);
        analytics.setFormationStage(HabitAnalytics.FormationStage.STABILITY);
        analytics.setHabitStrength(0.7);
        analytics.setLastAnalyzed(now);
        analytics.setCreatedAt(now);
        analytics.setUpdatedAt(now);

        // Then
        assertThat(analytics.getId()).isEqualTo(id);
        assertThat(analytics.getUserId()).isEqualTo(userId);
        assertThat(analytics.getHabitId()).isEqualTo(habitId);
        assertThat(analytics.getSuccessRate()).isEqualTo(0.9);
        assertThat(analytics.getConsistencyScore()).isEqualTo(0.8);
        assertThat(analytics.getOptimalTimeStart()).isEqualTo(startTime);
        assertThat(analytics.getOptimalTimeEnd()).isEqualTo(endTime);
        assertThat(analytics.getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.STABILITY);
        assertThat(analytics.getHabitStrength()).isEqualTo(0.7);
        assertThat(analytics.getLastAnalyzed()).isEqualTo(now);
        assertThat(analytics.getCreatedAt()).isEqualTo(now);
        assertThat(analytics.getUpdatedAt()).isEqualTo(now);
    }
}