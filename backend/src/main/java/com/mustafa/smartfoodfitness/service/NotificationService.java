package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateNotificationRequest;
import com.mustafa.smartfoodfitness.dto.MarkNotificationReadRequest;
import com.mustafa.smartfoodfitness.dto.NotificationResponse;
import com.mustafa.smartfoodfitness.entity.Notification;
import com.mustafa.smartfoodfitness.entity.NotificationType;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.NotificationRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserProfileRepository userProfileRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public NotificationResponse createNotification(CreateNotificationRequest request) { // create a new notification for a user, validating the input and ensuring that the associated user profile exists, then saving the new notification to the database and returning a response DTO representing the saved notification
        Long userId = request.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }

        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        NotificationType type = request.getNotificationType(); // validate that the notification type is provided in the request, and if the type is REMINDER, also validate that the scheduledFor timestamp is provided since it is required for reminders, throwing a 400 Bad Request error if any of these validations fail
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notificationType is required.");
        }

        if (type == NotificationType.REMINDER && request.getScheduledFor() == null) { 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledFor is required for REMINDER.");
        }

        Notification n = new Notification(); // create a new Notification entity and populate it with data from the request, including setting the user profile, title, message, notification type, read status (defaulting to false), scheduled timestamp (if provided), and createdAt timestamp before saving it to the database and returning a response DTO representing the saved notification
        n.setUserProfile(user);
        n.setTitle(request.getTitle());
        n.setMessage(request.getMessage());
        n.setNotificationType(type);
        n.setIsRead(false);
        n.setScheduledFor(request.getScheduledFor());
        n.setCreatedAt(Instant.now());

        Notification saved = notificationRepository.save(n);
        return toResponse(saved);
    }

    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserProfileIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(Long userId) { // return the count of unread notifications for a user by querying the database for notifications associated with the user's profile that have an isRead status of false, effectively providing a way to determine how many notifications the user has that they have not yet marked as read
        return notificationRepository.countByUserProfileIdAndIsReadFalse(userId);
    }

    public NotificationResponse markRead(Long notificationId, MarkNotificationReadRequest request) { // mark a notification as read or unread based on the provided notification ID and request data, validating the input and ensuring that the notification exists, then updating the read status and read timestamp accordingly before saving the updated notification to the database and returning a response DTO representing the updated notification
        if (notificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notificationId is required.");
        }

        Notification n = notificationRepository.findById(notificationId) // fetch the notification to be updated from the database using the provided notification ID, throwing a 404 Not Found error if it does not exist
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));
        com.mustafa.smartfoodfitness.auth.security.AuthGuard.requireSelf(n.getUserProfile().getId());

        Boolean isRead = request.getIsRead(); // validate that the isRead field is provided in the request, throwing a 400 Bad Request error if it is not provided since it is required to determine whether to mark the notification as read or unread
        if (isRead == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isRead is required.");
        }

        n.setIsRead(isRead);
        n.setReadAt(isRead ? Instant.now() : null);

        Notification saved = notificationRepository.save(n); // save the updated notification to the database and return a response DTO representing the updated notification
        return toResponse(saved);
    }

    public List<NotificationResponse> getDueReminders() { // retrieve a list of due reminder notifications by querying the database for notifications that have a notification type of REMINDER, an isRead status of false, and a scheduledFor timestamp that is less than or equal to the current time, effectively providing a way to get all reminders that are currently due and have not yet been marked as read, then mapping the retrieved notifications to response DTOs before returning the list
        Instant now = Instant.now();
        return notificationRepository
                .findByNotificationTypeAndIsReadFalseAndScheduledForLessThanEqualOrderByScheduledForAsc(
                        NotificationType.REMINDER,
                        now
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification n) { // helper method to convert a Notification entity to a NotificationResponse DTO by creating a new NotificationResponse instance and populating it with data from the Notification entity, including the ID, user ID, title, message, notification type, read status, scheduled timestamp, createdAt timestamp, and readAt timestamp before returning the populated DTO
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setUserId(n.getUserProfile().getId());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setNotificationType(n.getNotificationType());
        r.setIsRead(n.getIsRead());
        r.setScheduledFor(n.getScheduledFor());
        r.setCreatedAt(n.getCreatedAt());
        r.setReadAt(n.getReadAt());
        return r;
    }
}
