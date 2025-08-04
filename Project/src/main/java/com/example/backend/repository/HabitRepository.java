package com.example.backend.repository;

import com.example.backend.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {
    List<Habit> findByUserId(UUID userId);
    List<Habit> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
