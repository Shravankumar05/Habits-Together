package com.example.backend.repository;

import com.example.backend.entity.GroupHabit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupHabitRepository extends JpaRepository<GroupHabit, UUID> {
    
    // Find all habits for a specific group
    List<GroupHabit> findByGroupId(UUID groupId);
    
    // Find habits created by a specific user
    List<GroupHabit> findByCreatedBy(UUID createdBy);
    
    // Find habits by name in a specific group
    @Query("SELECT gh FROM GroupHabit gh WHERE gh.groupId = :groupId AND LOWER(gh.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<GroupHabit> findByGroupIdAndNameContainingIgnoreCase(@Param("groupId") UUID groupId, @Param("name") String name);
    
    // Count habits in a group
    long countByGroupId(UUID groupId);
    
    // Find habits by group and creator
    List<GroupHabit> findByGroupIdAndCreatedBy(UUID groupId, UUID createdBy);
}
