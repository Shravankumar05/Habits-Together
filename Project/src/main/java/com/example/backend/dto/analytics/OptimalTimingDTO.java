package com.example.backend.dto.analytics;

import com.example.backend.service.analytics.OptimalTimingAnalyzer;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for optimal timing analysis results.
 */
public class OptimalTimingDTO {
    
    private UUID habitId;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime optimalStartTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime optimalEndTime;
    
    private Map<Integer, TimingStatsDTO> hourlyStats;
    private Map<String, TimingStatsDTO> dayOfWeekStats;
    private List<TimeWindowDTO> bestTimeWindows;
    
    public OptimalTimingDTO() {}
    
    public OptimalTimingDTO(OptimalTimingAnalyzer.OptimalTimingResult result) {
        this.habitId = result.getHabitId();
        this.optimalStartTime = result.getOptimalStartTime();
        this.optimalEndTime = result.getOptimalEndTime();
        
        this.hourlyStats = result.getHourlyStats().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new TimingStatsDTO(entry.getValue())
            ));
        
        this.dayOfWeekStats = result.getDayOfWeekStats().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().name(),
                entry -> new TimingStatsDTO(entry.getValue())
            ));
    }
    
    public OptimalTimingDTO(OptimalTimingAnalyzer.OptimalTimingResult result, List<OptimalTimingAnalyzer.TimeWindow> timeWindows) {
        this(result);
        this.bestTimeWindows = timeWindows.stream()
            .map(TimeWindowDTO::new)
            .collect(Collectors.toList());
    }
    
    // Getters and Setters
    public UUID getHabitId() { return habitId; }
    public void setHabitId(UUID habitId) { this.habitId = habitId; }
    
    public LocalTime getOptimalStartTime() { return optimalStartTime; }
    public void setOptimalStartTime(LocalTime optimalStartTime) { this.optimalStartTime = optimalStartTime; }
    
    public LocalTime getOptimalEndTime() { return optimalEndTime; }
    public void setOptimalEndTime(LocalTime optimalEndTime) { this.optimalEndTime = optimalEndTime; }
    
    public Map<Integer, TimingStatsDTO> getHourlyStats() { return hourlyStats; }
    public void setHourlyStats(Map<Integer, TimingStatsDTO> hourlyStats) { this.hourlyStats = hourlyStats; }
    
    public Map<String, TimingStatsDTO> getDayOfWeekStats() { return dayOfWeekStats; }
    public void setDayOfWeekStats(Map<String, TimingStatsDTO> dayOfWeekStats) { this.dayOfWeekStats = dayOfWeekStats; }
    
    public List<TimeWindowDTO> getBestTimeWindows() { return bestTimeWindows; }
    public void setBestTimeWindows(List<TimeWindowDTO> bestTimeWindows) { this.bestTimeWindows = bestTimeWindows; }
    
    public static class TimingStatsDTO {
        private int totalAttempts;
        private int successfulAttempts;
        private double successRate;
        
        public TimingStatsDTO() {}
        
        public TimingStatsDTO(OptimalTimingAnalyzer.TimingStats stats) {
            this.totalAttempts = stats.getTotalAttempts();
            this.successfulAttempts = stats.getSuccessfulAttempts();
            this.successRate = stats.getSuccessRate();
        }
        
        // Getters and Setters
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        
        public int getSuccessfulAttempts() { return successfulAttempts; }
        public void setSuccessfulAttempts(int successfulAttempts) { this.successfulAttempts = successfulAttempts; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }
    
    public static class TimeWindowDTO {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        
        private double successRate;
        private int sampleSize;
        
        public TimeWindowDTO() {}
        
        public TimeWindowDTO(OptimalTimingAnalyzer.TimeWindow window) {
            this.startTime = window.getStartTime();
            this.endTime = window.getEndTime();
            this.successRate = window.getSuccessRate();
            this.sampleSize = window.getSampleSize();
        }
        
        // Getters and Setters
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
        
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public int getSampleSize() { return sampleSize; }
        public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }
    }
}