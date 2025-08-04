package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "daily_completions", schema = "public")
public class DailyCompletion {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(name = "habit_id", nullable = false)
    private UUID habitId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "completion_date", nullable = false, columnDefinition = "date default CURRENT_DATE")
    private LocalDate completionDate;

    @Column(columnDefinition = "boolean default true")
    private Boolean completed = true;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "completed_at", columnDefinition = "timestamp with time zone default now()")
    private LocalDateTime completedAt;

    public DailyCompletion() {}

    public DailyCompletion(UUID habitId, UUID userId, LocalDate completionDate) {
        this.habitId = habitId;
        this.userId = userId;
        this.completionDate = completionDate;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getHabitId() { return habitId; }
    public void setHabitId(UUID habitId) { this.habitId = habitId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
        if (completionDate == null) {
            completionDate = LocalDate.now();
        }
    }
}
