package com.example.backend.service.analytics;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.entity.GroupHabitCompletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for aggregating and processing habit completion data
 * into meaningful statistics for analytics.
 */
@Service
public class CompletionDataAggregator {
    
    private static final Logger logger = LoggerFactory.getLogger(CompletionDataAggregator.class);
    
    /**
     * Aggregates daily completion statistics for a user over a date range.
     */
    public DailyAggregationResult aggregateDailyCompletions(List<DailyCompletion> completions, 
                                                           LocalDate startDate, LocalDate endDate) {
        logger.debug("Aggregating daily completions from {} to {}", startDate, endDate);
        
        Map<LocalDate, List<DailyCompletion>> completionsByDate = completions.stream()
            .collect(Collectors.groupingBy(DailyCompletion::getCompletionDate));
        
        Map<LocalDate, CompletionStats> dailyStats = new HashMap<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            List<DailyCompletion> dayCompletions = completionsByDate.getOrDefault(currentDate, Collections.emptyList());
            
            long totalHabits = dayCompletions.size();
            long completedHabits = dayCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double completionRate = totalHabits > 0 ? (double) completedHabits / totalHabits : 0.0;
            
            dailyStats.put(currentDate, new CompletionStats(
                (int) totalHabits, 
                (int) completedHabits, 
                completionRate
            ));
            
            currentDate = currentDate.plusDays(1);
        }
        
