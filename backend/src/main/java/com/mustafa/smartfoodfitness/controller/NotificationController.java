package com.mustafa.smartfoodfitness.controller;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.CreateNotificationRequest;
import com.mustafa.smartfoodfitness.dto.MarkNotificationReadRequest;
import com.mustafa.smartfoodfitness.dto.NotificationResponse;
import com.mustafa.smartfoodfitness.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications") // define a REST controller for handling HTTP requests related to notifications, with a base URL of "/api/notifications" to group all notification-related endpoints together and allow for organized routing within the application
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping // handle HTTP POST requests to create a new notification, accepting a request body containing the details of the notification to be created, validating the input data, and returning a response DTO representing the created notification to the client when they access the relevant endpoint in the application
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        AuthGuard.requireSelf(request.getUserId());
        return notificationService.createNotification(request);
    }

    @GetMapping // handle HTTP GET requests to retrieve notifications for a specific user identified by their user ID, validating that the user ID is provided and returning a list of response DTOs representing the notifications for the user when they access the relevant endpoint in the application
    public List<NotificationResponse> listForUser(@RequestParam @NonNull Long userId) {
        AuthGuard.requireSelf(userId);
        return notificationService.getNotificationsForUser(userId);
    }

    @GetMapping("/unread-count") // handle HTTP GET requests to retrieve the count of unread notifications for a specific user identified by their user ID, validating that the user ID is provided and returning the count of unread notifications to the client when they access the relevant endpoint in the application
    public long unreadCount(@RequestParam @NonNull Long userId) {
        AuthGuard.requireSelf(userId);
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{notificationId}/read") // handle HTTP PUT requests to mark a specific notification as read, identified by its notification ID, accepting a request body containing any additional details needed to mark the notification as read, validating the input data, and returning a response DTO representing the updated notification to the client when they access the relevant endpoint in the application
    public NotificationResponse markRead(
            @PathVariable @NonNull Long notificationId,
            @Valid @RequestBody MarkNotificationReadRequest request
    ) {
        return notificationService.markRead(notificationId, request);
    }

    @GetMapping("/due-reminders") // handle HTTP GET requests to retrieve notifications that are due reminders, returning a list of response DTOs representing the due reminder notifications to the client when they access the relevant endpoint in the application
    public List<NotificationResponse> dueReminders() {
        // Scoped to the authenticated user — previously returned every user's reminders.
        return notificationService.getDueReminders().stream()
                .filter(n -> n.getUserId() != null && n.getUserId().equals(AuthGuard.currentUserId()))
                .toList();
    }
}
