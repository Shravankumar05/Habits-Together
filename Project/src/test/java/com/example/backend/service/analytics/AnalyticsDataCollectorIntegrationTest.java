package com.example.backend.service.analytics;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.analytics.AnalyticsDataCollector.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AnalyticsDataCollector service.
 * Tests data collection from existing habit and group completion tables.
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
class AnalyticsDataCollectorIntegrationTest {

    @Autowired
    private AnalyticsDataCollector analyticsDataCollector;

    @Autowired
    private DailyCompletionRepository dailyCompletionRepository;

    @Autowired
    private GroupHabitCompletionRepository groupHabitCompletionRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private GroupHabitRepository groupHabitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private UUID userId;
    private UUID habitId1;
    private UUID habitId2;
    private UUID groupId;
    private UUID groupHabitId;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        habitId1 = UUID.randomUUID();
        habitId2 = UUID.randomUUID();
        groupId = UUID.randomUUID();
        groupHabitId = UUID.randomUUID();
        testStartDate = LocalDate.now().minusDays(7);
        testEndDate = LocalDate.now();

        // Create test user
        User testUser = new User(userId, "test@example.com", "Test User");
        userRepository.save(testUser);

        // Create test habits
        Habit habit1 = new Habit();
        habit1.setId(habitId1);
        habit1.setUserId(userId);
        habit1.setName("Morning Exercise");
        habit1.setDescription("Daily morning workout");
        habitRepository.save(habit1);

        Habit habit2 = new Habit();
        habit2.setId(habitId2);
        habit2.setUserId(userId);
        habit2.setName("Reading");
        habit2.setDescription("Daily reading habit");
        habitRepository.save(habit2);

        // Create test group
        Group testGroup = new Group();
        testGroup.setId(groupId);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test group for analytics");
        groupRepository.save(testGroup);

        // Create test group habit
        GroupHabit groupHabit = new GroupHabit();
        groupHabit.setId(groupHabitId);
        groupHabit.setGroupId(groupId);
        groupHabit.setName("Group Exercise");
        groupHabit.setDescription("Group exercise habit");
        groupHabitRepository.save(groupHabit);

