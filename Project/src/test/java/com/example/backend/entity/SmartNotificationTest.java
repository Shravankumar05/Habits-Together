package com.example.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
class SmartNotificationTest {

    private UUID userId;
    private UUID habitId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
    }

    @Test
    void testSmartNotificationCreation() {
        // Given
        String message = "Time to complete your morning exercise!";
        SmartNotification notification = new SmartNotification(userId, habitId, message, SmartNotification.NotificationType.HABIT_REMINDER);
        
        LocalDateTime optimalTiming = LocalDateTime.now().plusHours(1);
        notification.setOptimalTiming(optimalTiming);
        notification.setMotivationLevel(8);
        notification.setResponse(SmartNotification.NotificationResponse.OPENED);

        // Then
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getHabitId()).isEqualTo(habitId);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.HABIT_REMINDER);
        assertThat(notification.getOptimalTiming()).isEqualTo(optimalTiming);
        assertThat(notification.getMotivationLevel()).isEqualTo(8);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.OPENED);
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    void testDefaultConstructor() {
        // Given
        SmartNotification notification = new SmartNotification();

        // Then
        assertThat(notification.getId()).isNull();
        assertThat(notification.getUserId()).isNull();
        assertThat(notification.getHabitId()).isNull();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getNotificationType()).isNull();
        assertThat(notification.getOptimalTiming()).isNull();
        assertThat(notification.getMotivationLevel()).isNull();
        assertThat(notification.getResponse()).isNull();
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    void testNotificationTypeEnum() {
        // Test all notification types
        SmartNotification notification = new SmartNotification();

        notification.setNotificationType(SmartNotification.NotificationType.HABIT_REMINDER);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.HABIT_REMINDER);

        notification.setNotificationType(SmartNotification.NotificationType.MOTIVATIONAL);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.MOTIVATIONAL);

        notification.setNotificationType(SmartNotification.NotificationType.GROUP_ENCOURAGEMENT);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.GROUP_ENCOURAGEMENT);

        notification.setNotificationType(SmartNotification.NotificationType.STREAK_CELEBRATION);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.STREAK_CELEBRATION);

        notification.setNotificationType(SmartNotification.NotificationType.OPTIMAL_TIMING);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.OPTIMAL_TIMING);

        notification.setNotificationType(SmartNotification.NotificationType.FORMATION_MILESTONE);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.FORMATION_MILESTONE);
    }

    @Test
    void testNotificationResponseEnum() {
        // Test all response types
        SmartNotification notification = new SmartNotification();

        notification.setResponse(SmartNotification.NotificationResponse.OPENED);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.OPENED);

        notification.setResponse(SmartNotification.NotificationResponse.DISMISSED);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.DISMISSED);

        notification.setResponse(SmartNotification.NotificationResponse.COMPLETED_HABIT);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.COMPLETED_HABIT);

        notification.setResponse(SmartNotification.NotificationResponse.SNOOZED);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.SNOOZED);

        notification.setResponse(SmartNotification.NotificationResponse.NO_RESPONSE);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.NO_RESPONSE);
    }

    @Test
    void testPrePersistCallback() {
        // Given
        SmartNotification notification = new SmartNotification();
        notification.setUserId(userId);
        notification.setMessage("Test message");

        // When
        notification.onCreate();

        // Then
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getUpdatedAt()).isNotNull();
        assertThat(notification.getCreatedAt()).isEqualTo(notification.getUpdatedAt());
    }

    @Test
    void testPreUpdateCallback() {
        // Given
        SmartNotification notification = new SmartNotification(userId, habitId, "Test", SmartNotification.NotificationType.HABIT_REMINDER);
        LocalDateTime originalUpdatedAt = notification.getUpdatedAt();

        // Wait a bit to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        notification.onUpdate();

        // Then
        assertThat(notification.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void testSettersAndGetters() {
        // Given
        SmartNotification notification = new SmartNotification();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime optimalTiming = now.plusHours(2);
        LocalDateTime sentAt = now.plusMinutes(30);
        String message = "Complete your habit now!";

        // When
        notification.setId(id);
        notification.setUserId(userId);
        notification.setHabitId(habitId);
        notification.setOptimalTiming(optimalTiming);
        notification.setMessage(message);
        notification.setMotivationLevel(9);
        notification.setNotificationType(SmartNotification.NotificationType.MOTIVATIONAL);
        notification.setSentAt(sentAt);
        notification.setResponse(SmartNotification.NotificationResponse.COMPLETED_HABIT);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);

        // Then
        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getHabitId()).isEqualTo(habitId);
        assertThat(notification.getOptimalTiming()).isEqualTo(optimalTiming);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getMotivationLevel()).isEqualTo(9);
        assertThat(notification.getNotificationType()).isEqualTo(SmartNotification.NotificationType.MOTIVATIONAL);
        assertThat(notification.getSentAt()).isEqualTo(sentAt);
        assertThat(notification.getResponse()).isEqualTo(SmartNotification.NotificationResponse.COMPLETED_HABIT);
        assertThat(notification.getCreatedAt()).isEqualTo(now);
        assertThat(notification.getUpdatedAt()).isEqualTo(now);
    }
}