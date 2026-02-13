package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> { // define custom query methods for retrieving notifications based on user profile ID, read status, and scheduled time, allowing for fetching all notifications for a user as well as filtering unread notifications and scheduled notifications that are due when querying the database

    List<Notification> findByUserProfileIdOrderByCreatedAtDesc(Long userId);

    long countByUserProfileIdAndIsReadFalse(Long userId);

    List<Notification> findByUserProfileIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByNotificationTypeAndIsReadFalseAndScheduledForLessThanEqualOrderByScheduledForAsc(
        com.mustafa.smartfoodfitness.entity.NotificationType notificationType,
        Instant now
    );
}
