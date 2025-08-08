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
class GroupMetricsTest {

    private UUID groupId;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();
    }

    @Test
    void testGroupMetricsCreation() {
        // Given
        GroupMetrics metrics = new GroupMetrics(groupId);
        metrics.setGroupStreak(15);
        metrics.setMomentumScore(0.85);
        metrics.setSynergisticScore(0.75);
        metrics.setCohesionScore(0.90);

        // Then
        assertThat(metrics.getGroupId()).isEqualTo(groupId);
        assertThat(metrics.getGroupStreak()).isEqualTo(15);
        assertThat(metrics.getMomentumScore()).isEqualTo(0.85);
        assertThat(metrics.getSynergisticScore()).isEqualTo(0.75);
        assertThat(metrics.getCohesionScore()).isEqualTo(0.90);
        assertThat(metrics.getCalculatedAt()).isNotNull();
        assertThat(metrics.getCreatedAt()).isNotNull();
        assertThat(metrics.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDefaultConstructor() {
        // Given
        GroupMetrics metrics = new GroupMetrics();

        // Then
        assertThat(metrics.getId()).isNull();
        assertThat(metrics.getGroupId()).isNull();
        assertThat(metrics.getGroupStreak()).isNull();
        assertThat(metrics.getMomentumScore()).isNull();
        assertThat(metrics.getSynergisticScore()).isNull();
        assertThat(metrics.getCohesionScore()).isNull();
        assertThat(metrics.getCalculatedAt()).isNull();
    }

    @Test
    void testPrePersistCallback() {
        // Given
        GroupMetrics metrics = new GroupMetrics();
        metrics.setGroupId(groupId);

        // When
        metrics.onCreate();

        // Then
        assertThat(metrics.getCreatedAt()).isNotNull();
        assertThat(metrics.getUpdatedAt()).isNotNull();
        assertThat(metrics.getCalculatedAt()).isNotNull();
        assertThat(metrics.getCreatedAt()).isEqualTo(metrics.getUpdatedAt());
    }

    @Test
    void testPreUpdateCallback() {
        // Given
        GroupMetrics metrics = new GroupMetrics(groupId);
        LocalDateTime originalUpdatedAt = metrics.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        metrics.onUpdate();

        // Then
        assertThat(metrics.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        GroupMetrics metrics = new GroupMetrics();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        metrics.setId(id);
        metrics.setGroupId(groupId);
        metrics.setGroupStreak(25);
        metrics.setMomentumScore(0.95);
        metrics.setSynergisticScore(0.88);
        metrics.setCohesionScore(0.92);
        metrics.setCalculatedAt(now);
        metrics.setCreatedAt(now);
        metrics.setUpdatedAt(now);

        // Then
        assertThat(metrics.getId()).isEqualTo(id);
        assertThat(metrics.getGroupId()).isEqualTo(groupId);
        assertThat(metrics.getGroupStreak()).isEqualTo(25);
        assertThat(metrics.getMomentumScore()).isEqualTo(0.95);
        assertThat(metrics.getSynergisticScore()).isEqualTo(0.88);
        assertThat(metrics.getCohesionScore()).isEqualTo(0.92);
        assertThat(metrics.getCalculatedAt()).isEqualTo(now);
        assertThat(metrics.getCreatedAt()).isEqualTo(now);
        assertThat(metrics.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testCalculatedAtDefaultBehavior() {
        // Given
        GroupMetrics metrics = new GroupMetrics();
        metrics.setGroupId(groupId);
        LocalDateTime customCalculatedAt = LocalDateTime.now().minusHours(1);
        metrics.setCalculatedAt(customCalculatedAt);

        // When - onCreate should not override existing calculatedAt
        metrics.onCreate();

        // Then
        assertThat(metrics.getCalculatedAt()).isEqualTo(customCalculatedAt);
        assertThat(metrics.getCreatedAt()).isNotNull();
        assertThat(metrics.getUpdatedAt()).isNotNull();
    }
}