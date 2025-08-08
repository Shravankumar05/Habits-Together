package com.example.backend.repository;

import com.example.backend.entity.HabitAnalytics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
class HabitAnalyticsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HabitAnalyticsRepository habitAnalyticsRepository;

    private UUID userId1;
    private UUID userId2;
    private UUID habitId1;
    private UUID habitId2;
    private HabitAnalytics analytics1;
    private HabitAnalytics analytics2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        habitId1 = UUID.randomUUID();
        habitId2 = UUID.randomUUID();

        // Create test analytics
        analytics1 = new HabitAnalytics(userId1, habitId1);
        analytics1.setSuccessRate(0.85);
        analytics1.setConsistencyScore(0.75);
        analytics1.setOptimalTimeStart(LocalTime.of(8, 0));
        analytics1.setOptimalTimeEnd(LocalTime.of(9, 0));
        analytics1.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        analytics1.setHabitStrength(0.65);

        analytics2 = new HabitAnalytics(userId1, habitId2);
        analytics2.setSuccessRate(0.95);
        analytics2.setConsistencyScore(0.90);
        analytics2.setFormationStage(HabitAnalytics.FormationStage.STABILITY);
        analytics2.setHabitStrength(0.85);
    }

    @Test
    void testSaveAndFindById() {
        // When
        HabitAnalytics saved = habitAnalyticsRepository.save(analytics1);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        Optional<HabitAnalytics> found = habitAnalyticsRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId1);
        assertThat(found.get().getHabitId()).isEqualTo(habitId1);
        assertThat(found.get().getSuccessRate()).isEqualTo(0.85);
    }

    @Test
    void testFindByUserId() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        
        HabitAnalytics analytics3 = new HabitAnalytics(userId2, habitId1);
        habitAnalyticsRepository.save(analytics3);
        entityManager.flush();

        // When
        List<HabitAnalytics> userAnalytics = habitAnalyticsRepository.findByUserId(userId1);

        // Then
        assertThat(userAnalytics).hasSize(2);
        assertThat(userAnalytics).extracting(HabitAnalytics::getUserId).containsOnly(userId1);
        assertThat(userAnalytics).extracting(HabitAnalytics::getHabitId).containsExactlyInAnyOrder(habitId1, habitId2);
    }

    @Test
    void testFindByUserIdAndHabitId() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        entityManager.flush();

        // When
        Optional<HabitAnalytics> found = habitAnalyticsRepository.findByUserIdAndHabitId(userId1, habitId1);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId1);
        assertThat(found.get().getHabitId()).isEqualTo(habitId1);
        assertThat(found.get().getSuccessRate()).isEqualTo(0.85);
    }

    @Test
    void testFindByUserIdAndFormationStage() {
        // Given
        habitAnalyticsRepository.save(analytics1); // LEARNING stage
        habitAnalyticsRepository.save(analytics2); // STABILITY stage
        
        HabitAnalytics analytics3 = new HabitAnalytics(userId1, UUID.randomUUID());
        analytics3.setFormationStage(HabitAnalytics.FormationStage.LEARNING);
        habitAnalyticsRepository.save(analytics3);
        entityManager.flush();

        // When
        List<HabitAnalytics> learningStage = habitAnalyticsRepository.findByUserIdAndFormationStage(userId1, HabitAnalytics.FormationStage.LEARNING);

        // Then
        assertThat(learningStage).hasSize(2);
        assertThat(learningStage).extracting(HabitAnalytics::getFormationStage)
                .containsOnly(HabitAnalytics.FormationStage.LEARNING);
    }

    @Test
    void testFindAnalyticsNeedingUpdate() {
        // Given
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        
        analytics1.setLastAnalyzed(LocalDateTime.now().minusHours(2)); // Needs update
        analytics2.setLastAnalyzed(LocalDateTime.now().minusMinutes(30)); // Recent
        
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        entityManager.flush();

        // When
        List<HabitAnalytics> needingUpdate = habitAnalyticsRepository.findAnalyticsNeedingUpdate(cutoffTime);

        // Then
        assertThat(needingUpdate).hasSize(1);
        assertThat(needingUpdate.get(0).getHabitId()).isEqualTo(habitId1);
    }

    @Test
    void testFindByUserIdAndSuccessRateGreaterThanEqual() {
        // Given
        habitAnalyticsRepository.save(analytics1); // 0.85 success rate
        habitAnalyticsRepository.save(analytics2); // 0.95 success rate
        entityManager.flush();

        // When
        List<HabitAnalytics> highSuccessRate = habitAnalyticsRepository.findByUserIdAndSuccessRateGreaterThanEqual(userId1, 0.90);

        // Then
        assertThat(highSuccessRate).hasSize(1);
        assertThat(highSuccessRate.get(0).getSuccessRate()).isEqualTo(0.95);
        assertThat(highSuccessRate.get(0).getHabitId()).isEqualTo(habitId2);
    }

    @Test
    void testFindByUserIdAndConsistencyScoreGreaterThanEqual() {
        // Given
        habitAnalyticsRepository.save(analytics1); // 0.75 consistency
        habitAnalyticsRepository.save(analytics2); // 0.90 consistency
        entityManager.flush();

        // When
        List<HabitAnalytics> highConsistency = habitAnalyticsRepository.findByUserIdAndConsistencyScoreGreaterThanEqual(userId1, 0.80);

        // Then
        assertThat(highConsistency).hasSize(1);
        assertThat(highConsistency.get(0).getConsistencyScore()).isEqualTo(0.90);
        assertThat(highConsistency.get(0).getHabitId()).isEqualTo(habitId2);
    }

    @Test
    void testExistsByUserIdAndHabitId() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        entityManager.flush();

        // When & Then
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId1, habitId1)).isTrue();
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId1, habitId2)).isFalse();
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId2, habitId1)).isFalse();
    }

    @Test
    void testCountByUserId() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        
        HabitAnalytics analytics3 = new HabitAnalytics(userId2, habitId1);
        habitAnalyticsRepository.save(analytics3);
        entityManager.flush();

        // When & Then
        assertThat(habitAnalyticsRepository.countByUserId(userId1)).isEqualTo(2);
        assertThat(habitAnalyticsRepository.countByUserId(userId2)).isEqualTo(1);
    }

    @Test
    void testDeleteByHabitId() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        
        HabitAnalytics analytics3 = new HabitAnalytics(userId2, habitId1);
        habitAnalyticsRepository.save(analytics3);
        entityManager.flush();

        // When
        habitAnalyticsRepository.deleteByHabitId(habitId1);
        entityManager.flush();

        // Then
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId1, habitId1)).isFalse();
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId2, habitId1)).isFalse();
        assertThat(habitAnalyticsRepository.existsByUserIdAndHabitId(userId1, habitId2)).isTrue(); // Should still exist
    }

    @Test
    void testFindAll() {
        // Given
        habitAnalyticsRepository.save(analytics1);
        habitAnalyticsRepository.save(analytics2);
        entityManager.flush();

        // When
        List<HabitAnalytics> all = habitAnalyticsRepository.findAll();

        // Then
        assertThat(all).hasSize(2);
    }

    @Test
    void testUpdate() {
        // Given
        HabitAnalytics saved = habitAnalyticsRepository.save(analytics1);
        entityManager.flush();
        entityManager.clear();

        // When
        saved.setSuccessRate(0.95);
        saved.setFormationStage(HabitAnalytics.FormationStage.MASTERY);
        HabitAnalytics updated = habitAnalyticsRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<HabitAnalytics> found = habitAnalyticsRepository.findById(updated.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSuccessRate()).isEqualTo(0.95);
        assertThat(found.get().getFormationStage()).isEqualTo(HabitAnalytics.FormationStage.MASTERY);
    }
}