package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.HabitCorrelation;
import com.example.backend.repository.HabitCorrelationRepository;
import com.example.backend.service.analytics.HabitCorrelationService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HabitCorrelationService.
 */
@SpringBootTest
class HabitCorrelationServiceTest {

    @Mock
    private HabitCorrelationRepository habitCorrelationRepository;

    @Mock
    private AnalyticsDataCollector analyticsDataCollector;

    @InjectMocks
    private HabitCorrelationService habitCorrelationService;

    private UUID userId;
    private UUID habit1Id;
    private UUID habit2Id;
    private UUID habit3Id;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        habit1Id = UUID.randomUUID();
        habit2Id = UUID.randomUUID();
        habit3Id = UUID.randomUUID();
    }

    @Test
    void testCalculateCorrelationCoefficient() {
        // Given
        List<Boolean> habit1Completions = Arrays.asList(true, true, false, true, false, true, true);
        List<Boolean> habit2Completions = Arrays.asList(true, false, false, true, false, true, false);

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(habit1Completions, habit2Completions);

        // Then
        assertThat(correlation).isBetween(-1.0, 1.0);
        // With this data, we expect a moderate positive correlation
        assertThat(correlation).isGreaterThan(0.0);
    }

    @Test
    void testCalculateCorrelationCoefficientPerfectPositive() {
        // Given - identical completion patterns
        List<Boolean> habit1Completions = Arrays.asList(true, true, false, true, false);
        List<Boolean> habit2Completions = Arrays.asList(true, true, false, true, false);

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(habit1Completions, habit2Completions);

        // Then
        assertThat(correlation).isEqualTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCalculateCorrelationCoefficientPerfectNegative() {
        // Given - opposite completion patterns
        List<Boolean> habit1Completions = Arrays.asList(true, true, false, true, false);
        List<Boolean> habit2Completions = Arrays.asList(false, false, true, false, true);

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(habit1Completions, habit2Completions);

        // Then
        assertThat(correlation).isEqualTo(-1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void testCalculateCorrelationCoefficientNoCorrelation() {
        // Given - random patterns with no correlation
        List<Boolean> habit1Completions = Arrays.asList(true, false, true, false, true);
        List<Boolean> habit2Completions = Arrays.asList(true, true, false, false, true);

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(habit1Completions, habit2Completions);

        // Then
        assertThat(correlation).isBetween(-1.0, 1.0);
    }

    @Test
    void testAnalyzeHabitCorrelations() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        Map<UUID, AnalyticsDataCollector.HabitCompletionData> habitData = createMockHabitData();
        when(analyticsDataCollector.collectAllUserHabitsData(userId, startDate, endDate))
            .thenReturn(habitData);

        // When
        List<CorrelationResult> results = habitCorrelationService.analyzeHabitCorrelations(userId, startDate, endDate);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3); // 3 pairs from 3 habits: (1,2), (1,3), (2,3)
        
        results.forEach(result -> {
            assertThat(result.getCorrelationCoefficient()).isBetween(-1.0, 1.0);
            assertThat(result.getConfidenceLevel()).isBetween(0.0, 1.0);
            assertThat(result.getCorrelationType()).isNotNull();
        });
    }

    @Test
    void testStoreCorrelationResults() {
        // Given
        List<CorrelationResult> correlationResults = Arrays.asList(
            new CorrelationResult(habit1Id, habit2Id, 0.75, HabitCorrelation.CorrelationType.POSITIVE, 0.95),
            new CorrelationResult(habit1Id, habit3Id, -0.60, HabitCorrelation.CorrelationType.NEGATIVE, 0.85)
        );

        when(habitCorrelationRepository.existsCorrelationBetweenHabits(eq(userId), any(UUID.class), any(UUID.class)))
            .thenReturn(false);
        when(habitCorrelationRepository.save(any(HabitCorrelation.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        habitCorrelationService.storeCorrelationResults(userId, correlationResults);

        // Then
        verify(habitCorrelationRepository, times(2)).save(any(HabitCorrelation.class));
    }

    @Test
    void testFindPositiveCorrelations() {
        // Given
        List<HabitCorrelation> mockCorrelations = Arrays.asList(
            createMockCorrelation(habit1Id, habit2Id, 0.8, HabitCorrelation.CorrelationType.POSITIVE),
            createMockCorrelation(habit1Id, habit3Id, -0.6, HabitCorrelation.CorrelationType.NEGATIVE),
            createMockCorrelation(habit2Id, habit3Id, 0.7, HabitCorrelation.CorrelationType.POSITIVE)
        );

        when(habitCorrelationRepository.findPositiveCorrelations(userId, 0.5))
            .thenReturn(mockCorrelations.stream()
                .filter(c -> c.getCorrelationCoefficient() >= 0.5)
                .toList());

        // When
        List<HabitCorrelation> positiveCorrelations = habitCorrelationService.findPositiveCorrelations(userId, 0.5);

        // Then
        assertThat(positiveCorrelations).hasSize(2);
        assertThat(positiveCorrelations).allMatch(c -> c.getCorrelationCoefficient() >= 0.5);
    }

    @Test
    void testFindNegativeCorrelations() {
        // Given
        List<HabitCorrelation> mockCorrelations = Arrays.asList(
            createMockCorrelation(habit1Id, habit3Id, -0.8, HabitCorrelation.CorrelationType.NEGATIVE),
            createMockCorrelation(habit2Id, habit3Id, -0.6, HabitCorrelation.CorrelationType.NEGATIVE)
        );

        when(habitCorrelationRepository.findNegativeCorrelations(userId, -0.5))
            .thenReturn(mockCorrelations);

        // When
        List<HabitCorrelation> negativeCorrelations = habitCorrelationService.findNegativeCorrelations(userId, -0.5);

        // Then
        assertThat(negativeCorrelations).hasSize(2);
        assertThat(negativeCorrelations).allMatch(c -> c.getCorrelationCoefficient() <= -0.5);
    }

    @Test
    void testGetCorrelationMatrix() {
        // Given
        List<UUID> habitIds = Arrays.asList(habit1Id, habit2Id, habit3Id);
        List<HabitCorrelation> mockCorrelations = Arrays.asList(
            createMockCorrelation(habit1Id, habit2Id, 0.8, HabitCorrelation.CorrelationType.POSITIVE),
            createMockCorrelation(habit1Id, habit3Id, -0.6, HabitCorrelation.CorrelationType.NEGATIVE),
            createMockCorrelation(habit2Id, habit3Id, 0.3, HabitCorrelation.CorrelationType.POSITIVE)
        );

        when(habitCorrelationRepository.getCorrelationMatrixForUser(userId))
            .thenReturn(mockCorrelations);

        // When
        CorrelationMatrix matrix = habitCorrelationService.getCorrelationMatrix(userId, habitIds);

        // Then
        assertThat(matrix).isNotNull();
        assertThat(matrix.getHabitIds()).containsExactlyElementsOf(habitIds);
        assertThat(matrix.getCorrelationValues()).isNotEmpty();
        
        // Verify diagonal values are 1.0 (habit correlates perfectly with itself)
        for (int i = 0; i < habitIds.size(); i++) {
            assertThat(matrix.getCorrelationValue(habitIds.get(i), habitIds.get(i))).isEqualTo(1.0);
        }
    }

    @Test
    void testCalculateCorrelationCoefficientWithEmptyLists() {
        // Given
        List<Boolean> emptyList1 = Collections.emptyList();
        List<Boolean> emptyList2 = Collections.emptyList();

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(emptyList1, emptyList2);

        // Then
        assertThat(correlation).isEqualTo(0.0);
    }

    @Test
    void testCalculateCorrelationCoefficientWithConstantValues() {
        // Given - all true values (no variance)
        List<Boolean> constantList1 = Arrays.asList(true, true, true, true, true);
        List<Boolean> constantList2 = Arrays.asList(true, true, true, true, true);

        // When
        double correlation = habitCorrelationService.calculateCorrelationCoefficient(constantList1, constantList2);

        // Then
        assertThat(correlation).isEqualTo(0.0); // No variance means no correlation can be calculated
    }

    // Helper methods

    private Map<UUID, AnalyticsDataCollector.HabitCompletionData> createMockHabitData() {
        Map<UUID, AnalyticsDataCollector.HabitCompletionData> habitData = new HashMap<>();
        
        // Create mock completion data for each habit
        habitData.put(habit1Id, createMockHabitCompletionData(habit1Id, Arrays.asList(true, true, false, true, false, true, true)));
        habitData.put(habit2Id, createMockHabitCompletionData(habit2Id, Arrays.asList(true, false, false, true, false, true, false)));
        habitData.put(habit3Id, createMockHabitCompletionData(habit3Id, Arrays.asList(false, true, true, false, true, false, true)));
        
        return habitData;
    }

    private AnalyticsDataCollector.HabitCompletionData createMockHabitCompletionData(UUID habitId, List<Boolean> completions) {
        LocalDate startDate = LocalDate.now().minusDays(completions.size() - 1);
        LocalDate endDate = LocalDate.now();
        
        AnalyticsDataCollector.HabitCompletionData data = new AnalyticsDataCollector.HabitCompletionData(habitId, startDate, endDate);
        
        List<DailyCompletion> dailyCompletions = new ArrayList<>();
        for (int i = 0; i < completions.size(); i++) {
            DailyCompletion completion = new DailyCompletion(habitId, userId, startDate.plusDays(i));
            completion.setCompleted(completions.get(i));
            dailyCompletions.add(completion);
        }
        
        data.setCompletions(dailyCompletions);
        return data;
    }

    private HabitCorrelation createMockCorrelation(UUID habit1Id, UUID habit2Id, double coefficient, HabitCorrelation.CorrelationType type) {
        HabitCorrelation correlation = new HabitCorrelation(userId, habit1Id, habit2Id, coefficient, type);
        correlation.setConfidenceLevel(0.9);
        return correlation;
    }
}