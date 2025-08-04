package com.example.backend.repository;

import com.example.backend.entity.DailyCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyCompletionRepository extends JpaRepository<DailyCompletion, UUID> {
    List<DailyCompletion> findByUserIdAndCompletionDate(UUID userId, LocalDate completionDate);
    Optional<DailyCompletion> findByHabitIdAndCompletionDate(UUID habitId, LocalDate completionDate);
    Optional<DailyCompletion> findByHabitIdAndUserIdAndCompletionDate(UUID habitId, UUID userId, LocalDate completionDate);
    List<DailyCompletion> findByUserId(UUID userId);
    
    // Calendar functionality for habit completion tracking
    List<DailyCompletion> findByHabitIdAndUserIdAndCompletionDateBetween(
        UUID habitId, UUID userId, LocalDate startDate, LocalDate endDate);
}
