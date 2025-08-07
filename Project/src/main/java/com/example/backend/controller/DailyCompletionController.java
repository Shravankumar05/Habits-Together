package com.example.backend.controller;

import com.example.backend.entity.DailyCompletion;
import com.example.backend.repository.DailyCompletionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/daily-completions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "false")
public class DailyCompletionController {

    @Autowired
    private DailyCompletionRepository dailyCompletionRepository;

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        // Check if authentication was processed
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        Boolean authMissing = (Boolean) request.getAttribute("authenticationMissing");
        
        if (Boolean.TRUE.equals(authMissing) || Boolean.FALSE.equals(authenticated)) {
            System.err.println("ERROR: Request is not authenticated - authentication required");
            throw new IllegalArgumentException("Authentication required - please provide valid authorization token");
        }
        
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            try {
                System.out.println("Authenticated user ID for completion: " + userIdStr);
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid UUID format in request: " + userIdStr);
                throw new IllegalArgumentException("Invalid user ID format in request");
            }
        }
        
        System.err.println("No user ID found in authenticated request");
        throw new IllegalArgumentException("No user ID found in request - authentication may have failed");
    }

    @PostMapping("/toggle")
    public ResponseEntity<DailyCompletion> toggleCompletion(
            @RequestBody ToggleCompletionRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            UUID habitId = UUID.fromString(request.getHabitId());
            System.out.println("=== HABIT COMPLETION TOGGLE ===");
            System.out.println("User: " + userId + " toggling habit: " + habitId);
            
            LocalDate date = LocalDate.parse(request.getDate());
            System.out.println("Date: " + date);
            
            // Check if completion exists for this user, habit, and date
            Optional<DailyCompletion> existingCompletion = dailyCompletionRepository
                    .findByHabitIdAndUserIdAndCompletionDate(habitId, userId, date);
            
            DailyCompletion completion;
            if (existingCompletion.isPresent()) {
                completion = existingCompletion.get();
                // Toggle the completion status
                completion.setCompleted(!completion.getCompleted());
                completion.setCompletedAt(LocalDateTime.now()); // Update timestamp
                System.out.println("TOGGLED existing completion to: " + completion.getCompleted());
            } else {
                // Create new completion record
                completion = new DailyCompletion();
                completion.setHabitId(habitId);
                completion.setUserId(userId);
                completion.setCompletionDate(date);
                completion.setCompleted(true);
                completion.setCompletedAt(LocalDateTime.now());
                System.out.println("CREATED new completion: true");
            }
            
            DailyCompletion savedCompletion = dailyCompletionRepository.save(completion);
            System.out.println("SUCCESS: Habit completion saved for user: " + userId);
            System.out.println("=== END HABIT COMPLETION TOGGLE ===");
            return ResponseEntity.ok(savedCompletion);
        } catch (Exception e) {
            System.err.println("Error toggling habit completion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<DailyCompletion> createOrUpdateCompletion(
            @RequestBody CompletionRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            UUID habitId = UUID.fromString(request.getHabitId());
            LocalDate completionDate = LocalDate.parse(request.getCompletionDate());
            
            System.out.println("=== CREATING/UPDATING COMPLETION ===");
            System.out.println("User: " + userId + " updating completion for habit: " + habitId);
            System.out.println("Date: " + completionDate + ", Completed: " + request.getCompleted());
            
            // Check if completion already exists
            Optional<DailyCompletion> existingCompletion = dailyCompletionRepository
                    .findByHabitIdAndUserIdAndCompletionDate(habitId, userId, completionDate);
            
            DailyCompletion completion;
            if (existingCompletion.isPresent()) {
                completion = existingCompletion.get();
                completion.setCompleted(request.getCompleted());
                completion.setNotes(request.getNotes());
                completion.setCompletedAt(LocalDateTime.now());
                System.out.println("UPDATED existing completion");
            } else {
                completion = new DailyCompletion();
                completion.setHabitId(habitId);
                completion.setUserId(userId);
                completion.setCompletionDate(completionDate);
                completion.setCompleted(request.getCompleted());
                completion.setNotes(request.getNotes());
                completion.setCompletedAt(LocalDateTime.now());
                System.out.println("CREATED new completion");
            }
            
            DailyCompletion savedCompletion = dailyCompletionRepository.save(completion);
            System.out.println("SUCCESS: Completion saved for user: " + userId);
            System.out.println("=== END CREATING/UPDATING COMPLETION ===");
            
            return ResponseEntity.ok(savedCompletion);
        } catch (Exception e) {
            System.err.println("Error creating/updating completion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<DailyCompletion> checkCompletion(
            @RequestParam String habitId,
            @RequestParam String date,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            UUID habitUuid = UUID.fromString(habitId);
            LocalDate completionDate = LocalDate.parse(date);
            
            System.out.println("=== CHECKING COMPLETION STATUS ===");
            System.out.println("User: " + userId + " checking habit: " + habitUuid + " for date: " + completionDate);
            
            Optional<DailyCompletion> completion = dailyCompletionRepository
                    .findByHabitIdAndUserIdAndCompletionDate(habitUuid, userId, completionDate);
            
            if (completion.isPresent()) {
                System.out.println("Found completion record: " + completion.get().getCompleted());
                System.out.println("=== END COMPLETION CHECK ===");
                return ResponseEntity.ok(completion.get());
            } else {
                System.out.println("No completion record found - habit not completed");
                System.out.println("=== END COMPLETION CHECK ===");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error checking completion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DailyCompletion>> getCompletions(
            @RequestParam String habitId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            UUID habitUuid = UUID.fromString(habitId);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            System.out.println("=== GETTING COMPLETIONS ===");
            System.out.println("User: " + userId + " requesting completions for habit: " + habitUuid);
            System.out.println("Date range: " + start + " to " + end);
            
            List<DailyCompletion> completions = dailyCompletionRepository
                    .findByHabitIdAndUserIdAndCompletionDateBetween(habitUuid, userId, start, end);
            
            System.out.println("Found " + completions.size() + " completion records");
            System.out.println("=== END GETTING COMPLETIONS ===");
            
            return ResponseEntity.ok(completions);
        } catch (Exception e) {
            System.err.println("Error fetching completions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/calendar/{habitId}")
    public ResponseEntity<List<DailyCompletion>> getCompletionCalendar(
            @PathVariable String habitId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            UUID habitUuid = UUID.fromString(habitId);
            
            System.out.println("=== CALENDAR REQUEST ===");
            System.out.println("User: " + userId + " requesting calendar for habit: " + habitUuid);
            
            // Default to last 30 days if no date range specified
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            
            System.out.println("Date range: " + start + " to " + end);
            
            // Get all completions for this user and habit in the date range
            List<DailyCompletion> completions = dailyCompletionRepository
                    .findByHabitIdAndUserIdAndCompletionDateBetween(habitUuid, userId, start, end);
            
            System.out.println("Found " + completions.size() + " completion records");
            System.out.println("=== END CALENDAR REQUEST ===");
            
            return ResponseEntity.ok(completions);
        } catch (Exception e) {
            System.err.println("Error fetching completion calendar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<List<DailyCompletion>> getTodayCompletions(HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromRequest(httpRequest);
            LocalDate today = LocalDate.now();
            
            System.out.println("=== TODAY'S COMPLETIONS ===");
            System.out.println("User: " + userId + " requesting today's completions: " + today);
            
            List<DailyCompletion> todayCompletions = dailyCompletionRepository
                    .findByUserIdAndCompletionDate(userId, today);
            
            System.out.println("Found " + todayCompletions.size() + " completions for today");
            System.out.println("=== END TODAY'S COMPLETIONS ===");
            
            return ResponseEntity.ok(todayCompletions);
        } catch (Exception e) {
            System.err.println("Error fetching today's completions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public static class ToggleCompletionRequest {
        private String habitId;
        private String date;

        public String getHabitId() { return habitId; }
        public void setHabitId(String habitId) { this.habitId = habitId; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class CompletionRequest {
        private String habitId;
        private String completionDate;
        private Boolean completed;
        private String notes;

        public String getHabitId() { return habitId; }
        public void setHabitId(String habitId) { this.habitId = habitId; }
        public String getCompletionDate() { return completionDate; }
        public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }
        public Boolean getCompleted() { return completed; }
        public void setCompleted(Boolean completed) { this.completed = completed; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
