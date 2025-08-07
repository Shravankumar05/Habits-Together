package com.example.backend.controller;

import com.example.backend.entity.Group;
import com.example.backend.entity.GroupMember;
import com.example.backend.entity.User;
import com.example.backend.repository.GroupMemberRepository;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:3000")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        // Log all request attributes for debugging
        System.out.println("=== REQUEST ATTRIBUTES IN GROUP CONTROLLER ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        
        Enumeration<String> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            System.out.println(attrName + " = " + request.getAttribute(attrName));
        }
        
        // Log all headers for debugging
        System.out.println("=== REQUEST HEADERS ===");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        
        // Check if authentication was processed
        Boolean authenticated = (Boolean) request.getAttribute("authenticated");
        Boolean authMissing = (Boolean) request.getAttribute("authenticationMissing");
        
        System.out.println("Authentication status - authenticated: " + authenticated + ", authMissing: " + authMissing);
        
        if (Boolean.TRUE.equals(authMissing) || Boolean.FALSE.equals(authenticated)) {
            System.err.println("ERROR: Request is not authenticated - authentication required");
            throw new IllegalArgumentException("Authentication required - please provide valid authorization token");
        }
        
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            System.err.println("No user ID found in authenticated request");
            throw new IllegalArgumentException("No user ID found in request - authentication may have failed");
        }

        try {
            // Only accept valid UUIDs from real JWT tokens
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format in request: " + userIdStr);
            throw new IllegalArgumentException("Invalid user ID format - must be a valid UUID");
        }
    }

    // Get all groups for a user
    @GetMapping
    public ResponseEntity<?> getGroupsForUser(HttpServletRequest httpRequest) {
        try {
            System.out.println("=== GET GROUPS FOR USER ===");
            
            UUID userId;
            try {
                userId = getUserIdFromRequest(httpRequest);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required: " + e.getMessage()));
            }
            
            System.out.println("Fetching groups for user: " + userId);
            
            List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
            System.out.println("Found " + memberships.size() + " group memberships for user");
            
            List<GroupWithMemberCount> groupsWithCounts = memberships.stream()
                .map(membership -> {
                    Optional<Group> groupOpt = groupRepository.findById(membership.getGroupId());
                    if (groupOpt.isPresent()) {
                        Group group = groupOpt.get();
                        long memberCount = groupMemberRepository.countByGroupId(group.getId());
                        return new GroupWithMemberCount(group, memberCount);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            System.out.println("Returning " + groupsWithCounts.size() + " groups with member counts");
            System.out.println("=== END GET GROUPS FOR USER ===");
            
            return ResponseEntity.ok(groupsWithCounts);
        } catch (Exception e) {
            System.err.println("ERROR in getGroupsForUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch groups: " + e.getMessage()));
        }
    }

    // Create a new group
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== CREATE GROUP ===");
            System.out.println("Processing group creation request...");
            System.out.println("Request URL: " + httpRequest.getRequestURL());
            System.out.println("Content Type: " + httpRequest.getContentType());
            
            // Log request body for debugging
            System.out.println("Request body: " + (request != null ? 
                ("name: " + request.getName() + ", description: " + request.getDescription()) : "null"));
            
            if (request == null) {
                System.err.println("ERROR: Request body is null");
                return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
            }
            
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                System.err.println("ERROR: Group name is null or empty");
                return ResponseEntity.badRequest().body(Map.of("error", "Group name is required"));
            }
            
            System.out.println("=== BEFORE USER ID VALIDATION ===");
            UUID userId;
            try {
                System.out.println("Calling getUserIdFromRequest...");
                userId = getUserIdFromRequest(httpRequest);
                System.out.println("Successfully got user ID: " + userId);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Invalid or missing user ID: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required: " + e.getMessage()));
            }

            System.out.println("Creating new group with name: " + request.getName());
            System.out.println("Database connection status check...");
            
            // Test database connectivity
            try {
                long totalGroups = groupRepository.count();
                System.out.println("Current total groups in database: " + totalGroups);
            } catch (Exception e) {
                System.err.println("ERROR: Database connectivity issue: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database connectivity issue: " + e.getMessage()));
            }
            
            // Create the group entity
            Group group = new Group();
            group.setName(request.getName().trim());
            group.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            group.setCreatedBy(userId);
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("Group object created:");
            System.out.println("  Name: " + group.getName());
            System.out.println("  Description: " + group.getDescription());
            System.out.println("  Created By: " + group.getCreatedBy());
            System.out.println("  Created At: " + group.getCreatedAt());
            System.out.println("Saving group to database...");
            
            Group savedGroup = groupRepository.save(group);
            System.out.println("Group saved successfully!");
            System.out.println("  Saved Group ID: " + savedGroup.getId());
            System.out.println("  Saved Group Name: " + savedGroup.getName());
            System.out.println("  Saved Group Created By: " + savedGroup.getCreatedBy());
            System.out.println("  Saved Group Created At: " + savedGroup.getCreatedAt());

            // Verify the group was actually saved by querying it back
            System.out.println("Verifying group persistence...");
            Optional<Group> verifyGroup = groupRepository.findById(savedGroup.getId());
            if (verifyGroup.isPresent()) {
                System.out.println("SUCCESS: Group verified in database after save");
                System.out.println("  Verified Group: " + verifyGroup.get().getName());
            } else {
                System.err.println("ERROR: Group not found in database after save!");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Group was not persisted properly"));
            }

            // Add the creator as a member of the group
            System.out.println("Adding creator as group member...");
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(savedGroup.getId());
            groupMember.setUserId(userId);
            groupMember.setRole("admin");
            groupMember.setJoinedAt(LocalDateTime.now());
            
            GroupMember savedMember = groupMemberRepository.save(groupMember);
            System.out.println("Group member added successfully!");
            System.out.println("  Member ID: " + savedMember.getId());
            System.out.println("  Group ID: " + savedMember.getGroupId());
            System.out.println("  User ID: " + savedMember.getUserId());
            System.out.println("  Role: " + savedMember.getRole());

            return ResponseEntity.ok(savedGroup);
        } catch (Exception e) {
            System.err.println("ERROR in createGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create group: " + e.getMessage()));
        }
    }

    // Get group details with members
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupDetails(@PathVariable UUID groupId, HttpServletRequest request) {
        try {
            System.out.println("=== INCOMING REQUEST TO /api/groups/{groupId} GET ===");
            System.out.println("HTTP Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Content Type: " + request.getContentType());
            System.out.println("Authorization: " + request.getHeader("Authorization"));
            UUID userId = getUserIdFromRequest(request);
            System.out.println("=== GET GROUP DETAILS ===");
            System.out.println("User: " + userId + " requesting group: " + groupId);

            Optional<Group> group = groupRepository.findById(groupId);
            if (group.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if user is a member of this group
            Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (membership.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not a member of this group"));
            }

            // Get all members of the group
            List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
            List<GroupMemberDetails> memberDetails = members.stream()
                .map(member -> {
                    Optional<User> user = userRepository.findById(member.getUserId());
                    return new GroupMemberDetails(
                        member.getId(),
                        member.getUserId(),
                        user.map(User::getEmail).orElse("unknown@example.com"),
                        user.map(User::getDisplayName).orElse("Unknown User"),
                        member.getRole(),
                        member.getJoinedAt()
                    );
                })
                .collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                "group", group.get(),
                "members", memberDetails,
                "userRole", membership.get().getRole()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in getGroupDetails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get group details: " + e.getMessage()));
        }
    }

    // Add member to group
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMember(@PathVariable UUID groupId, @RequestBody AddMemberRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== INCOMING REQUEST TO /api/groups/{groupId}/members POST ===");
            System.out.println("HTTP Method: " + httpRequest.getMethod());
            System.out.println("Request URI: " + httpRequest.getRequestURI());
            System.out.println("Content Type: " + httpRequest.getContentType());
            System.out.println("Authorization: " + httpRequest.getHeader("Authorization"));
            UUID userId = getUserIdFromRequest(httpRequest);
            System.out.println("=== ADD GROUP MEMBER ===");
            System.out.println("User: " + userId + " adding member to group: " + groupId);
            
            // Validate request
            if (request == null || request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));
            }
            
            UUID memberId;
            try {
                memberId = UUID.fromString(request.getUserId());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid user ID format"));
            }
            
            // Check if the group exists
            Optional<Group> group = groupRepository.findById(groupId);
            if (group.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if the requesting user is a member of the group (and has admin rights)
            Optional<GroupMember> requestingMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (requestingMember.isEmpty() || !"admin".equals(requestingMember.get().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only group admins can add members"));
            }
            
            // Check if user exists
            Optional<User> user = userRepository.findById(memberId);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User does not exist"));
            }
            
            // Check if member is already in the group
            boolean alreadyMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, memberId);
            if (alreadyMember) {
                return ResponseEntity.badRequest().body(Map.of("error", "User is already a member of this group"));
            }
            
            // Add member to group
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setUserId(memberId);
            groupMember.setRole(request.getRole() != null ? request.getRole() : "member");
            groupMember.setJoinedAt(LocalDateTime.now());
            
            GroupMember savedMember = groupMemberRepository.save(groupMember);
            
            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            System.err.println("ERROR in addMember: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add member to group: " + e.getMessage()));
        }
    }

    // Remove member from group
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable UUID groupId, @PathVariable UUID memberId, HttpServletRequest request) {
        try {
            System.out.println("=== INCOMING REQUEST TO /api/groups/{groupId}/members/{memberId} DELETE ===");
            System.out.println("HTTP Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Content Type: " + request.getContentType());
            System.out.println("Authorization: " + request.getHeader("Authorization"));
            UUID userId = getUserIdFromRequest(request);
            System.out.println("=== REMOVE GROUP MEMBER ===");
            System.out.println("User: " + userId + " removing member from group: " + groupId);
            System.out.println("Member ID to remove: " + memberId);
            
            // Check if the group exists
            Optional<Group> group = groupRepository.findById(groupId);
            if (group.isEmpty()) {
                System.out.println("ERROR: Group not found with ID: " + groupId);
                return ResponseEntity.notFound().build();
            }
            
            // Check if the requesting user is a member of the group (and has admin rights)
            Optional<GroupMember> requestingMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
            if (requestingMember.isEmpty() || !"admin".equals(requestingMember.get().getRole())) {
                System.out.println("ERROR: User is not an admin or not a member of the group");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only group admins can remove members"));
            }
            
            // Check if member exists in the group
            Optional<GroupMember> memberToRemove = groupMemberRepository.findByGroupIdAndUserId(groupId, memberId);
            if (memberToRemove.isEmpty()) {
                System.out.println("ERROR: Member not found in group");
                return ResponseEntity.notFound().build();
            }
            
            // Prevent admin from removing themselves if they're the only admin
            if (userId.equals(memberId) && "admin".equals(memberToRemove.get().getRole())) {
                long adminCount = groupMemberRepository.countByGroupIdAndRole(groupId, "admin");
                if (adminCount <= 1) {
                    System.out.println("ERROR: Cannot remove the only admin from the group");
                    return ResponseEntity.badRequest().body(Map.of("error", "Cannot remove the only admin from the group"));
                }
            }
            
            // Remove member from group
            groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberId);
            
            System.out.println("SUCCESS: Member removed from group");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("ERROR in removeMember: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to remove member from group: " + e.getMessage()));
        }
    }

    // Delete group
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID groupId, HttpServletRequest request) {
        try {
            System.out.println("=== INCOMING REQUEST TO /api/groups/{groupId} DELETE ===");
            System.out.println("HTTP Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Content Type: " + request.getContentType());
            System.out.println("Authorization: " + request.getHeader("Authorization"));
            UUID userId = getUserIdFromRequest(request);
            System.out.println("=== DELETE GROUP ===");
            System.out.println("User: " + userId + " deleting group: " + groupId);
            
            // Check if the group exists
            Optional<Group> group = groupRepository.findById(groupId);
            if (group.isEmpty()) {
                System.out.println("ERROR: Group not found with ID: " + groupId);
                return ResponseEntity.notFound().build();
            }
            
            // Check if the requesting user is the creator of the group
            if (!group.get().getCreatedBy().equals(userId)) {
                System.out.println("ERROR: User is not the creator of the group");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only the group creator can delete the group"));
            }
            
            // Delete the group (this will cascade delete related members, habits, and completions)
            groupRepository.deleteById(groupId);
            
            System.out.println("SUCCESS: Group deleted");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("ERROR in deleteGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete group: " + e.getMessage()));
        }
    }

    // Join group by group ID
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable UUID groupId, HttpServletRequest request) {
        try {
            System.out.println("=== INCOMING REQUEST TO /api/groups/{groupId}/join POST ===");
            System.out.println("HTTP Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Content Type: " + request.getContentType());
            System.out.println("Authorization: " + request.getHeader("Authorization"));
            UUID userId = getUserIdFromRequest(request);
            System.out.println("=== JOIN GROUP BY ID ===");
            System.out.println("User: " + userId + " joining group: " + groupId);
            
            // Check if the group exists
            Optional<Group> group = groupRepository.findById(groupId);
            if (group.isEmpty()) {
                System.out.println("ERROR: Group not found with ID: " + groupId);
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is already a member of the group
            boolean alreadyMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
            if (alreadyMember) {
                System.out.println("ERROR: User is already a member of this group");
                return ResponseEntity.badRequest().body(Map.of("error", "You are already a member of this group"));
            }
            
            // Add user as a member of the group
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setUserId(userId);
            groupMember.setRole("member"); // Default role for new members
            groupMember.setJoinedAt(LocalDateTime.now());
            
            GroupMember savedMember = groupMemberRepository.save(groupMember);
            
            System.out.println("SUCCESS: User joined group");
            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            System.err.println("ERROR in joinGroup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to join group: " + e.getMessage()));
        }
    }

    // Inner class for group member details
    public static class GroupMemberDetails {
        private UUID id;
        private UUID userId;
        private String userEmail;
        private String userDisplayName;
        private String role;
        private LocalDateTime joinedAt;

        public GroupMemberDetails() {}

        public GroupMemberDetails(UUID id, UUID userId, String userEmail, String userDisplayName, String role, LocalDateTime joinedAt) {
            this.id = id;
            this.userId = userId;
            this.userEmail = userEmail;
            this.userDisplayName = userDisplayName;
            this.role = role;
            this.joinedAt = joinedAt;
        }

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public String getUserDisplayName() { return userDisplayName; }
        public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    }

    // Inner class for add member requests
    public static class AddMemberRequest {
        private String userId;
        private String role;

        public AddMemberRequest() {}

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    // Inner class for group with member count
    public static class GroupWithMemberCount {
        private UUID id;
        private String name;
        private String description;
        private UUID created_by;
        private String created_at;
        private String updated_at;
        private long member_count;

        public GroupWithMemberCount() {}

        public GroupWithMemberCount(Group group, long memberCount) {
            this.id = group.getId();
            this.name = group.getName();
            this.description = group.getDescription();
            this.created_by = group.getCreatedBy();
            this.created_at = group.getCreatedAt() != null ? group.getCreatedAt().toString() : null;
            this.updated_at = group.getUpdatedAt() != null ? group.getUpdatedAt().toString() : null;
            this.member_count = memberCount;
        }

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public UUID getCreated_by() { return created_by; }
        public void setCreated_by(UUID created_by) { this.created_by = created_by; }
        
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        
        public String getUpdated_at() { return updated_at; }
        public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
        
        public long getMember_count() { return member_count; }
        public void setMember_count(long member_count) { this.member_count = member_count; }
    }
}
