package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.service.analytics.HabitAnalyticsEngine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HabitAnalyticsEngine.
 * Tests calculation methods with known test datasets.
 */
@SpringBootTest
@SpringJUnitConfig
class HabitAnalyticsEngineTest {

    private HabitAnalyticsEngine analyticsEngine;
    private UUID userId;
    private UUID habitId;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        analyticsEngine = new HabitAnalyticsEngine();
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
        testStartDate = LocalDate.now().minusDays(30);
        testEndDate = LocalDate.now();
    }

    @Test
    void testCalculateSuccessRateWithPerfectCompletion() {
        // Given - 100% completion rate
        List<DailyCompletion> completions = createPerfectCompletions(testStartDate, testEndDate);

        // When
        double successRate = analyticsEngine.calculateSuccessRate(completions, testStartDate, testEndDate);

        // Then
        assertThat(successRate).isEqualTo(1.0);
    }

    @Test
    void testCalculateSuccessRateWithPartialCompletion() {
        // Given - 70% completion rate
        List<DailyCompletion> completions = createPartialCompletions(testStartDate, testEndDate, 0.7);

        // When
        double successRate = analyticsEngine.calculateSuccessRate(completions, testStartDate, testEndDate);

        // Then
        assertThat(successRate).isBetween(0.65, 0.75); // Allow for some variance due to randomization
    }

    @Test
    void testCalculateSuccessRateWithNoCompletions() {
        // Given - empty completions
        List<DailyCompletion> completions = Collections.emptyList();

        // When
        double successRate = analyticsEngine.calculateSuccessRate(completions, testStartDate, testEndDate);

        // Then
        assertThat(successRate).isEqualTo(0.0);
    }

    @Test
    void testCalculateConsistencyScoreWithConsistentPattern() {
        // Given - very consistent completions (same pattern each week)
        List<DailyCompletion> completions = createConsistentWeeklyPattern(testStartDate, testEndDate);

        // When
        double consistencyScore = analyticsEngine.calculateConsistencyScore(completions, testStartDate, testEndDate);

        // Then
        assertThat(consistencyScore).isGreaterThan(0.7); // High consistency
    }

    @Test
    void testCalculateConsistencyScoreWithInconsistentPattern() {
        // Given - very inconsistent completions
        List<DailyCompletion> completions = createInconsistentPattern(testStartDate, testEndDate);

        // When
        double consistencyScore = analyticsEngine.calculateConsistencyScore(completions, testStartDate, testEndDate);

        // Then
        assertThat(consistencyScore).isLessThan(0.5); // Low consistency
    }

    @Test
    void testDetectCompletionTrendsImproving() {
        // Given - improving trend (low start, high end)
        List<DailyCompletion> completions = createImprovingTrend(testStartDate, testEndDate);

        // When
        CompletionTrendAnalysis analysis = analyticsEngine.detectCompletionTrends(completions, testStartDate, testEndDate);

        // Then
        assertThat(analysis.getOverallTrend()).isEqualTo(TrendDirection.IMPROVING);
        assertThat(analysis.getTrendStrength()).isGreaterThan(0.0);
        assertThat(analysis.getDayOfWeekPatterns()).hasSize(7);
    }

    @Test
    void testDetectCompletionTrendsStable() {
        // Given - stable trend
        List<DailyCompletion> completions = createStableTrend(testStartDate, testEndDate);

        // When
        CompletionTrendAnalysis analysis = analyticsEngine.detectCompletionTrends(completions, testStartDate, testEndDate);

        // Then
        assertThat(analysis.getOverallTrend()).isEqualTo(TrendDirection.STABLE);
        assertThat(analysis.getDayOfWeekPatterns()).isNotEmpty();
    }

    @Test
    void testAnalyzeHabitFormationEarlyStage() {
        // Given - new habit (less than a week)
        LocalDate habitStartDate = LocalDate.now().minusDays(5);
        List<DailyCompletion> completions = createEarlyStageCompletions(habitStartDate);

        // When
        HabitFormationAnalysis analysis = analyticsEngine.analyzeHabitFormation(completions, habitStartDate);

        // Then
        assertThat(analysis.getStage()).isEqualTo(FormationStage.INITIATION);
        assertThat(analysis.getProgress()).isLessThan(0.3);
        assertThat(analysis.getCurrentStreak()).isLessThan(7);
    }

    @Test
    void testAnalyzeHabitFormationMasteryStage() {
        // Given - well-established habit (90+ days, high success rate)
        LocalDate habitStartDate = LocalDate.now().minusDays(100);
        List<DailyCompletion> completions = createMasteryStageCompletions(habitStartDate);

        // When
        HabitFormationAnalysis analysis = analyticsEngine.analyzeHabitFormation(completions, habitStartDate);

        // Then
        assertThat(analysis.getStage()).isEqualTo(FormationStage.MASTERY);
        assertThat(analysis.getProgress()).isGreaterThan(0.8);
        assertThat(analysis.getMilestones()).isNotEmpty();
        
        // Should have reached major milestones
        List<String> milestoneTypes = analysis.getMilestones().stream()
            .map(FormationMilestone::getType)
            .toList();
        assertThat(milestoneTypes).contains("HABIT_FORMED");
    }

    @Test
    void testRecognizePatternsWeeklyPattern() {
        // Given - strong weekly pattern (weekdays vs weekends)
        List<DailyCompletion> completions = createWeeklyPattern(testStartDate, testEndDate);

        // When
        PatternRecognitionResult result = analyticsEngine.recognizePatterns(completions, testStartDate, testEndDate);

        // Then
        assertThat(result.getPatterns()).isNotEmpty();
        assertThat(result.getPatternConfidence()).isNotEmpty();
        
        // Should detect weekly pattern
        boolean hasWeeklyPattern = result.getPatterns().stream()
            .anyMatch(pattern -> pattern.getType().equals("WEEKLY_CYCLE"));
        assertThat(hasWeeklyPattern).isTrue();
    }

    @Test
    void testRecognizePatternsWithNoData() {
        // Given - no completions
        List<DailyCompletion> completions = Collections.emptyList();

        // When
        PatternRecognitionResult result = analyticsEngine.recognizePatterns(completions, testStartDate, testEndDate);

        // Then
        assertThat(result.getPatterns()).isEmpty();
        assertThat(result.getPatternConfidence()).isEmpty();
    }

    @Test
    void testCalculateSuccessRateEdgeCases() {
        // Test with single day period
        LocalDate singleDay = LocalDate.now();
        List<DailyCompletion> singleCompletion = Arrays.asList(
            createCompletion(singleDay, true)
        );

        double rate = analyticsEngine.calculateSuccessRate(singleCompletion, singleDay, singleDay);
        assertThat(rate).isEqualTo(1.0);

        // Test with future dates
        LocalDate futureStart = LocalDate.now().plusDays(1);
        LocalDate futureEnd = LocalDate.now().plusDays(5);
        double futureRate = analyticsEngine.calculateSuccessRate(Collections.emptyList(), futureStart, futureEnd);
        assertThat(futureRate).isEqualTo(0.0);
    }

    @Test
    void testConsistencyScoreWithShortPeriod() {
        // Given - only a few days of data
        LocalDate shortStart = LocalDate.now().minusDays(3);
        LocalDate shortEnd = LocalDate.now();
        List<DailyCompletion> shortCompletions = createPerfectCompletions(shortStart, shortEnd);

        // When
        double consistencyScore = analyticsEngine.calculateConsistencyScore(shortCompletions, shortStart, shortEnd);

        // Then
        assertThat(consistencyScore).isBetween(0.0, 1.0);
    }

    // Helper methods to create test data

    private List<DailyCompletion> createPerfectCompletions(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            completions.add(createCompletion(currentDate, true));
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createPartialCompletions(LocalDate startDate, LocalDate endDate, double completionRate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            if (Math.random() < completionRate) {
                completions.add(createCompletion(currentDate, true));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createConsistentWeeklyPattern(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // Complete on weekdays, skip weekends
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            boolean shouldComplete = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
            
            if (shouldComplete) {
                completions.add(createCompletion(currentDate, true));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createInconsistentPattern(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        // Create very random pattern with high variance
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        while (!currentDate.isAfter(endDate)) {
            if (random.nextDouble() < 0.5) { // 50% base rate with high randomness
                completions.add(createCompletion(currentDate, true));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createImprovingTrend(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long dayIndex = 0;
        
        while (!currentDate.isAfter(endDate)) {
            // Increasing probability of completion over time
            double completionProbability = 0.2 + (0.6 * dayIndex / totalDays);
            
            if (Math.random() < completionProbability) {
                completions.add(createCompletion(currentDate, true));
            }
            
            currentDate = currentDate.plusDays(1);
            dayIndex++;
        }
        
        return completions;
    }

    private List<DailyCompletion> createStableTrend(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // Stable 70% completion rate
            if (Math.random() < 0.7) {
                completions.add(createCompletion(currentDate, true));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createEarlyStageCompletions(LocalDate habitStartDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = habitStartDate;
        LocalDate endDate = LocalDate.now();
        
        // Early stage: some completions but not consistent
        while (!currentDate.isAfter(endDate)) {
            if (Math.random() < 0.4) { // 40% completion rate
                completions.add(createCompletion(currentDate, true));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createMasteryStageCompletions(LocalDate habitStartDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = habitStartDate;
        LocalDate endDate = LocalDate.now();
        
        // Mastery stage: very high completion rate with long streaks
        int consecutiveDays = 0;
        while (!currentDate.isAfter(endDate)) {
            boolean shouldComplete = Math.random() < 0.9; // 90% completion rate
            
            if (shouldComplete) {
                completions.add(createCompletion(currentDate, true));
                consecutiveDays++;
            } else {
                consecutiveDays = 0;
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createWeeklyPattern(LocalDate startDate, LocalDate endDate) {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // Strong weekday pattern: 90% on weekdays, 30% on weekends
            double completionProbability = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? 0.3 : 0.9;
            
            if (Math.random() < completionProbability) {
                completions.add(createCompletion(currentDate, true));
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private DailyCompletion createCompletion(LocalDate date, boolean completed) {
        DailyCompletion completion = new DailyCompletion(habitId, userId, date);
        completion.setCompleted(completed);
        completion.setCompletedAt(LocalDateTime.of(date, java.time.LocalTime.of(8, 0)));
        return completion;
    }
}