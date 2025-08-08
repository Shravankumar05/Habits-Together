package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.service.analytics.OptimalTimingAnalyzer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OptimalTimingAnalyzer service.
 */
@SpringBootTest
class OptimalTimingAnalyzerTest {

    @Mock
    private AnalyticsDataCollector analyticsDataCollector;

    @InjectMocks
    private OptimalTimingAnalyzer optimalTimingAnalyzer;

    private UUID userId;
    private UUID habitId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
    }

    @Test
    void testAnalyzeOptimalTiming() {
        // Given
        List<DailyCompletion> mockCompletions = createMockCompletionsWithTiming();
        AnalyticsDataCollector.HabitCompletionData mockData = createMockHabitCompletionData(mockCompletions);
        
        when(analyticsDataCollector.collectHabitCompletionData(eq(habitId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);

        // When
        OptimalTimingResult result = optimalTimingAnalyzer.analyzeOptimalTiming(userId, habitId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHabitId()).isEqualTo(habitId);
        assertThat(result.getOptimalStartTime()).isNotNull();
        assertThat(result.getOptimalEndTime()).isNotNull();
        assertThat(result.getHourlyStats()).isNotEmpty();
        assertThat(result.getDayOfWeekStats()).isNotEmpty();
        
        // Verify hourly stats
        assertThat(result.getHourlyStats()).hasSize(24);
        result.getHourlyStats().values().forEach(stats -> {
            assertThat(stats.getSuccessRate()).isBetween(0.0, 1.0);
            assertThat(stats.getSuccessfulAttempts()).isLessThanOrEqualTo(stats.getTotalAttempts());
        });
        
        // Verify day-of-week stats
        assertThat(result.getDayOfWeekStats()).hasSize(7);
        result.getDayOfWeekStats().values().forEach(stats -> {
            assertThat(stats.getSuccessRate()).isBetween(0.0, 1.0);
            assertThat(stats.getSuccessfulAttempts()).isLessThanOrEqualTo(stats.getTotalAttempts());
        });
    }

    @Test
    void testAnalyzeOptimalTimingWithNoData() {
        // Given
        AnalyticsDataCollector.HabitCompletionData emptyData = createMockHabitCompletionData(Collections.emptyList());
        
        when(analyticsDataCollector.collectHabitCompletionData(eq(habitId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(emptyData);

        // When
        OptimalTimingResult result = optimalTimingAnalyzer.analyzeOptimalTiming(userId, habitId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHabitId()).isEqualTo(habitId);
        assertThat(result.getOptimalStartTime()).isNull();
        assertThat(result.getOptimalEndTime()).isNull();
        assertThat(result.getHourlyStats()).isEmpty();
        assertThat(result.getDayOfWeekStats()).isEmpty();
    }

    @Test
    void testPredictSuccessForTiming() {
        // Given
        List<DailyCompletion> mockCompletions = createMockCompletionsWithTiming();
        AnalyticsDataCollector.HabitCompletionData mockData = createMockHabitCompletionData(mockCompletions);
        
        when(analyticsDataCollector.collectHabitCompletionData(eq(habitId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);

        LocalTime proposedTime = LocalTime.of(8, 30);
        DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

        // When
        SuccessPredictionResult result = optimalTimingAnalyzer.predictSuccessForTiming(
            userId, habitId, proposedTime, dayOfWeek, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProposedTime()).isEqualTo(proposedTime);
        assertThat(result.getDayOfWeek()).isEqualTo(dayOfWeek);
        assertThat(result.getPredictedSuccessRate()).isBetween(0.0, 1.0);
        assertThat(result.getConfidence()).isBetween(0.0, 1.0);
        assertThat(result.getHourlySuccessRate()).isBetween(0.0, 1.0);
        assertThat(result.getDaySuccessRate()).isBetween(0.0, 1.0);
    }

    @Test
    void testFindBestTimeWindows() {
        // Given
        List<DailyCompletion> mockCompletions = createMockCompletionsWithTiming();
        AnalyticsDataCollector.HabitCompletionData mockData = createMockHabitCompletionData(mockCompletions);
        
        when(analyticsDataCollector.collectHabitCompletionData(eq(habitId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);

        int windowCount = 3;

        // When
        List<TimeWindow> result = optimalTimingAnalyzer.findBestTimeWindows(userId, habitId, startDate, endDate, windowCount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSizeLessThanOrEqualTo(windowCount);
        
        result.forEach(window -> {
            assertThat(window.getStartTime()).isNotNull();
            assertThat(window.getEndTime()).isNotNull();
            assertThat(window.getSuccessRate()).isBetween(0.0, 1.0);
            assertThat(window.getSampleSize()).isGreaterThanOrEqualTo(3); // Minimum sample size
            assertThat(window.getEndTime()).isAfter(window.getStartTime());
        });
        
        // Verify windows are sorted by success rate (descending)
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i).getSuccessRate()).isLessThanOrEqualTo(result.get(i - 1).getSuccessRate());
        }
    }

    @Test
    void testAnalyzeWeeklyTimingPattern() {
        // Given
        List<DailyCompletion> mockCompletions = createMockCompletionsWithWeeklyPattern();
        AnalyticsDataCollector.HabitCompletionData mockData = createMockHabitCompletionData(mockCompletions);
        
        when(analyticsDataCollector.collectHabitCompletionData(eq(habitId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockData);

        // When
        WeeklyTimingPattern result = optimalTimingAnalyzer.analyzeWeeklyTimingPattern(userId, habitId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHabitId()).isEqualTo(habitId);
        assertThat(result.getWeeklyPattern()).hasSize(7); // All days of week
        
        // Verify each day has timing data
        for (DayOfWeek day : DayOfWeek.values()) {
            Map<Integer, TimingStats> dayStats = result.getWeeklyPattern().get(day);
            assertThat(dayStats).isNotNull();
            
            // Check if we can get best time for each day
            Optional<TimeWindow> bestTime = result.getBestTimeForDay(day);
            // bestTime may be empty if no sufficient data for that day
        }
    }

    @Test
    void testWeeklyTimingPatternGetBestTimeForDay() {
        // Given
        Map<DayOfWeek, Map<Integer, TimingStats>> weeklyPattern = new HashMap<>();
        
        // Create mock data for Monday with best time at 8 AM
        Map<Integer, TimingStats> mondayStats = new HashMap<>();
        mondayStats.put(8, new TimingStats(10, 9, 0.9)); // High success rate
        mondayStats.put(14, new TimingStats(5, 3, 0.6)); // Lower success rate
        weeklyPattern.put(DayOfWeek.MONDAY, mondayStats);
        
        // Create mock data for Tuesday with insufficient data
        Map<Integer, TimingStats> tuesdayStats = new HashMap<>();
        tuesdayStats.put(10, new TimingStats(1, 1, 1.0)); // Too few attempts
        weeklyPattern.put(DayOfWeek.TUESDAY, tuesdayStats);
        
        WeeklyTimingPattern pattern = new WeeklyTimingPattern(habitId, weeklyPattern);

        // When & Then
        Optional<TimeWindow> mondayBest = pattern.getBestTimeForDay(DayOfWeek.MONDAY);
        assertThat(mondayBest).isPresent();
        assertThat(mondayBest.get().getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(mondayBest.get().getSuccessRate()).isEqualTo(0.9);
        
        Optional<TimeWindow> tuesdayBest = pattern.getBestTimeForDay(DayOfWeek.TUESDAY);
        assertThat(tuesdayBest).isEmpty(); // Insufficient data
        
        Optional<TimeWindow> wednesdayBest = pattern.getBestTimeForDay(DayOfWeek.WEDNESDAY);
        assertThat(wednesdayBest).isEmpty(); // No data
    }

    @Test
    void testTimingStatsCalculation() {
        // Given
        int totalAttempts = 10;
        int successfulAttempts = 7;
        double expectedSuccessRate = 0.7;

        // When
        TimingStats stats = new TimingStats(totalAttempts, successfulAttempts, expectedSuccessRate);

        // Then
        assertThat(stats.getTotalAttempts()).isEqualTo(totalAttempts);
        assertThat(stats.getSuccessfulAttempts()).isEqualTo(successfulAttempts);
        assertThat(stats.getSuccessRate()).isEqualTo(expectedSuccessRate);
    }

    @Test
    void testTimeWindowCreation() {
        // Given
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(10, 0);
        double successRate = 0.85;
        int sampleSize = 20;

        // When
        TimeWindow window = new TimeWindow(startTime, endTime, successRate, sampleSize);

        // Then
        assertThat(window.getStartTime()).isEqualTo(startTime);
        assertThat(window.getEndTime()).isEqualTo(endTime);
        assertThat(window.getSuccessRate()).isEqualTo(successRate);
        assertThat(window.getSampleSize()).isEqualTo(sampleSize);
    }

    // Helper methods

    private List<DailyCompletion> createMockCompletionsWithTiming() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate.minusDays(1))) {
            // Create completions at different times with varying success rates
            // Morning completions (8-10 AM) - high success rate
            if (Math.random() < 0.8) {
                DailyCompletion completion = new DailyCompletion(habitId, userId, currentDate);
                completion.setCompleted(true);
                completion.setCompletedAt(LocalDateTime.of(currentDate, LocalTime.of(8 + (int)(Math.random() * 2), 0)));
                completions.add(completion);
            }
            
            // Afternoon completions (2-4 PM) - medium success rate
            if (Math.random() < 0.6) {
                DailyCompletion completion = new DailyCompletion(habitId, userId, currentDate);
                completion.setCompleted(Math.random() < 0.7);
                completion.setCompletedAt(LocalDateTime.of(currentDate, LocalTime.of(14 + (int)(Math.random() * 2), 0)));
                completions.add(completion);
            }
            
            // Evening completions (8-10 PM) - lower success rate
            if (Math.random() < 0.4) {
                DailyCompletion completion = new DailyCompletion(habitId, userId, currentDate);
                completion.setCompleted(Math.random() < 0.5);
                completion.setCompletedAt(LocalDateTime.of(currentDate, LocalTime.of(20 + (int)(Math.random() * 2), 0)));
                completions.add(completion);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private List<DailyCompletion> createMockCompletionsWithWeeklyPattern() {
        List<DailyCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate.minusDays(1))) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // Different patterns for different days
            if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY) {
                // Weekdays - morning preference
                if (Math.random() < 0.7) {
                    DailyCompletion completion = new DailyCompletion(habitId, userId, currentDate);
                    completion.setCompleted(true);
                    completion.setCompletedAt(LocalDateTime.of(currentDate, LocalTime.of(8, 0)));
                    completions.add(completion);
                }
            } else if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                // Weekends - afternoon preference
                if (Math.random() < 0.6) {
                    DailyCompletion completion = new DailyCompletion(habitId, userId, currentDate);
                    completion.setCompleted(true);
                    completion.setCompletedAt(LocalDateTime.of(currentDate, LocalTime.of(14, 0)));
                    completions.add(completion);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }

    private AnalyticsDataCollector.HabitCompletionData createMockHabitCompletionData(List<DailyCompletion> completions) {
        AnalyticsDataCollector.HabitCompletionData data = 
            new AnalyticsDataCollector.HabitCompletionData(habitId, startDate, endDate);
        data.setCompletions(completions);
        return data;
    }
}