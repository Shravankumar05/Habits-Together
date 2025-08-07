package com.example.backend.repository;

import com.example.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    
    // Find groups where the user is a member
    @Query("SELECT DISTINCT g FROM Group g JOIN GroupMember gm ON g.id = gm.groupId WHERE gm.userId = :userId")
    List<Group> findGroupsByUserId(@Param("userId") UUID userId);
    
    // Find groups created by a specific user
    List<Group> findByCreatedBy(UUID createdBy);
    
    // Find groups by name (case-insensitive)
    List<Group> findByNameContainingIgnoreCase(String name);
}
