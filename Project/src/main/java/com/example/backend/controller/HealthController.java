package com.example.backend.controller;

import com.example.backend.entity.Group;
import com.example.backend.entity.GroupMember;
import com.example.backend.entity.User;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.GroupMemberRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.HabitRepository;
import com.example.backend.repository.DailyCompletionRepository;
import com.example.backend.repository.GroupHabitRepository;
import com.example.backend.repository.GroupHabitCompletionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" }, allowCredentials = "false")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private DailyCompletionRepository dailyCompletionRepository;

    @Autowired
    private GroupHabitRepository groupHabitRepository;

    @Autowired
    private GroupHabitCompletionRepository groupHabitCompletionRepository;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        System.out.println("=== DATABASE HEALTH CHECK ===");
        Map<String, Object> health = new HashMap<>();

        try {
            // Test database connection
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                health.put("database_url", metaData.getURL());
                health.put("database_product", metaData.getDatabaseProductName());
                health.put("database_version", metaData.getDatabaseProductVersion());
                health.put("driver_name", metaData.getDriverName());
                health.put("driver_version", metaData.getDriverVersion());
                health.put("connection_valid", conn.isValid(5));

                System.out.println("Database connection successful");
                System.out.println("URL: " + metaData.getURL());
                System.out.println("Product: " + metaData.getDatabaseProductName());
                System.out.println("Version: " + metaData.getDatabaseProductVersion());
            }

            // Test table existence and record counts
            Map<String, Object> tableCounts = new HashMap<>();

            try {
                long userCount = userRepository.count();
                tableCounts.put("users", userCount);
                System.out.println("Users table: " + userCount + " records");
            } catch (Exception e) {
                tableCounts.put("users", "ERROR: " + e.getMessage());
                System.err.println("Error accessing users table: " + e.getMessage());
            }

            try {
                long habitCount = habitRepository.count();
                tableCounts.put("habits", habitCount);
                System.out.println("Habits table: " + habitCount + " records");
            } catch (Exception e) {
                tableCounts.put("habits", "ERROR: " + e.getMessage());
                System.err.println("Error accessing habits table: " + e.getMessage());
            }

            try {
                long completionCount = dailyCompletionRepository.count();
                tableCounts.put("daily_completions", completionCount);
                System.out.println("Daily completions table: " + completionCount + " records");
            } catch (Exception e) {
                tableCounts.put("daily_completions", "ERROR: " + e.getMessage());
                System.err.println("Error accessing daily_completions table: " + e.getMessage());
            }

            try {
                long groupCount = groupRepository.count();
                tableCounts.put("groups", groupCount);
                System.out.println("Groups table: " + groupCount + " records");
            } catch (Exception e) {
                tableCounts.put("groups", "ERROR: " + e.getMessage());
                System.err.println("Error accessing groups table: " + e.getMessage());
            }

            try {
                long memberCount = groupMemberRepository.count();
                tableCounts.put("group_members", memberCount);
                System.out.println("Group members table: " + memberCount + " records");
            } catch (Exception e) {
                tableCounts.put("group_members", "ERROR: " + e.getMessage());
                System.err.println("Error accessing group_members table: " + e.getMessage());
            }

            try {
                long groupHabitCount = groupHabitRepository.count();
                tableCounts.put("group_habits", groupHabitCount);
                System.out.println("Group habits table: " + groupHabitCount + " records");
            } catch (Exception e) {
                tableCounts.put("group_habits", "ERROR: " + e.getMessage());
                System.err.println("Error accessing group_habits table: " + e.getMessage());
            }

            try {
                long groupCompletionCount = groupHabitCompletionRepository.count();
                tableCounts.put("group_habit_completions", groupCompletionCount);
                System.out.println("Group habit completions table: " + groupCompletionCount + " records");
            } catch (Exception e) {
                tableCounts.put("group_habit_completions", "ERROR: " + e.getMessage());
                System.err.println("Error accessing group_habit_completions table: " + e.getMessage());
            }

            health.put("table_counts", tableCounts);

            // Test specific queries that are used in group functionality
            Map<String, Object> queryTests = new HashMap<>();

            try {
                List<User> allUsers = userRepository.findAll();
                queryTests.put("findAllUsers", "SUCCESS - " + allUsers.size() + " users");
                System.out.println("Query test - findAllUsers: " + allUsers.size() + " users");

                if (!allUsers.isEmpty()) {
                    User firstUser = allUsers.get(0);
                    System.out.println("First user: " + firstUser.getId() + " - " + firstUser.getEmail());

                    // Test group queries for this user
                    List<Group> userGroups = groupRepository.findGroupsByUserId(firstUser.getId());
                    queryTests.put("findGroupsByUserId", "SUCCESS - " + userGroups.size() + " groups for user");
                    System.out.println("Query test - findGroupsByUserId: " + userGroups.size() + " groups");

                    List<GroupMember> userMemberships = groupMemberRepository.findByUserId(firstUser.getId());
                    queryTests.put("findGroupMembersByUserId", "SUCCESS - " + userMemberships.size() + " memberships");
                    System.out.println(
                            "Query test - findGroupMembersByUserId: " + userMemberships.size() + " memberships");
                }
            } catch (Exception e) {
                queryTests.put("userQueries", "ERROR: " + e.getMessage());
                System.err.println("Error in user queries: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                List<Group> allGroups = groupRepository.findAll();
                queryTests.put("findAllGroups", "SUCCESS - " + allGroups.size() + " groups");
                System.out.println("Query test - findAllGroups: " + allGroups.size() + " groups");

                for (Group group : allGroups) {
                    System.out.println("  Group: " + group.getId() + " - " + group.getName() + " (created by: "
                            + group.getCreatedBy() + ")");
                }
            } catch (Exception e) {
                queryTests.put("findAllGroups", "ERROR: " + e.getMessage());
                System.err.println("Error in group queries: " + e.getMessage());
                e.printStackTrace();
            }

            health.put("query_tests", queryTests);
            health.put("status", "HEALTHY");
            health.put("timestamp", System.currentTimeMillis());

            System.out.println("=== END DATABASE HEALTH CHECK ===");
            return ResponseEntity.ok(health);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in database health check:");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();

            health.put("status", "UNHEALTHY");
            health.put("error", e.getMessage());
            health.put("error_class", e.getClass().getName());
            health.put("timestamp", System.currentTimeMillis());

            System.out.println("=== END DATABASE HEALTH CHECK (ERROR) ===");
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("message", "Backend is running successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/create-test-jwt")
    public ResponseEntity<Map<String, Object>> createTestJwt() {
        System.out.println("=== CREATE TEST JWT ===");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create a simple JWT-like token for testing
            // This is just for testing - in production, Supabase creates real JWTs
            String userId = "a27fdb4e-fab4-4e9a-a1c8-c2b12b852d10";
            
            // Create a simple payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", userId);
            payload.put("user_id", userId);
            payload.put("email", "user@example.com");
            payload.put("iat", System.currentTimeMillis() / 1000);
            payload.put("exp", (System.currentTimeMillis() / 1000) + 3600); // 1 hour
            
            // Convert to JSON
            String payloadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            
            // Base64 encode (this is a simplified version - real JWTs have headers and signatures)
            String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
            
            // Create a simple test token (header.payload.signature format)
            String testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + encodedPayload + ".test-signature";
            
            result.put("test_token", testToken);
            result.put("user_id", userId);
            result.put("payload", payload);
            result.put("instructions", "Use this token with: Authorization: Bearer " + testToken);
            
            System.out.println("Test JWT created for user: " + userId);
            System.out.println("Token: " + testToken);
            System.out.println("=== END CREATE TEST JWT ===");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERROR creating test JWT: " + e.getMessage());
            e.printStackTrace();
            
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<Map<String, Object>> testJwt(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                result.put("error", "No Authorization header found");
                return ResponseEntity.badRequest().body(result);
            }

            String jwt = authHeader.substring(7);
            String[] parts = jwt.split("\\.");

            if (parts.length != 3) {
                result.put("error", "Invalid JWT format");
                return ResponseEntity.badRequest().body(result);
            }

            // Decode payload
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload,
                    Map.class);

            result.put("jwt_length", jwt.length());
            result.put("claims", claims);
            result.put("user_id_from_sub", claims.get("sub"));
            result.put("user_id_from_user_id", claims.get("user_id"));

            System.out.println("JWT Claims: " + claims);
            System.out.println("=== END TEST JWT PARSING ===");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR parsing JWT: " + e.getMessage());
            e.printStackTrace();

            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/create-real-user")
    public ResponseEntity<Map<String, Object>> createRealUser() {
        System.out.println("=== CREATE REAL SUPABASE USER ===");
        Map<String, Object> result = new HashMap<>();

        try {
            // Create the real Supabase user with the provided UUID
            UUID realUserId = UUID.fromString("a27fdb4e-fab4-4e9a-a1c8-c2b12b852d10");

            // Check if user already exists
            Optional<User> existingUser = userRepository.findById(realUserId);
            if (existingUser.isPresent()) {
                result.put("message", "Real Supabase user already exists");
                result.put("user_id", existingUser.get().getId());
                result.put("email", existingUser.get().getEmail());
                return ResponseEntity.ok(result);
            }

            User realUser = new User();
            realUser.setId(realUserId);
            realUser.setEmail("user@example.com"); // You can update this
            realUser.setDisplayName("Real User");
            realUser.setAvatarUrl(null);

            User savedUser = userRepository.save(realUser);

            result.put("message", "Real Supabase user created successfully");
            result.put("user_id", savedUser.getId());
            result.put("email", savedUser.getEmail());
            result.put("display_name", savedUser.getDisplayName());

            System.out.println("Real Supabase user created: " + savedUser.getId());
            System.out.println("=== END CREATE REAL SUPABASE USER ===");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR creating real Supabase user: " + e.getMessage());
            e.printStackTrace();

            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        System.out.println("=== CREATE TEST USER ===");
        Map<String, Object> result = new HashMap<>();

        try {
            // Create a test user with a known UUID for testing
            UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

            // Check if user already exists
            Optional<User> existingUser = userRepository.findById(testUserId);
            if (existingUser.isPresent()) {
                result.put("message", "Test user already exists");
                result.put("user_id", existingUser.get().getId());
                result.put("email", existingUser.get().getEmail());
                return ResponseEntity.ok(result);
            }

            User testUser = new User();
            testUser.setId(testUserId);
            testUser.setEmail("test@example.com");
            testUser.setDisplayName("Test User");
            testUser.setAvatarUrl(null);

            User savedUser = userRepository.save(testUser);

            result.put("message", "Test user created successfully");
            result.put("user_id", savedUser.getId());
            result.put("email", savedUser.getEmail());
            result.put("display_name", savedUser.getDisplayName());

            System.out.println("Test user created: " + savedUser.getId());
            System.out.println("=== END CREATE TEST USER ===");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR creating test user: " + e.getMessage());
            e.printStackTrace();

            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/test-group-creation")
    public ResponseEntity<Map<String, Object>> testGroupCreation() {
        System.out.println("=== TEST GROUP CREATION ===");
        Map<String, Object> result = new HashMap<>();

        try {
            // Check if we have any users
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                result.put("error", "No users found in database. Cannot test group creation.");
                return ResponseEntity.badRequest().body(result);
            }

            User testUser = users.get(0);
            System.out.println("Using test user: " + testUser.getId() + " - " + testUser.getEmail());

            // Create a test group
            Group testGroup = new Group();
            testGroup.setName("Test Group " + System.currentTimeMillis());
            testGroup.setDescription("Test group for debugging");
            testGroup.setCreatedBy(testUser.getId()); // Use String instead of UUID

            System.out.println("Creating test group: " + testGroup.getName());
            Group savedGroup = groupRepository.save(testGroup);
            System.out.println("Test group saved with ID: " + savedGroup.getId());

            // Verify the group was saved
            Optional<Group> verifyGroup = groupRepository.findById(savedGroup.getId());
            if (verifyGroup.isEmpty()) {
                result.put("error", "Group was not found after save");
                return ResponseEntity.status(500).body(result);
            }

            // Add the user as a member
            GroupMember member = new GroupMember();
            member.setGroupId(savedGroup.getId());
            member.setUserId(testUser.getId()); // Already String, no conversion needed
            member.setRole("admin");

            System.out.println("Adding user as member of test group");
            GroupMember savedMember = groupMemberRepository.save(member);
            System.out.println("Member saved with ID: " + savedMember.getId());

            // Test retrieval
            List<Group> userGroups = groupRepository.findGroupsByUserId(testUser.getId()); // Use String instead of UUID
            boolean groupFound = userGroups.stream().anyMatch(g -> g.getId().equals(savedGroup.getId()));

            result.put("test_group_id", savedGroup.getId());
            result.put("test_group_name", savedGroup.getName());
            result.put("test_user_id", testUser.getId());
            result.put("test_user_email", testUser.getEmail());
            result.put("group_found_in_user_query", groupFound);
            result.put("total_user_groups", userGroups.size());
            result.put("status", "SUCCESS");

            System.out.println("Test group creation completed successfully");
            System.out.println("Group found in user query: " + groupFound);
            System.out.println("=== END TEST GROUP CREATION ===");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR in test group creation:");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();

            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("error_class", e.getClass().getName());

            System.out.println("=== END TEST GROUP CREATION (ERROR) ===");
            return ResponseEntity.status(500).body(result);
        }
    }
}
