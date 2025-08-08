package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.GroupHabitCompletion;
import com.example.backend.service.analytics.CompletionDataAggregator.*;
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
 * Unit test for CompletionDataAggregator service.
 * Tests aggregation and processing of habit completion data.
 */
@SpringBootTest
@SpringJUnitConfig
class CompletionDataAggregatorTest {

    private CompletionDataAggregator aggregator;
    private UUID userId;
    private UUID habitId1;
    private UUID habitId2;
    private UUID groupHabitId;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        aggregator = new CompletionDataAggregator();
        userId = UUID.randomUUID();
        habitId1 = UUID.randomUUID();
        habitId2 = UUID.randomUUID();
        groupHabitId = UUID.randomUUID();
        testStartDate = LocalDate.now().minusDays(14);
        testEndDate = LocalDate.now();
    }

    @Test
    void testAggregateDailyCompletions() {
        // Given
        List<DailyCompletion> completions = createTestDailyCompletions();

        // When
        DailyAggregationResult result = aggregator.aggregateDailyCompletions(completions, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getDailyStats()).isNotEmpty();
        
        // Verify all dates in range are included
        long expectedDays = testStartDate.datesUntil(testEndDate.plusDays(1)).count();
        assertThat(result.getDailyStats()).hasSize((int) expectedDays);
        
        // Verify completion rates are valid
        result.getDailyStats().values().forEach(stats -> {
            assertThat(stats.getCompletionRate()).isBetween(0.0, 1.0);
            assertThat(stats.getCompletedHabits()).isLessThanOrEqualTo(stats.getTotalHabits());
        });
        
        // Verify average completion rate calculation
        double averageRate = result.getAverageCompletionRate();
        assertThat(averageRate).isBetween(0.0, 1.0);
    }

    @Test
    void testAggregateWeeklyCompletions() {
        // Given
        List<DailyCompletion> completions = createTestDailyCompletions();

        // When
        WeeklyAggregationResult result = aggregator.aggregateWeeklyCompletions(completions, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getWeeklyStats()).isNotEmpty();
        
        // Verify weekly stats structure
        result.getWeeklyStats().values().forEach(weeklyStats -> {
            assertThat(weeklyStats.getCompletionRate()).isBetween(0.0, 1.0);
            assertThat(weeklyStats.getDailyRates()).hasSize(7); // All days of week
            
            // Verify daily rates within week
            weeklyStats.getDailyRates().values().forEach(dailyRate -> {
                assertThat(dailyRate).isBetween(0.0, 1.0);
            });
        });
    }

    @Test
    void testAggregateTimePatterns() {
        // Given
        List<DailyCompletion> completions = createTestDailyCompletionsWithTimes();

        // When
        TimePatternResult result = aggregator.aggregateTimePatterns(completions);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHourlyStats()).hasSize(24); // All hours of day
        
        // Verify hourly stats
        result.getHourlyStats().forEach((hour, stats) -> {
            assertThat(hour).isBetween(0, 23);
            assertThat(stats.getCompletionRate()).isBetween(0.0, 1.0);
            assertThat(stats.getCompletedHabits()).isLessThanOrEqualTo(stats.getTotalHabits());
        });
        
        // Verify peak hour detection
        OptionalInt peakHour = result.getPeakHour();
        if (peakHour.isPresent()) {
            assertThat(peakHour.getAsInt()).isBetween(0, 23);
        }
    }

    @Test
    void testAnalyzeStreaks() {
        // Given
        List<DailyCompletion> completions = createConsecutiveCompletions();

        // When
        StreakAnalysisResult result = aggregator.analyzeStreaks(completions, habitId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStreak()).isGreaterThanOrEqualTo(0);
        assertThat(result.getMaxStreak()).isGreaterThanOrEqualTo(result.getCurrentStreak());
        assertThat(result.getAllStreaks()).isNotNull();
        
        // Verify streak periods
        result.getAllStreaks().forEach(streak -> {
            assertThat(streak.getLength()).isGreaterThan(0);
            assertThat(streak.getStartDate()).isNotNull();
            assertThat(streak.getEndDate()).isNotNull();
            assertThat(streak.getEndDate()).isAfterOrEqualTo(streak.getStartDate());
        });
    }

    @Test
    void testAnalyzeStreaksWithGaps() {
        // Given
        List<DailyCompletion> completions = createCompletionsWithGaps();

        // When
        StreakAnalysisResult result = aggregator.analyzeStreaks(completions, habitId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAllStreaks()).hasSizeGreaterThan(1); // Should have multiple streaks due to gaps
        
        // Verify streaks are properly separated
        List<StreakPeriod> streaks = result.getAllStreaks();
        for (int i = 1; i < streaks.size(); i++) {
            StreakPeriod previousStreak = streaks.get(i - 1);
            StreakPeriod currentStreak = streaks.get(i);
            assertThat(currentStreak.getStartDate()).isAfter(previousStreak.getEndDate());
        }
    }

    @Test
    void testAggregateGroupCompletions() {
        // Given
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = createTestGroupCompletions();

        // When
        GroupAggregationResult result = aggregator.aggregateGroupCompletions(habitCompletions, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getDailyStats()).isNotEmpty();
        
        // Verify daily group stats
        result.getDailyStats().values().forEach(dailyStats -> {
            assertThat(dailyStats.getCompletionRate()).isBetween(0.0, 1.0);
            assertThat(dailyStats.getCompletedHabits()).isLessThanOrEqualTo(dailyStats.getTotalHabits());
            assertThat(dailyStats.getHabitParticipation()).isNotNull();
        });
    }

    @Test
    void testAggregateDailyCompletionsWithEmptyData() {
        // Given
        List<DailyCompletion> emptyCompletions = Collections.emptyList();

        // When
        DailyAggregationResult result = aggregator.aggregateDailyCompletions(emptyCompletions, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDailyStats()).isNotEmpty(); // Should still have entries for each date
        
        // All stats should show zero completions
        result.getDailyStats().values().forEach(stats -> {
            assertThat(stats.getTotalHabits()).isEqualTo(0);
            assertThat(stats.getCompletedHabits()).isEqualTo(0);
            assertThat(stats.getCompletionRate()).isEqualTo(0.0);
        });
        
        assertThat(result.getAverageCompletionRate()).isEqualTo(0.0);
    }

    @Test
    void testAnalyzeStreaksWithNoCompletions() {
        // Given
        List<DailyCompletion> emptyCompletions = Collections.emptyList();

        // When
        StreakAnalysisResult result = aggregator.analyzeStreaks(emptyCompletions, habitId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStreak()).isEqualTo(0);
        assertThat(result.getMaxStreak()).isEqualTo(0);
        assertThat(result.getAllStreaks()).isEmpty();
    }

    // Helper methods to create test data

    private List<DailyCompletion> createTestDailyCompletions() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = testStartDate;
        
        while (!currentDate.isAfter(testEndDate)) {
            // Create completions with varying success rates
            if (Math.random() < 0.7) { // 70% completion rate
                DailyCompletion completion = new DailyCompletion(habitId1, userId, currentDate);
                completion.setCompleted(true);
                completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 0)));
                completions.add(completion);
            }
            
            if (Math.random() < 0.5) { // 50% completion rate for second habit
                DailyCompletion completion = new DailyCompletion(habitId2, userId, currentDate);
                completion.setCompleted(true);
                completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(20, 0)));
                completions.add(completion);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createTestDailyCompletionsWithTimes() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = testStartDate;
        
        while (!currentDate.isAfter(testEndDate)) {
            // Create completions at different times of day
            int[] completionHours = {8, 12, 18, 20}; // Morning, noon, evening, night
            
            for (int hour : completionHours) {
                if (Math.random() < 0.6) { // 60% chance of completion at each time
                    DailyCompletion completion = new DailyCompletion(habitId1, userId, currentDate);
                    completion.setCompleted(true);
                    completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(hour, 0)));
                    completions.add(completion);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createConsecutiveCompletions() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = testStartDate;
        
        // Create 10 consecutive days of completions
        for (int i = 0; i < 10; i++) {
            DailyCompletion completion = new DailyCompletion(habitId1, userId, currentDate);
            completion.setCompleted(true);
            completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 0)));
            completions.add(completion);
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createCompletionsWithGaps() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = testStartDate;
        
        // Create first streak (3 days)
        for (int i = 0; i < 3; i++) {
            DailyCompletion completion = new DailyCompletion(habitId1, userId, currentDate);
            completion.setCompleted(true);
            completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 0)));
            completions.add(completion);
            currentDate = currentDate.plusDays(1);
        }
        
        // Gap of 2 days
        currentDate = currentDate.plusDays(2);
        
        // Create second streak (5 days)
        for (int i = 0; i < 5; i++) {
            DailyCompletion completion = new DailyCompletion(habitId1, userId, currentDate);
            completion.setCompleted(true);
            completion.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 0)));
            completions.add(completion);
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private Map<UUID, List<GroupHabitCompletion>> createTestGroupCompletions() {
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = new HashMap<>();
        List<GroupHabitCompletion> completions = new ArrayList<>();
        
        LocalDate currentDate = testStartDate;
        while (!currentDate.isAfter(testEndDate)) {
            if (Math.random() < 0.8) { // 80% completion rate
                GroupHabitCompletion completion = new GroupHabitCompletion(groupHabitId, userId, currentDate, true);
                completions.add(completion);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        habitCompletions.put(groupHabitId, completions);
        return habitCompletions;
    }
}