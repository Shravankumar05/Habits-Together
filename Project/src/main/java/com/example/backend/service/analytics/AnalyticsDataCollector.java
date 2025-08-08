package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.GroupHabitCompletion;
import com.example.backend.entity.Habit;
import com.example.backend.entity.GroupHabit;
import com.example.backend.repository.DailyCompletionRepository;
import com.example.backend.repository.GroupHabitCompletionRepository;
import com.example.backend.repository.HabitRepository;
import com.example.backend.repository.GroupHabitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for collecting and aggregating habit completion data
 * from existing entities for analytics processing.
 */
@Service
public class AnalyticsDataCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDataCollector.class);
    
    @Autowired
    private DailyCompletionRepository dailyCompletionRepository;
    
    @Autowired
    private GroupHabitCompletionRepository groupHabitCompletionRepository;
    
    @Autowired
    private HabitRepository habitRepository;
    
    @Autowired
    private GroupHabitRepository groupHabitRepository;
    
    /**
     * Collects all completion data for a specific user within a date range.
     */
    public UserCompletionData collectUserCompletionData(UUID userId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Collecting completion data for user {} from {} to {}", userId, startDate, endDate);
        
        // Collect individual habit completions
        List<DailyCompletion> dailyCompletions = collectDailyCompletions(userId, startDate, endDate);
        
        // Collect group habit completions
        List<GroupHabitCompletion> groupCompletions = collectGroupCompletions(userId, startDate, endDate);
        
        // Get user's habits for context
        List<Habit> userHabits = habitRepository.findByUserId(userId);
        
        UserCompletionData data = new UserCompletionData(userId, startDate, endDate);
        data.setDailyCompletions(dailyCompletions);
        data.setGroupCompletions(groupCompletions);
        data.setUserHabits(userHabits);
        
        logger.debug("Collected {} daily completions and {} group completions for user {}", 
                    dailyCompletions.size(), groupCompletions.size(), userId);
        
        return data;
    }
    
    /**
     * Collects completion data for a specific habit within a date range.
     */
    public HabitCompletionData collectHabitCompletionData(UUID habitId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Collecting completion data for habit {} from {} to {}", habitId, startDate, endDate);
        
        List<DailyCompletion> completions = dailyCompletionRepository.findByHabitIdAndUserIdAndCompletionDateBetween(
            habitId, null, startDate, endDate);
        
        // Filter out null user IDs and get unique completions
        List<DailyCompletion> validCompletions = completions.stream()
            .filter(completion -> completion.getUserId() != null)
            .collect(Collectors.toList());
        
        HabitCompletionData data = new HabitCompletionData(habitId, startDate, endDate);
        data.setCompletions(validCompletions);
        
        logger.debug("Collected {} completions for habit {}", validCompletions.size(), habitId);
        
        return data;
    }
    
    /**
     * Collects completion data for all habits of a specific user.
     */
    public Map<UUID, HabitCompletionData> collectAllUserHabitsData(UUID userId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Collecting all habit data for user {} from {} to {}", userId, startDate, endDate);
        
        List<Habit> userHabits = habitRepository.findByUserId(userId);
        
        return userHabits.stream()
            .collect(Collectors.toMap(
                Habit::getId,
                habit -> collectHabitCompletionData(habit.getId(), startDate, endDate)
            ));
    }
    
    /**
     * Collects group completion data for analytics.
     */
    public GroupCompletionData collectGroupCompletionData(UUID groupId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Collecting group completion data for group {} from {} to {}", groupId, startDate, endDate);
        
        // Get all group habits
        List<GroupHabit> groupHabits = groupHabitRepository.findByGroupId(groupId);
        
        // Collect completions for each group habit
        Map<UUID, List<GroupHabitCompletion>> habitCompletions = groupHabits.stream()
            .collect(Collectors.toMap(
                GroupHabit::getId,
                habit -> groupHabitCompletionRepository.findCompletionsByHabitAndDateRange(
                    habit.getId(), startDate, endDate)
            ));
        
        GroupCompletionData data = new GroupCompletionData(groupId, startDate, endDate);
        data.setGroupHabits(groupHabits);
        data.setHabitCompletions(habitCompletions);
        
        int totalCompletions = habitCompletions.values().stream()
            .mapToInt(List::size)
            .sum();
        
        logger.debug("Collected {} total completions across {} habits for group {}", 
                    totalCompletions, groupHabits.size(), groupId);
        
        return data;
    }
    
    /**
     * Collects recent completion data for real-time analytics.
     */
    public RecentCompletionData collectRecentCompletions(int daysBack) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysBack);
        
        logger.debug("Collecting recent completions for last {} days", daysBack);
        
        // Get all recent daily completions
        List<DailyCompletion> recentDaily = dailyCompletionRepository.findAll().stream()
            .filter(completion -> !completion.getCompletionDate().isBefore(startDate))
            .collect(Collectors.toList());
        
        // Get all recent group completions
        List<GroupHabitCompletion> recentGroup = groupHabitCompletionRepository.findAll().stream()
            .filter(completion -> !completion.getCompletionDate().isBefore(startDate))
            .collect(Collectors.toList());
        
        RecentCompletionData data = new RecentCompletionData(startDate, endDate);
        data.setRecentDailyCompletions(recentDaily);
        data.setRecentGroupCompletions(recentGroup);
        
        logger.debug("Collected {} recent daily and {} recent group completions", 
                    recentDaily.size(), recentGroup.size());
        
        return data;
    }
    
    /**
     * Gets completion statistics for a user on a specific date.
     */
    public DailyCompletionStats getDailyCompletionStats(UUID userId, LocalDate date) {
        List<DailyCompletion> dailyCompletions = dailyCompletionRepository.findByUserIdAndCompletionDate(userId, date);
        List<GroupHabitCompletion> groupCompletions = groupHabitCompletionRepository.findByUserIdAndCompletionDate(userId, date);
        
        long completedDaily = dailyCompletions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        long completedGroup = groupCompletions.stream()
            .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
            .sum();
        
        return new DailyCompletionStats(
            date,
            dailyCompletions.size(),
            (int) completedDaily,
            groupCompletions.size(),
            (int) completedGroup
        );
    }
    
    // Private helper methods
    
    private List<DailyCompletion> collectDailyCompletions(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Habit> userHabits = habitRepository.findByUserId(userId);
        
        return userHabits.stream()
            .flatMap(habit -> dailyCompletionRepository.findByHabitIdAndUserIdAndCompletionDateBetween(
                habit.getId(), userId, startDate, endDate).stream())
            .collect(Collectors.toList());
    }
    
    private List<GroupHabitCompletion> collectGroupCompletions(UUID userId, LocalDate startDate, LocalDate endDate) {
        return groupHabitCompletionRepository.findAll().stream()
            .filter(completion -> completion.getUserId().equals(userId))
            .filter(completion -> !completion.getCompletionDate().isBefore(startDate))
            .filter(completion -> !completion.getCompletionDate().isAfter(endDate))
            .collect(Collectors.toList());
    }
    
    // Data Transfer Objects
    
    public static class UserCompletionData {
        private final UUID userId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private List<DailyCompletion> dailyCompletions;
        private List<GroupHabitCompletion> groupCompletions;
        private List<Habit> userHabits;
        
        public UserCompletionData(UUID userId, LocalDate startDate, LocalDate endDate) {
            this.userId = userId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters and setters
        public UUID getUserId() { return userId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<DailyCompletion> getDailyCompletions() { return dailyCompletions; }
        public void setDailyCompletions(List<DailyCompletion> dailyCompletions) { this.dailyCompletions = dailyCompletions; }
        public List<GroupHabitCompletion> getGroupCompletions() { return groupCompletions; }
        public void setGroupCompletions(List<GroupHabitCompletion> groupCompletions) { this.groupCompletions = groupCompletions; }
        public List<Habit> getUserHabits() { return userHabits; }
        public void setUserHabits(List<Habit> userHabits) { this.userHabits = userHabits; }
    }
    
    public static class HabitCompletionData {
        private final UUID habitId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private List<DailyCompletion> completions;
        
        public HabitCompletionData(UUID habitId, LocalDate startDate, LocalDate endDate) {
            this.habitId = habitId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters and setters
        public UUID getHabitId() { return habitId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<DailyCompletion> getCompletions() { return completions; }
        public void setCompletions(List<DailyCompletion> completions) { this.completions = completions; }
    }
    
    public static class GroupCompletionData {
        private final UUID groupId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private List<GroupHabit> groupHabits;
        private Map<UUID, List<GroupHabitCompletion>> habitCompletions;
        
        public GroupCompletionData(UUID groupId, LocalDate startDate, LocalDate endDate) {
            this.groupId = groupId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters and setters
        public UUID getGroupId() { return groupId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<GroupHabit> getGroupHabits() { return groupHabits; }
        public void setGroupHabits(List<GroupHabit> groupHabits) { this.groupHabits = groupHabits; }
        public Map<UUID, List<GroupHabitCompletion>> getHabitCompletions() { return habitCompletions; }
        public void setHabitCompletions(Map<UUID, List<GroupHabitCompletion>> habitCompletions) { this.habitCompletions = habitCompletions; }
    }
    
    public static class RecentCompletionData {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private List<DailyCompletion> recentDailyCompletions;
        private List<GroupHabitCompletion> recentGroupCompletions;
        
        public RecentCompletionData(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        // Getters and setters
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public List<DailyCompletion> getRecentDailyCompletions() { return recentDailyCompletions; }
        public void setRecentDailyCompletions(List<DailyCompletion> recentDailyCompletions) { this.recentDailyCompletions = recentDailyCompletions; }
        public List<GroupHabitCompletion> getRecentGroupCompletions() { return recentGroupCompletions; }
        public void setRecentGroupCompletions(List<GroupHabitCompletion> recentGroupCompletions) { this.recentGroupCompletions = recentGroupCompletions; }
    }
    
    public static class DailyCompletionStats {
        private final LocalDate date;
        private final int totalDailyHabits;
        private final int completedDailyHabits;
        private final int totalGroupHabits;
        private final int completedGroupHabits;
        
        public DailyCompletionStats(LocalDate date, int totalDailyHabits, int completedDailyHabits, 
                                   int totalGroupHabits, int completedGroupHabits) {
            this.date = date;
            this.totalDailyHabits = totalDailyHabits;
            this.completedDailyHabits = completedDailyHabits;
            this.totalGroupHabits = totalGroupHabits;
            this.completedGroupHabits = completedGroupHabits;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public int getTotalDailyHabits() { return totalDailyHabits; }
        public int getCompletedDailyHabits() { return completedDailyHabits; }
        public int getTotalGroupHabits() { return totalGroupHabits; }
        public int getCompletedGroupHabits() { return completedGroupHabits; }
        
        public double getDailyCompletionRate() {
            return totalDailyHabits > 0 ? (double) completedDailyHabits / totalDailyHabits : 0.0;
        }
        
        public double getGroupCompletionRate() {
            return totalGroupHabits > 0 ? (double) completedGroupHabits / totalGroupHabits : 0.0;
        }
        
        public double getOverallCompletionRate() {
            int totalHabits = totalDailyHabits + totalGroupHabits;
            int totalCompleted = completedDailyHabits + completedGroupHabits;
            return totalHabits > 0 ? (double) totalCompleted / totalHabits : 0.0;
        }
    }
}