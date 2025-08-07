package com.example.backend.controller;

import com.example.backend.entity.Habit;
import com.example.backend.entity.DailyCompletion;
import com.example.backend.repository.HabitRepository;
import com.example.backend.repository.DailyCompletionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/habits")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "false")
public class HabitController {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private DailyCompletionRepository completionRepository;

    @GetMapping
    public ResponseEntity<?> getAllHabits(HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromRequest(request);
            System.out.println("Fetching habits for user: " + userId);
            
            List<Habit> habits = habitRepository.findByUserId(userId);
            System.out.println("Found " + habits.size() + " habits for user: " + userId);
            return ResponseEntity.ok(habits);
        } catch (Exception e) {
            System.err.println("Error fetching habits: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching habits: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createHabit(@RequestBody Habit habit, HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromRequest(request);
            System.out.println("Creating habit for user: " + userId);
            System.out.println("Habit data: " + habit);
            
            habit.setUserId(userId);
            habit.setCreatedAt(LocalDateTime.now());
            habit.setUpdatedAt(LocalDateTime.now());
            
            Habit savedHabit = habitRepository.save(habit);
            System.out.println("SUCCESS: Created habit with ID: " + savedHabit.getId() + " for user: " + userId);
            return ResponseEntity.ok(savedHabit);
        } catch (Exception e) {
            System.err.println("Error creating habit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating habit: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHabit(@PathVariable UUID id, @RequestBody Habit habit, HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromRequest(request);
            System.out.println("Updating habit " + id + " for user: " + userId);
            
            Optional<Habit> existingHabitOpt = habitRepository.findById(id);
            if (existingHabitOpt.isEmpty() || !existingHabitOpt.get().getUserId().equals(userId)) {
                System.err.println("Habit not found or access denied for habit ID: " + id + ", user: " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Habit not found or access denied");
            }
            
            Habit existingHabit = existingHabitOpt.get();
            existingHabit.setName(habit.getName());
            existingHabit.setDescription(habit.getDescription());
            existingHabit.setColor(habit.getColor());
            existingHabit.setUpdatedAt(LocalDateTime.now());
            
            Habit updatedHabit = habitRepository.save(existingHabit);
            System.out.println("SUCCESS: Updated habit ID: " + id + " for user: " + userId);
            return ResponseEntity.ok(updatedHabit);
        } catch (Exception e) {
            System.err.println("Error updating habit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating habit: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@PathVariable UUID id, HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromRequest(request);
            System.out.println("Deleting habit " + id + " for user: " + userId);
            
            Optional<Habit> existingHabitOpt = habitRepository.findById(id);
            if (existingHabitOpt.isEmpty() || !existingHabitOpt.get().getUserId().equals(userId)) {
                System.err.println("Habit not found or access denied for habit ID: " + id + ", user: " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Habit not found or access denied");
            }
            
            habitRepository.deleteById(id);
            System.out.println("SUCCESS: Deleted habit ID: " + id + " for user: " + userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting habit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting habit: " + e.getMessage());
        }
    }

    @GetMapping("/completions")
    public ResponseEntity<List<DailyCompletion>> getCompletions(HttpServletRequest request, @RequestParam(required = false) String date) {
        UUID userId = getUserIdFromRequest(request);

        if (date != null) {
            LocalDate completionDate = LocalDate.parse(date);
            List<DailyCompletion> completions = completionRepository.findByUserIdAndCompletionDate(userId, completionDate);
            return ResponseEntity.ok(completions);
        } else {
            List<DailyCompletion> completions = completionRepository.findByUserId(userId);
            return ResponseEntity.ok(completions);
        }
    }

    @PostMapping("/completions")
    public ResponseEntity<DailyCompletion> markCompletion(HttpServletRequest request, @RequestBody CompletionRequest requestCompletion) {
        UUID userId = getUserIdFromRequest(request);
        LocalDate completionDate = LocalDate.parse(requestCompletion.getCompletionDate());

        DailyCompletion completion = completionRepository
            .findByHabitIdAndCompletionDate(requestCompletion.getHabitId(), completionDate)
            .orElse(new DailyCompletion());

        completion.setHabitId(requestCompletion.getHabitId());
        completion.setUserId(userId);
        completion.setCompletionDate(completionDate);
        completion.setCompleted(requestCompletion.getCompleted());
        completion.setNotes(requestCompletion.getNotes());

        DailyCompletion savedCompletion = completionRepository.save(completion);
        return ResponseEntity.ok(savedCompletion);
    }

    public static class CompletionRequest {
        private UUID habitId;
        private String completionDate;
        private Boolean completed;
        private String notes;

        public UUID getHabitId() { return habitId; }
        public void setHabitId(UUID habitId) { this.habitId = habitId; }

        public String getCompletionDate() { return completionDate; }
        public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }

        public Boolean getCompleted() { return completed; }
        public void setCompleted(Boolean completed) { this.completed = completed; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    // Test endpoint to verify the controller is working
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Habit controller is working!");
    }

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
                System.out.println("Authenticated user ID: " + userIdStr);
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid UUID format in request: " + userIdStr);
                throw new IllegalArgumentException("Invalid user ID format in request");
            }
        }
        
        System.err.println("No user ID found in authenticated request");
        throw new IllegalArgumentException("No user ID found in request - authentication may have failed");
    }
}
