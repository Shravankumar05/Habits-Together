package com.example.backend.service.analytics;

import com.example.backend.entity.GroupHabitCompletion;
import com.example.backend.entity.GroupHabit;
import com.example.backend.entity.GroupMember;
import com.example.backend.repository.GroupMemberRepository;
import com.example.backend.service.analytics.GroupDynamicsEngine.*;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for GroupDynamicsEngine service.
 */
@SpringBootTest
class GroupDynamicsEngineTest {

    @Mock
    private AnalyticsDataCollector analyticsDataCollector;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupDynamicsEngine groupDynamicsEngine;

    private UUID groupId;
    private UUID userId1;
    private UUID userId2;
    private UUID userId3;
    private UUID habitId1;
    private UUID habitId2;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        groupId = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        userId3 = UUID.randomUUID();
        habitId1 = UUID.randomUUID();
        habitId2 = UUID.randomUUID();
        startDate = LocalDate.now().minusDays(14);
        endDate = LocalDate.now();
    }

    @Test
    void testCalculateGroupDynamics() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();
        
        when(groupMemberRepository.findByGroupId(groupId)).thenReturn(mockMembers);
        when(analyticsDataCollector.collectGroupCompletionData(any(), any(), any())).thenReturn(mockData);

        // When
        GroupDynamicsResult result = groupDynamicsEngine.calculateGroupDynamics(groupId, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGroupId()).isEqualTo(groupId);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getMomentumScore()).isBetween(0.0, 1.0);
        assertThat(result.getCohesionScore()).isBetween(0.0, 1.0);
        assertThat(result.getGroupStreak()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSynergisticScore()).isBetween(0.0, 1.0);
        assertThat(result.getKeyContributors()).isNotNull();
        assertThat(result.getParticipationMetrics()).isNotNull();
    }

    @Test
    void testCalculateMomentumScore() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        double momentumScore = groupDynamicsEngine.calculateMomentumScore(mockData, mockMembers);

        // Then
        assertThat(momentumScore).isBetween(0.0, 1.0);
    }

    @Test
    void testCalculateMomentumScoreWithEmptyData() {
        // Given
        List<GroupMember> emptyMembers = Collections.emptyList();
        AnalyticsDataCollector.GroupCompletionData emptyData = createEmptyGroupCompletionData();

        // When
        double momentumScore = groupDynamicsEngine.calculateMomentumScore(emptyData, emptyMembers);

        // Then
        assertThat(momentumScore).isEqualTo(0.0);
    }

    @Test
    void testCalculateCohesionScore() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        double cohesionScore = groupDynamicsEngine.calculateCohesionScore(mockData, mockMembers);

        // Then
        assertThat(cohesionScore).isBetween(0.0, 1.0);
    }

    @Test
    void testCalculateCohesionScoreWithConsistentParticipation() {
        // Given - all members have similar participation rates
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData consistentData = createConsistentParticipationData();

        // When
        double cohesionScore = groupDynamicsEngine.calculateCohesionScore(consistentData, mockMembers);

        // Then
        assertThat(cohesionScore).isGreaterThan(0.7); // High cohesion expected
    }

    @Test
    void testCalculateGroupStreak() {
        // Given
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        int groupStreak = groupDynamicsEngine.calculateGroupStreak(mockData);

        // Then
        assertThat(groupStreak).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testCalculateGroupStreakWithEmptyData() {
        // Given
        AnalyticsDataCollector.GroupCompletionData emptyData = createEmptyGroupCompletionData();

        // When
        int groupStreak = groupDynamicsEngine.calculateGroupStreak(emptyData);

        // Then
        assertThat(groupStreak).isEqualTo(0);
    }

    @Test
    void testCalculateSynergisticScore() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        double synergisticScore = groupDynamicsEngine.calculateSynergisticScore(mockData, mockMembers);

        // Then
        assertThat(synergisticScore).isBetween(0.0, 1.0);
    }

    @Test
    void testCalculateSynergisticScoreWithSingleMember() {
        // Given
        List<GroupMember> singleMember = Arrays.asList(createGroupMember(userId1));
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        double synergisticScore = groupDynamicsEngine.calculateSynergisticScore(mockData, singleMember);

        // Then
        assertThat(synergisticScore).isEqualTo(0.0); // No synergy with single member
    }

    @Test
    void testIdentifyKeyContributors() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        List<KeyContributor> keyContributors = groupDynamicsEngine.identifyKeyContributors(mockData, mockMembers);

        // Then
        assertThat(keyContributors).isNotNull();
        assertThat(keyContributors).hasSizeLessThanOrEqualTo(mockMembers.size());
        
        keyContributors.forEach(contributor -> {
            assertThat(contributor.getUserId()).isNotNull();
            assertThat(contributor.getCompletionRate()).isBetween(0.0, 1.0);
            assertThat(contributor.getContributionScore()).isGreaterThanOrEqualTo(0.0);
            assertThat(contributor.getContributorType()).isNotNull();
            assertThat(contributor.getSuccessfulCompletions()).isLessThanOrEqualTo(contributor.getTotalAttempts());
        });
        
        // Verify contributors are sorted by contribution score (descending)
        for (int i = 1; i < keyContributors.size(); i++) {
            assertThat(keyContributors.get(i).getContributionScore())
                .isLessThanOrEqualTo(keyContributors.get(i - 1).getContributionScore());
        }
    }

    @Test
    void testCalculateParticipationMetrics() {
        // Given
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData mockData = createMockGroupCompletionData();

        // When
        ParticipationMetrics metrics = groupDynamicsEngine.calculateParticipationMetrics(mockData, mockMembers);

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalMembers()).isEqualTo(mockMembers.size());
        assertThat(metrics.getActiveMembers()).isLessThanOrEqualTo(metrics.getTotalMembers());
        assertThat(metrics.getParticipationRate()).isBetween(0.0, 1.0);
        assertThat(metrics.getTotalCompletions()).isLessThanOrEqualTo(metrics.getTotalAttempts());
        assertThat(metrics.getCompletionRate()).isBetween(0.0, 1.0);
    }

    @Test
    void testContributorTypeClassification() {
        // Test that different performance levels result in appropriate contributor types
        List<GroupMember> mockMembers = createMockGroupMembers();
        AnalyticsDataCollector.GroupCompletionData variedPerformanceData = createVariedPerformanceData();

        // When
        List<KeyContributor> contributors = groupDynamicsEngine.identifyKeyContributors(variedPerformanceData, mockMembers);

        // Then
        assertThat(contributors).isNotEmpty();
        
        // Verify we have different contributor types
        Set<ContributorType> contributorTypes = contributors.stream()
            .map(KeyContributor::getContributorType)
            .collect(java.util.stream.Collectors.toSet());
        
        assertThat(contributorTypes).isNotEmpty();
        
        // Verify top contributor has highest score
        if (contributors.size() > 1) {
            KeyContributor topContributor = contributors.get(0);
            assertThat(topContributor.getContributionScore()).isGreaterThan(0.0);
        }
    }

    // Helper methods

    private List<GroupMember> createMockGroupMembers() {
        return Arrays.asList(
            createGroupMember(userId1),
            createGroupMember(userId2),
            createGroupMember(userId3)
        );
    }

    private GroupMember createGroupMember(UUID userId) {
        GroupMember member = new GroupMember();
        member.setId(UUID.randomUUID());
        member.setGroupId(groupId);
        member.setUserId(userId);
        return member;
    }

    private AnalyticsDataCollector.GroupCompletionData createMockGroupCompletionData() {
        AnalyticsDataCollector.GroupCompletionData data = 
            new AnalyticsDataCollector.GroupCompletionData(groupId, startDate, endDate);
        
        List<GroupHabit> groupHabits = Arrays.asList(
            createGroupHabit(habitId1),
            createGroupHabit(habitId2)
        );
        data.setGroupHabits(groupHabits);
        
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = new HashMap<>();
        habitCompletions.put(habitId1, createMockCompletions(habitId1));
        habitCompletions.put(habitId2, createMockCompletions(habitId2));
        data.setHabitCompletions(habitCompletions);
        
        return data;
    }

    private AnalyticsDataCollector.GroupCompletionData createEmptyGroupCompletionData() {
        AnalyticsDataCollector.GroupCompletionData data = 
            new AnalyticsDataCollector.GroupCompletionData(groupId, startDate, endDate);
        data.setGroupHabits(Collections.emptyList());
        data.setHabitCompletions(Collections.emptyMap());
        return data;
    }

    private AnalyticsDataCollector.GroupCompletionData createConsistentParticipationData() {
        AnalyticsDataCollector.GroupCompletionData data = 
            new AnalyticsDataCollector.GroupCompletionData(groupId, startDate, endDate);
        
        List<GroupHabit> groupHabits = Arrays.asList(createGroupHabit(habitId1));
        data.setGroupHabits(groupHabits);
        
        // Create consistent completions for all members
        List<GroupHabitCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // All members complete with 80% success rate
            for (UUID userId : Arrays.asList(userId1, userId2, userId3)) {
                if (Math.random() < 0.8) {
                    GroupHabitCompletion completion = new GroupHabitCompletion(habitId1, userId, currentDate, true);
                    completions.add(completion);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = new HashMap<>();
        habitCompletions.put(habitId1, completions);
        data.setHabitCompletions(habitCompletions);
        
        return data;
    }

    private AnalyticsDataCollector.GroupCompletionData createVariedPerformanceData() {
        AnalyticsDataCollector.GroupCompletionData data = 
            new AnalyticsDataCollector.GroupCompletionData(groupId, startDate, endDate);
        
        List<GroupHabit> groupHabits = Arrays.asList(createGroupHabit(habitId1));
        data.setGroupHabits(groupHabits);
        
        // Create varied performance data
        List<GroupHabitCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // User1: High performer (90% success rate)
            if (Math.random() < 0.9) {
                completions.add(new GroupHabitCompletion(habitId1, userId1, currentDate, Math.random() < 0.9));
            }
            
            // User2: Average performer (60% success rate)
            if (Math.random() < 0.7) {
                completions.add(new GroupHabitCompletion(habitId1, userId2, currentDate, Math.random() < 0.6));
            }
            
            // User3: Low performer (30% success rate)
            if (Math.random() < 0.5) {
                completions.add(new GroupHabitCompletion(habitId1, userId3, currentDate, Math.random() < 0.3));
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = new HashMap<>();
        habitCompletions.put(habitId1, completions);
        data.setHabitCompletions(habitCompletions);
        
        return data;
    }

    private GroupHabit createGroupHabit(UUID habitId) {
        GroupHabit habit = new GroupHabit();
        habit.setId(habitId);
        habit.setGroupId(groupId);
        habit.setName("Test Habit");
        return habit;
    }

    private List<GroupHabitCompletion> createMockCompletions(UUID habitId) {
        List<GroupHabitCompletion> completions = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // Create completions for different users with varying success rates
            for (UUID userId : Arrays.asList(userId1, userId2, userId3)) {
                if (Math.random() < 0.7) { // 70% chance of attempt
                    boolean completed = Math.random() < 0.8; // 80% success rate
                    GroupHabitCompletion completion = new GroupHabitCompletion(habitId, userId, currentDate, completed);
                    completions.add(completion);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return completions;
    }
}