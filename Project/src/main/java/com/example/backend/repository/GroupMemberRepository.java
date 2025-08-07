package com.example.backend.repository;

import com.example.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    
    // Find all members of a specific group
    List<GroupMember> findByGroupId(UUID groupId);
    
    // Find a specific member in a specific group
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
    
    // Find all groups a user is a member of
    List<GroupMember> findByUserId(UUID userId);
    
    // Check if a user is a member of a group
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
    
    // Count members in a group
    long countByGroupId(UUID groupId);
    
    // Find members by role
    List<GroupMember> findByGroupIdAndRole(UUID groupId, String role);
    
    // Count members by group and role
    long countByGroupIdAndRole(UUID groupId, String role);
    
    // Delete member by group and user ID
    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.groupId = :groupId AND gm.userId = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Custom query to get member details with user information
    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupId = :groupId ORDER BY gm.joinedAt")
    List<GroupMember> findGroupMembersOrderedByJoinDate(@Param("groupId") UUID groupId);
}