        return new DailyAggregationResult(startDate, endDate, dailyStats);
    }
    
    /**
     * Aggregates weekly completion statistics.
     */
    public WeeklyAggregationResult aggregateWeeklyCompletions(List<DailyCompletion> completions,
                                                             LocalDate startDate, LocalDate endDate) {
        logger.debug("Aggregating weekly completions from {} to {}", startDate, endDate);
        
        Map<LocalDate, List<DailyCompletion>> completionsByWeek = completions.stream()
            .collect(Collectors.groupingBy(completion -> 
                completion.getCompletionDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
        
        Map<LocalDate, WeeklyStats> weeklyStats = new HashMap<>();
        
        for (Map.Entry<LocalDate, List<DailyCompletion>> entry : completionsByWeek.entrySet()) {
            LocalDate weekStart = entry.getKey();
            List<DailyCompletion> weekCompletions = entry.getValue();
            
            // Group by day of week
            Map<DayOfWeek, List<DailyCompletion>> dailyBreakdown = weekCompletions.stream()
                .collect(Collectors.groupingBy(completion -> 
                    completion.getCompletionDate().getDayOfWeek()));
            
            // Calculate daily completion rates for the week
            Map<DayOfWeek, Double> dailyRates = new HashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                List<DailyCompletion> dayCompletions = dailyBreakdown.getOrDefault(day, Collections.emptyList());
                long completed = dayCompletions.stream()
                    .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                    .sum();
                double rate = dayCompletions.size() > 0 ? (double) completed / dayCompletions.size() : 0.0;
                dailyRates.put(day, rate);
            }
            
            long totalWeeklyHabits = weekCompletions.size();
            long completedWeeklyHabits = weekCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double weeklyCompletionRate = totalWeeklyHabits > 0 ? 
                (double) completedWeeklyHabits / totalWeeklyHabits : 0.0;
            
            weeklyStats.put(weekStart, new WeeklyStats(
                (int) totalWeeklyHabits,
                (int) completedWeeklyHabits,
                weeklyCompletionRate,
                dailyRates
            ));
        }
        
        return new WeeklyAggregationResult(startDate, endDate, weeklyStats);
    }
    
    /**
     * Aggregates completion patterns by time of day.
     */
    public TimePatternResult aggregateTimePatterns(List<DailyCompletion> completions) {
        logger.debug("Aggregating time patterns for {} completions", completions.size());
        
        Map<Integer, List<DailyCompletion>> completionsByHour = completions.stream()
            .filter(completion -> completion.getCompletedAt() != null)
            .collect(Collectors.groupingBy(completion -> 
                completion.getCompletedAt().getHour()));
        
        Map<Integer, CompletionStats> hourlyStats = new HashMap<>();
        
        for (int hour = 0; hour < 24; hour++) {
            List<DailyCompletion> hourCompletions = completionsByHour.getOrDefault(hour, Collections.emptyList());
            
            long totalHabits = hourCompletions.size();
            long completedHabits = hourCompletions.stream()
                .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                .sum();
            
            double completionRate = totalHabits > 0 ? (double) completedHabits / totalHabits : 0.0;
            
            hourlyStats.put(hour, new CompletionStats(
                (int) totalHabits,
                (int) completedHabits,
                completionRate
            ));
        }
        
        return new TimePatternResult(hourlyStats);
    }
    
    /**
     * Calculates streak information from completion data.
     */
    public StreakAnalysisResult analyzeStreaks(List<DailyCompletion> completions, UUID habitId) {
        logger.debug("Analyzing streaks for habit {} with {} completions", habitId, completions.size());
        
        // Filter and sort completions for the specific habit
        List<DailyCompletion> habitCompletions = completions.stream()
            .filter(completion -> completion.getHabitId().equals(habitId))
            .filter(DailyCompletion::getCompleted)
            .sorted(Comparator.comparing(DailyCompletion::getCompletionDate))
            .collect(Collectors.toList());
        
        if (habitCompletions.isEmpty()) {
            return new StreakAnalysisResult(0, 0, Collections.emptyList());
        }
        
        List<StreakPeriod> streaks = new ArrayList<>();
        int currentStreak = 0;
        int maxStreak = 0;
        
        LocalDate previousDate = null;
        LocalDate currentStreakStart = null;
        
        for (DailyCompletion completion : habitCompletions) {
            LocalDate completionDate = completion.getCompletionDate();
            
            if (previousDate == null) {
                // First completion
                currentStreak = 1;
                currentStreakStart = completionDate;
            } else if (ChronoUnit.DAYS.between(previousDate, completionDate) == 1) {
                // Consecutive day
                currentStreak++;
            } else {
                // Streak broken, record the previous streak
                if (currentStreak > 0) {
                    streaks.add(new StreakPeriod(currentStreakStart, previousDate, currentStreak));
                    maxStreak = Math.max(maxStreak, currentStreak);
                }
                currentStreak = 1;
                currentStreakStart = completionDate;
            }
            
            previousDate = completionDate;
        }
        
        // Record the final streak
        if (currentStreak > 0) {
            streaks.add(new StreakPeriod(currentStreakStart, previousDate, currentStreak));
            maxStreak = Math.max(maxStreak, currentStreak);
        }
        
        // Current streak is the last streak if it ends today or yesterday
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        int activeStreak = 0;
        if (!streaks.isEmpty()) {
            StreakPeriod lastStreak = streaks.get(streaks.size() - 1);
            if (lastStreak.getEndDate().equals(today) || lastStreak.getEndDate().equals(yesterday)) {
                activeStreak = lastStreak.getLength();
            }
        }
        
        return new StreakAnalysisResult(activeStreak, maxStreak, streaks);
    }
    
    /**
     * Aggregates group completion statistics.
     */
    public GroupAggregationResult aggregateGroupCompletions(Map<UUID, List<GroupHabitCompletion>> habitCompletions,
                                                           LocalDate startDate, LocalDate endDate) {
        logger.debug("Aggregating group completions for {} habits from {} to {}", 
                    habitCompletions.size(), startDate, endDate);
        
        Map<LocalDate, GroupDailyStats> dailyGroupStats = new HashMap<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            final LocalDate date = currentDate;
            
            int totalHabitsForDay = 0;
            int completedHabitsForDay = 0;
            Map<UUID, Integer> habitParticipation = new HashMap<>();
            
            for (Map.Entry<UUID, List<GroupHabitCompletion>> entry : habitCompletions.entrySet()) {
                UUID habitId = entry.getKey();
                List<GroupHabitCompletion> completions = entry.getValue().stream()
                    .filter(completion -> completion.getCompletionDate().equals(date))
                    .collect(Collectors.toList());
                
                totalHabitsForDay += completions.size();
                completedHabitsForDay += (int) completions.stream()
                    .mapToLong(completion -> completion.getCompleted() ? 1 : 0)
                    .sum();
                
                habitParticipation.put(habitId, completions.size());
            }
            
            double completionRate = totalHabitsForDay > 0 ? 
                (double) completedHabitsForDay / totalHabitsForDay : 0.0;
            
            dailyGroupStats.put(currentDate, new GroupDailyStats(
                totalHabitsForDay,
                completedHabitsForDay,
                completionRate,
                habitParticipation
            ));
            
            currentDate = currentDate.plusDays(1);
        }
        
        return new GroupAggregationResult(startDate, endDate, dailyGroupStats);
    }
    
    // Data Transfer Objects
    
    public static class CompletionStats {
        private final int totalHabits;
        private final int completedHabits;
        private final double completionRate;
        
        public CompletionStats(int totalHabits, int completedHabits, double completionRate) {
            this.totalHabits = totalHabits;
            this.completedHabits = completedHabits;
            this.completionRate = completionRate;
        }
        
        public int getTotalHabits() { return totalHabits; }
        public int getCompletedHabits() { return completedHabits; }
        public double getCompletionRate() { return completionRate; }
    }
    
    public static class DailyAggregationResult {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<LocalDate, CompletionStats> dailyStats;
        
        public DailyAggregationResult(LocalDate startDate, LocalDate endDate, Map<LocalDate, CompletionStats> dailyStats) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dailyStats = dailyStats;
        }
        
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<LocalDate, CompletionStats> getDailyStats() { return dailyStats; }
        
        public double getAverageCompletionRate() {
            return dailyStats.values().stream()
                .mapToDouble(CompletionStats::getCompletionRate)
                .average()
                .orElse(0.0);
        }
    }
    
    public static class WeeklyStats {
        private final int totalHabits;
        private final int completedHabits;
        private final double completionRate;
        private final Map<DayOfWeek, Double> dailyRates;
        
        public WeeklyStats(int totalHabits, int completedHabits, double completionRate, Map<DayOfWeek, Double> dailyRates) {
            this.totalHabits = totalHabits;
            this.completedHabits = completedHabits;
            this.completionRate = completionRate;
            this.dailyRates = dailyRates;
        }
        
        public int getTotalHabits() { return totalHabits; }
        public int getCompletedHabits() { return completedHabits; }
        public double getCompletionRate() { return completionRate; }
        public Map<DayOfWeek, Double> getDailyRates() { return dailyRates; }
    }
    
    public static class WeeklyAggregationResult {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<LocalDate, WeeklyStats> weeklyStats;
        
        public WeeklyAggregationResult(LocalDate startDate, LocalDate endDate, Map<LocalDate, WeeklyStats> weeklyStats) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.weeklyStats = weeklyStats;
        }
        
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<LocalDate, WeeklyStats> getWeeklyStats() { return weeklyStats; }
    }
    
    public static class TimePatternResult {
        private final Map<Integer, CompletionStats> hourlyStats;
        
        public TimePatternResult(Map<Integer, CompletionStats> hourlyStats) {
            this.hourlyStats = hourlyStats;
        }
        
        public Map<Integer, CompletionStats> getHourlyStats() { return hourlyStats; }
        
        public OptionalInt getPeakHour() {
            return hourlyStats.entrySet().stream()
                .max(Comparator.comparing(entry -> entry.getValue().getCompletionRate()))
                .map(Map.Entry::getKey)
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
        }
    }
    
    public static class StreakPeriod {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int length;
        
        public StreakPeriod(LocalDate startDate, LocalDate endDate, int length) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.length = length;
        }
        
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getLength() { return length; }
    }
    
    public static class StreakAnalysisResult {
        private final int currentStreak;
        private final int maxStreak;
        private final List<StreakPeriod> allStreaks;
        
        public StreakAnalysisResult(int currentStreak, int maxStreak, List<StreakPeriod> allStreaks) {
            this.currentStreak = currentStreak;
            this.maxStreak = maxStreak;
            this.allStreaks = allStreaks;
        }
        
        public int getCurrentStreak() { return currentStreak; }
        public int getMaxStreak() { return maxStreak; }
        public List<StreakPeriod> getAllStreaks() { return allStreaks; }
    }
    
    public static class GroupDailyStats {
        private final int totalHabits;
        private final int completedHabits;
        private final double completionRate;
        private final Map<UUID, Integer> habitParticipation;
        
        public GroupDailyStats(int totalHabits, int completedHabits, double completionRate, Map<UUID, Integer> habitParticipation) {
            this.totalHabits = totalHabits;
            this.completedHabits = completedHabits;
            this.completionRate = completionRate;
            this.habitParticipation = habitParticipation;
        }
        
        public int getTotalHabits() { return totalHabits; }
        public int getCompletedHabits() { return completedHabits; }
        public double getCompletionRate() { return completionRate; }
        public Map<UUID, Integer> getHabitParticipation() { return habitParticipation; }
    }
    
    public static class GroupAggregationResult {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Map<LocalDate, GroupDailyStats> dailyStats;
        
        public GroupAggregationResult(LocalDate startDate, LocalDate endDate, Map<LocalDate, GroupDailyStats> dailyStats) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dailyStats = dailyStats;
        }
        
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Map<LocalDate, GroupDailyStats> getDailyStats() { return dailyStats; }
    }
}