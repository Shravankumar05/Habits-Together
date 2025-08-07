package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_habit_completions")
public class GroupHabitCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "group_habit_id", nullable = false)
    private UUID groupHabitId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    @Column(columnDefinition = "boolean default true")
    private Boolean completed = true;

    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Constructors
    public GroupHabitCompletion() {}

    public GroupHabitCompletion(UUID groupHabitId, UUID userId, LocalDate completionDate, Boolean completed) {
        this.groupHabitId = groupHabitId;
        this.userId = userId;
        this.completionDate = completionDate != null ? completionDate : LocalDate.now();
        this.completed = completed != null ? completed : true;
        this.completedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupHabitId() {
        return groupHabitId;
    }

    public void setGroupHabitId(UUID groupHabitId) {
        this.groupHabitId = groupHabitId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (completionDate == null) {
            completionDate = LocalDate.now();
        }
        completedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        completedAt = LocalDateTime.now();
    }
}
