package com.example.backend.repository;

import com.example.backend.entity.GroupHabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupHabitCompletionRepository extends JpaRepository<GroupHabitCompletion, UUID> {
    
    // Find completion for a specific habit, user, and date
    Optional<GroupHabitCompletion> findByGroupHabitIdAndUserIdAndCompletionDate(
        UUID groupHabitId, UUID userId, LocalDate completionDate);
    
    // Find all completions for a specific group habit on a date
    List<GroupHabitCompletion> findByGroupHabitIdAndCompletionDate(UUID groupHabitId, LocalDate completionDate);
    
    // Find all completions for a user on a specific date
    List<GroupHabitCompletion> findByUserIdAndCompletionDate(UUID userId, LocalDate completionDate);
    
    // Find all completions for a specific group habit
    List<GroupHabitCompletion> findByGroupHabitId(UUID groupHabitId);
    
    // Find completions for all habits in a group on a specific date
    @Query("SELECT ghc FROM GroupHabitCompletion ghc JOIN GroupHabit gh ON ghc.groupHabitId = gh.id WHERE gh.groupId = :groupId AND ghc.completionDate = :completionDate")
    List<GroupHabitCompletion> findByGroupIdAndCompletionDate(@Param("groupId") UUID groupId, @Param("completionDate") LocalDate completionDate);
    
    // Count completed habits for a user on a specific date in a group
    @Query("SELECT COUNT(ghc) FROM GroupHabitCompletion ghc JOIN GroupHabit gh ON ghc.groupHabitId = gh.id WHERE gh.groupId = :groupId AND ghc.userId = :userId AND ghc.completionDate = :date AND ghc.completed = true")
    long countCompletedHabitsForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId, @Param("date") LocalDate date);
    
    // Get completion statistics for a group habit over a date range
    @Query("SELECT ghc FROM GroupHabitCompletion ghc WHERE ghc.groupHabitId = :habitId AND ghc.completionDate BETWEEN :startDate AND :endDate ORDER BY ghc.completionDate DESC")
    List<GroupHabitCompletion> findCompletionsByHabitAndDateRange(@Param("habitId") UUID habitId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
