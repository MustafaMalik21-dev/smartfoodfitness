package com.mustafa.smartfoodfitness.dto;
// Define the CreateNotificationRequest DTO with fields for user ID, title, message, notification type, and optional scheduled time, along with getter and setter methods for each field to facilitate data transfer of notification creation information between the backend and frontend of the application
import java.time.Instant;
import com.mustafa.smartfoodfitness.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateNotificationRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationType notificationType;

    private Instant scheduledFor;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getMessage() { 
        return message; 
    }
    public void setMessage(String message) { 
        this.message = message; 
    }

    public NotificationType getNotificationType() { 
        return notificationType; 
    }
    public void setNotificationType(NotificationType notificationType) { 
        this.notificationType = notificationType; 
    }

    public Instant getScheduledFor() { 
        return scheduledFor; 
    }
    public void setScheduledFor(Instant scheduledFor) { 
        this.scheduledFor = scheduledFor; 
    }
}