        // Create test completion data
        createTestCompletionData();
    }

    @Test
    void testCollectUserCompletionData() {
        // When
        UserCompletionData result = analyticsDataCollector.collectUserCompletionData(userId, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getDailyCompletions()).isNotEmpty();
        assertThat(result.getGroupCompletions()).isNotEmpty();
        assertThat(result.getUserHabits()).hasSize(2);
        
        // Verify we collected completions for both habits
        List<UUID> habitIds = result.getDailyCompletions().stream()
            .map(DailyCompletion::getHabitId)
            .distinct()
            .toList();
        assertThat(habitIds).containsExactlyInAnyOrder(habitId1, habitId2);
    }

    @Test
    void testCollectHabitCompletionData() {
        // When
        HabitCompletionData result = analyticsDataCollector.collectHabitCompletionData(habitId1, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHabitId()).isEqualTo(habitId1);
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getCompletions()).isNotEmpty();
        
        // Verify all completions are for the correct habit
        assertThat(result.getCompletions()).allMatch(completion -> 
            completion.getHabitId().equals(habitId1));
    }

    @Test
    void testCollectAllUserHabitsData() {
        // When
        Map<UUID, HabitCompletionData> result = analyticsDataCollector.collectAllUserHabitsData(userId, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(habitId1, habitId2);
        
        // Verify each habit has its own completion data
        HabitCompletionData habit1Data = result.get(habitId1);
        assertThat(habit1Data.getHabitId()).isEqualTo(habitId1);
        assertThat(habit1Data.getCompletions()).allMatch(completion -> 
            completion.getHabitId().equals(habitId1));
        
        HabitCompletionData habit2Data = result.get(habitId2);
        assertThat(habit2Data.getHabitId()).isEqualTo(habitId2);
        assertThat(habit2Data.getCompletions()).allMatch(completion -> 
            completion.getHabitId().equals(habitId2));
    }

    @Test
    void testCollectGroupCompletionData() {
        // When
        GroupCompletionData result = analyticsDataCollector.collectGroupCompletionData(groupId, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getStartDate()).isEqualTo(testStartDate);
        assertThat(result.getEndDate()).isEqualTo(testEndDate);
        assertThat(result.getGroupHabits()).hasSize(1);
        assertThat(result.getHabitCompletions()).containsKey(groupHabitId);
        
        List<GroupHabitCompletion> groupCompletions = result.getHabitCompletions().get(groupHabitId);
        assertThat(groupCompletions).isNotEmpty();
        assertThat(groupCompletions).allMatch(completion -> 
            completion.getGroupHabitId().equals(groupHabitId));
    }

    @Test
    void testCollectRecentCompletions() {
        // When
        RecentCompletionData result = analyticsDataCollector.collectRecentCompletions(3);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(LocalDate.now().minusDays(3));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
        assertThat(result.getRecentDailyCompletions()).isNotEmpty();
        assertThat(result.getRecentGroupCompletions()).isNotEmpty();
        
        // Verify all completions are within the date range
        LocalDate cutoffDate = LocalDate.now().minusDays(3);
        assertThat(result.getRecentDailyCompletions()).allMatch(completion -> 
            !completion.getCompletionDate().isBefore(cutoffDate));
        assertThat(result.getRecentGroupCompletions()).allMatch(completion -> 
            !completion.getCompletionDate().isBefore(cutoffDate));
    }

    @Test
    void testGetDailyCompletionStats() {
        // Given
        LocalDate testDate = LocalDate.now().minusDays(1);

        // When
        DailyCompletionStats result = analyticsDataCollector.getDailyCompletionStats(userId, testDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(testDate);
        assertThat(result.getTotalDailyHabits()).isGreaterThanOrEqualTo(0);
        assertThat(result.getCompletedDailyHabits()).isGreaterThanOrEqualTo(0);
        assertThat(result.getTotalGroupHabits()).isGreaterThanOrEqualTo(0);
        assertThat(result.getCompletedGroupHabits()).isGreaterThanOrEqualTo(0);
        
        // Verify completion rates are valid percentages
        assertThat(result.getDailyCompletionRate()).isBetween(0.0, 1.0);
        assertThat(result.getGroupCompletionRate()).isBetween(0.0, 1.0);
        assertThat(result.getOverallCompletionRate()).isBetween(0.0, 1.0);
    }

    @Test
    void testCollectUserCompletionDataWithNoData() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        UserCompletionData result = analyticsDataCollector.collectUserCompletionData(
            nonExistentUserId, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(nonExistentUserId);
        assertThat(result.getDailyCompletions()).isEmpty();
        assertThat(result.getGroupCompletions()).isEmpty();
        assertThat(result.getUserHabits()).isEmpty();
    }

    @Test
    void testCollectHabitCompletionDataWithNoData() {
        // Given
        UUID nonExistentHabitId = UUID.randomUUID();

        // When
        HabitCompletionData result = analyticsDataCollector.collectHabitCompletionData(
            nonExistentHabitId, testStartDate, testEndDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHabitId()).isEqualTo(nonExistentHabitId);
        assertThat(result.getCompletions()).isEmpty();
    }

    private void createTestCompletionData() {
        // Create daily completions for the test period
        LocalDate currentDate = testStartDate;
        while (!currentDate.isAfter(testEndDate)) {
            // Create completions for habit1 (80% completion rate)
            if (Math.random() < 0.8) {
                DailyCompletion completion1 = new DailyCompletion(habitId1, userId, currentDate);
                completion1.setCompleted(true);
                completion1.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 0)));
                dailyCompletionRepository.save(completion1);
            }

            // Create completions for habit2 (60% completion rate)
            if (Math.random() < 0.6) {
                DailyCompletion completion2 = new DailyCompletion(habitId2, userId, currentDate);
                completion2.setCompleted(true);
                completion2.setCompletedAt(LocalDateTime.of(currentDate, java.time.LocalTime.of(20, 0)));
                dailyCompletionRepository.save(completion2);
            }

            // Create group habit completions (70% completion rate)
            if (Math.random() < 0.7) {
                GroupHabitCompletion groupCompletion = new GroupHabitCompletion(groupHabitId, userId, currentDate, true);
                groupHabitCompletionRepository.save(groupCompletion);
            }

            currentDate = currentDate.plusDays(1);
        }
    }
}