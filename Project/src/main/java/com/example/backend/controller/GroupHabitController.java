package com.example.backend.controller;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups/{groupId}/habits")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "false")
public class GroupHabitController {

    @Autowired
    private GroupHabitRepository groupHabitRepository;

    @Autowired
    private GroupHabitCompletionRepository completionRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        // Check if authentication was processed
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        Boolean authMissing = (Boolean) request.getAttribute("authenticationMissing");
        
        if (Boolean.TRUE.equals(authMissing) || Boolean.FALSE.equals(authenticated)) {
            System.err.println("ERROR: Request is not authenticated - authentication required");
            throw new IllegalArgumentException("Authentication required - please provide valid authorization token");
        }
        
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid UUID format in request: " + userIdStr);
                throw new IllegalArgumentException("Invalid user ID format in request");
            }
        }
        
        System.err.println("No user ID found in authenticated request");
        throw new IllegalArgumentException("No user ID found in request - authentication may have failed");
    }

    // Get all habits for a group
    @GetMapping
    public ResponseEntity<List<GroupHabit>> getGroupHabits(@PathVariable UUID groupId, HttpServletRequest request) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(request);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            System.out.println("=== GET GROUP HABITS ===");
            System.out.println("User: " + userId + " requesting habits for group: " + groupId);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                System.out.println("ERROR: User is not a member of group: " + groupId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<GroupHabit> habits = groupHabitRepository.findByGroupId(groupId);

            System.out.println("SUCCESS: Found " + habits.size() + " habits for group");
            System.out.println("=== END GET GROUP HABITS ===");
            return ResponseEntity.ok(habits);
        } catch (Exception e) {
            System.err.println("Error getting group habits: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Create a new group habit
    @PostMapping
    public ResponseEntity<GroupHabit> createGroupHabit(@PathVariable UUID groupId, @RequestBody CreateGroupHabitRequest request, HttpServletRequest httpRequest) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(httpRequest);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            System.out.println("=== CREATE GROUP HABIT ===");
            System.out.println("User: " + userId + " creating habit for group: " + groupId);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                System.out.println("ERROR: User is not a member of group: " + groupId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            GroupHabit habit = new GroupHabit(
                groupId,
                request.getName(),
                request.getDescription(),
                request.getColor(),
                userId
            );

            GroupHabit savedHabit = groupHabitRepository.save(habit);

            System.out.println("SUCCESS: Group habit created with ID: " + savedHabit.getId());
            System.out.println("=== END CREATE GROUP HABIT ===");
            return ResponseEntity.ok(savedHabit);
        } catch (Exception e) {
            System.err.println("Error creating group habit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Update a group habit
    @PutMapping("/{habitId}")
    public ResponseEntity<GroupHabit> updateGroupHabit(@PathVariable UUID groupId, @PathVariable UUID habitId, @RequestBody UpdateGroupHabitRequest request, HttpServletRequest httpRequest) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(httpRequest);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            System.out.println("=== UPDATE GROUP HABIT ===");
            System.out.println("User: " + userId + " updating habit: " + habitId + " in group: " + groupId);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                System.out.println("ERROR: User is not a member of group: " + groupId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<GroupHabit> habitOpt = groupHabitRepository.findById(habitId);
            if (habitOpt.isEmpty()) {
                System.out.println("ERROR: Habit not found: " + habitId);
                return ResponseEntity.notFound().build();
            }

            GroupHabit habit = habitOpt.get();
            if (request.getName() != null) habit.setName(request.getName());
            if (request.getDescription() != null) habit.setDescription(request.getDescription());
            if (request.getColor() != null) habit.setColor(request.getColor());
            habit.setUpdatedAt(LocalDateTime.now());

            GroupHabit updatedHabit = groupHabitRepository.save(habit);
            System.out.println("SUCCESS: Updated habit: " + updatedHabit.getId());
            System.out.println("=== END UPDATE GROUP HABIT ===");
            return ResponseEntity.ok(updatedHabit);
        } catch (Exception e) {
            System.err.println("ERROR in updateGroupHabit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete a group habit
    @DeleteMapping("/{habitId}")
    public ResponseEntity<String> deleteGroupHabit(@PathVariable UUID groupId, @PathVariable UUID habitId, HttpServletRequest request) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(request);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            System.out.println("=== DELETE GROUP HABIT ===");
            System.out.println("User: " + userId + " deleting habit: " + habitId + " from group: " + groupId);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                System.out.println("ERROR: User is not a member of group: " + groupId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<GroupHabit> habitOpt = groupHabitRepository.findById(habitId);
            if (habitOpt.isEmpty()) {
                System.out.println("ERROR: Habit not found: " + habitId);
                return ResponseEntity.notFound().build();
            }

            groupHabitRepository.deleteById(habitId);
            System.out.println("SUCCESS: Deleted habit: " + habitId);
            System.out.println("=== END DELETE GROUP HABIT ===");
            return ResponseEntity.ok("Habit deleted successfully");
        } catch (Exception e) {
            System.err.println("ERROR in deleteGroupHabit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get completions for a specific habit on a specific date
    @GetMapping("/{habitId}/completions")
    public ResponseEntity<List<GroupHabitCompletionWithUser>> getHabitCompletions(@PathVariable UUID groupId, @PathVariable UUID habitId, @RequestParam(required = false) String date, HttpServletRequest request) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(request);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            LocalDate completionDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            
            System.out.println("=== GET HABIT COMPLETIONS ===");
            System.out.println("User: " + userId + " requesting completions for habit: " + habitId + " on date: " + completionDate);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<GroupHabitCompletion> completions = completionRepository.findByGroupHabitIdAndCompletionDate(habitId, completionDate);
            
            // Convert to response with user info
            List<GroupHabitCompletionWithUser> completionsWithUsers = completions.stream()
                .map(completion -> {
                    Optional<User> user = userRepository.findById(completion.getUserId());
                    return new GroupHabitCompletionWithUser(
                        completion,
                        user.map(User::getDisplayName).orElse("Unknown User"),
                        user.map(User::getEmail).orElse("unknown@example.com")
                    );
                })
                .collect(Collectors.toList());

            System.out.println("SUCCESS: Found " + completions.size() + " completions");
            System.out.println("=== END GET HABIT COMPLETIONS ===");
            return ResponseEntity.ok(completionsWithUsers);
        } catch (Exception e) {
            System.err.println("Error getting habit completions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Toggle completion for a group habit
    @PostMapping("/{habitId}/completions/toggle")
    public ResponseEntity<GroupHabitCompletion> toggleCompletion(@PathVariable UUID groupId, @PathVariable UUID habitId, @RequestBody ToggleCompletionRequest request, HttpServletRequest httpRequest) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(httpRequest);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            LocalDate date = request.getDate() != null ? LocalDate.parse(request.getDate()) : LocalDate.now();
            
            System.out.println("=== TOGGLE GROUP HABIT COMPLETION ===");
            System.out.println("User: " + userId + " toggling habit: " + habitId + " for date: " + date);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<GroupHabitCompletion> existingCompletion = completionRepository
                .findByGroupHabitIdAndUserIdAndCompletionDate(habitId, userId, date);

            GroupHabitCompletion completion;
            if (existingCompletion.isPresent()) {
                completion = existingCompletion.get();
                completion.setCompleted(!completion.getCompleted());
                completion.setCompletedAt(LocalDateTime.now());
            } else {
                completion = new GroupHabitCompletion(habitId, userId, date, true);
            }

            GroupHabitCompletion savedCompletion = completionRepository.save(completion);

            System.out.println("SUCCESS: Group habit completion toggled");
            System.out.println("=== END TOGGLE GROUP HABIT COMPLETION ===");
            return ResponseEntity.ok(savedCompletion);
        } catch (Exception e) {
            System.err.println("Error toggling group habit completion: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/completions")
    public ResponseEntity<List<GroupHabitCompletionWithUser>> getGroupCompletions(
            @PathVariable UUID groupId,
            @RequestParam String date,
            HttpServletRequest request) {
        try {
            UUID userId;
            try {
                userId = getUserIdFromRequest(request);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            LocalDate completionDate = LocalDate.parse(date);
            
            System.out.println("=== GET GROUP COMPLETIONS ===");
            System.out.println("User: " + userId + " requesting completions for group: " + groupId + " on date: " + completionDate);

            // Check if user is a member of the group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<GroupHabitCompletion> completions = completionRepository.findByGroupIdAndCompletionDate(groupId, completionDate);
            
            // Convert to response with user info
            List<GroupHabitCompletionWithUser> completionsWithUsers = completions.stream()
                .map(completion -> {
                    Optional<User> user = userRepository.findById(completion.getUserId());
                    return new GroupHabitCompletionWithUser(
                        completion,
                        user.map(User::getDisplayName).orElse("Unknown User"),
                        user.map(User::getEmail).orElse("unknown@example.com")
                    );
                })
                .collect(Collectors.toList());

            System.out.println("SUCCESS: Retrieved " + completionsWithUsers.size() + " completions");
            System.out.println("=== END GET GROUP COMPLETIONS ===");
            return ResponseEntity.ok(completionsWithUsers);

        } catch (Exception e) {
            System.err.println("Error getting group completions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Request/Response DTOs
    public static class CreateGroupHabitRequest {
        private String name;
        private String description;
        private String color;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class UpdateGroupHabitRequest {
        private String name;
        private String description;
        private String color;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class ToggleCompletionRequest {
        private String date;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class GroupHabitCompletionWithUser {
        private UUID id;
        private UUID groupHabitId;
        private UUID userId;
        private String userEmail;
        private String userDisplayName;
        private LocalDate completionDate;
        private Boolean completed;
        private String notes;
        private LocalDateTime completedAt;

        public GroupHabitCompletionWithUser() {}

        public GroupHabitCompletionWithUser(UUID id, UUID groupHabitId, UUID userId, String userEmail, String userDisplayName, 
                                          LocalDate completionDate, Boolean completed, String notes, LocalDateTime completedAt) {
            this.id = id;
            this.groupHabitId = groupHabitId;
            this.userId = userId;
            this.userEmail = userEmail;
            this.userDisplayName = userDisplayName;
            this.completionDate = completionDate;
            this.completed = completed;
            this.notes = notes;
            this.completedAt = completedAt;
        }

        public GroupHabitCompletionWithUser(GroupHabitCompletion completion, String userDisplayName, String userEmail) {
            this.id = completion.getId();
            this.groupHabitId = completion.getGroupHabitId();
            this.userId = completion.getUserId();
            this.userEmail = userEmail;
            this.userDisplayName = userDisplayName;
            this.completionDate = completion.getCompletionDate();
            this.completed = completion.getCompleted();
            this.notes = completion.getNotes();
            this.completedAt = completion.getCompletedAt();
        }

        public UUID getId() { return id; }
        public UUID getGroupHabitId() { return groupHabitId; }
        public UUID getUserId() { return userId; }
        public String getUserEmail() { return userEmail; }
        public String getUserDisplayName() { return userDisplayName; }
        public LocalDate getCompletionDate() { return completionDate; }
        public Boolean getCompleted() { return completed; }
        public String getNotes() { return notes; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }
}
