package com.mustafa.smartfoodfitness.dto;
// Define the NotificationResponse DTO with fields for notification details such as ID, user ID, title, message, notification type, read status, scheduled time, creation time, and read time, along with getter and setter methods for each field to facilitate data transfer of notification information between the backend and frontend of the application
import java.time.Instant;

import com.mustafa.smartfoodfitness.entity.NotificationType;

public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private Boolean isRead;
    private Instant scheduledFor;
    private Instant createdAt;
    private Instant readAt;

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

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

    public Boolean getIsRead() { 
        return isRead; 
    }
    public void setIsRead(Boolean isRead) { 
        this.isRead = isRead; 
    }

    public Instant getScheduledFor() { 
        return scheduledFor; 
    }
    public void setScheduledFor(Instant scheduledFor) { 
        this.scheduledFor = scheduledFor; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }

    public Instant getReadAt() { 
        return readAt; 
    }
    public void setReadAt(Instant readAt) { 
        this.readAt = readAt; 
    }
}
