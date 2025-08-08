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
class HabitCorrelationTest {

    private UUID userId;
    private UUID habit1Id;
    private UUID habit2Id;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        habit1Id = UUID.randomUUID();
        habit2Id = UUID.randomUUID();
    }

    @Test
    void testHabitCorrelationCreation() {
        // Given
        Double correlationCoefficient = 0.75;
        HabitCorrelation correlation = new HabitCorrelation(userId, habit1Id, habit2Id, correlationCoefficient, HabitCorrelation.CorrelationType.POSITIVE);
        correlation.setConfidenceLevel(0.95);

        // Then
        assertThat(correlation.getUserId()).isEqualTo(userId);
        assertThat(correlation.getHabit1Id()).isEqualTo(habit1Id);
        assertThat(correlation.getHabit2Id()).isEqualTo(habit2Id);
        assertThat(correlation.getCorrelationCoefficient()).isEqualTo(correlationCoefficient);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.POSITIVE);
        assertThat(correlation.getConfidenceLevel()).isEqualTo(0.95);
        assertThat(correlation.getCalculatedAt()).isNotNull();
        assertThat(correlation.getCreatedAt()).isNotNull();
        assertThat(correlation.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDefaultConstructor() {
        // Given
        HabitCorrelation correlation = new HabitCorrelation();

        // Then
        assertThat(correlation.getId()).isNull();
        assertThat(correlation.getUserId()).isNull();
        assertThat(correlation.getHabit1Id()).isNull();
        assertThat(correlation.getHabit2Id()).isNull();
        assertThat(correlation.getCorrelationCoefficient()).isNull();
        assertThat(correlation.getCorrelationType()).isNull();
        assertThat(correlation.getConfidenceLevel()).isNull();
        assertThat(correlation.getCalculatedAt()).isNull();
    }

    @Test
    void testCorrelationTypeEnum() {
        // Test all correlation types
        HabitCorrelation correlation = new HabitCorrelation();

        correlation.setCorrelationType(HabitCorrelation.CorrelationType.POSITIVE);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.POSITIVE);

        correlation.setCorrelationType(HabitCorrelation.CorrelationType.NEGATIVE);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.NEGATIVE);

        correlation.setCorrelationType(HabitCorrelation.CorrelationType.NEUTRAL);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.NEUTRAL);

        correlation.setCorrelationType(HabitCorrelation.CorrelationType.CAUSAL);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.CAUSAL);

        correlation.setCorrelationType(HabitCorrelation.CorrelationType.INVERSE_CAUSAL);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.INVERSE_CAUSAL);
    }

    @Test
    void testPrePersistCallback() {
        // Given
        HabitCorrelation correlation = new HabitCorrelation();
        correlation.setUserId(userId);
        correlation.setHabit1Id(habit1Id);
        correlation.setHabit2Id(habit2Id);

        // When
        correlation.onCreate();

        // Then
        assertThat(correlation.getCreatedAt()).isNotNull();
        assertThat(correlation.getUpdatedAt()).isNotNull();
        assertThat(correlation.getCalculatedAt()).isNotNull();
        assertThat(correlation.getCreatedAt()).isEqualTo(correlation.getUpdatedAt());
    }

    @Test
    void testPreUpdateCallback() {
        // Given
        HabitCorrelation correlation = new HabitCorrelation(userId, habit1Id, habit2Id, 0.5, HabitCorrelation.CorrelationType.POSITIVE);
        LocalDateTime originalUpdatedAt = correlation.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        correlation.onUpdate();

        // Then
        assertThat(correlation.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        HabitCorrelation correlation = new HabitCorrelation();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Double coefficient = -0.65;
        Double confidence = 0.88;

        // When
        correlation.setId(id);
        correlation.setUserId(userId);
        correlation.setHabit1Id(habit1Id);
        correlation.setHabit2Id(habit2Id);
        correlation.setCorrelationCoefficient(coefficient);
        correlation.setCorrelationType(HabitCorrelation.CorrelationType.NEGATIVE);
        correlation.setConfidenceLevel(confidence);
        correlation.setCalculatedAt(now);
        correlation.setCreatedAt(now);
        correlation.setUpdatedAt(now);

        // Then
        assertThat(correlation.getId()).isEqualTo(id);
        assertThat(correlation.getUserId()).isEqualTo(userId);
        assertThat(correlation.getHabit1Id()).isEqualTo(habit1Id);
        assertThat(correlation.getHabit2Id()).isEqualTo(habit2Id);
        assertThat(correlation.getCorrelationCoefficient()).isEqualTo(coefficient);
        assertThat(correlation.getCorrelationType()).isEqualTo(HabitCorrelation.CorrelationType.NEGATIVE);
        assertThat(correlation.getConfidenceLevel()).isEqualTo(confidence);
        assertThat(correlation.getCalculatedAt()).isEqualTo(now);
        assertThat(correlation.getCreatedAt()).isEqualTo(now);
        assertThat(correlation.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testCalculatedAtDefaultBehavior() {
        // Given
        HabitCorrelation correlation = new HabitCorrelation();
        correlation.setUserId(userId);
        correlation.setHabit1Id(habit1Id);
        correlation.setHabit2Id(habit2Id);
        LocalDateTime customCalculatedAt = LocalDateTime.now().minusHours(2);
        correlation.setCalculatedAt(customCalculatedAt);

        // When - onCreate should not override existing calculatedAt
        correlation.onCreate();

        // Then
        assertThat(correlation.getCalculatedAt()).isEqualTo(customCalculatedAt);
        assertThat(correlation.getCreatedAt()).isNotNull();
        assertThat(correlation.getUpdatedAt()).isNotNull();
    }

    @Test
    void testCorrelationCoefficientBoundaries() {
        // Test boundary values for correlation coefficient
        HabitCorrelation correlation = new HabitCorrelation();

        // Test maximum positive correlation
        correlation.setCorrelationCoefficient(1.0);
        assertThat(correlation.getCorrelationCoefficient()).isEqualTo(1.0);

        // Test maximum negative correlation
        correlation.setCorrelationCoefficient(-1.0);
        assertThat(correlation.getCorrelationCoefficient()).isEqualTo(-1.0);

        // Test no correlation
        correlation.setCorrelationCoefficient(0.0);
        assertThat(correlation.getCorrelationCoefficient()).isEqualTo(0.0);
    }
}