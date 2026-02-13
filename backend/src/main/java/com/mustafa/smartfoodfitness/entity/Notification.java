package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// Define the Notification entity with fields for user profile reference, title, message, notification type, read status, scheduled timestamp, created timestamp, and read timestamp, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private Boolean isRead;

    private Instant scheduledFor;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;

    public Long getId() { 
        return id; 
    }

    public UserProfile getUserProfile() { 
        return userProfile; 
    }
    public void setUserProfile(UserProfile userProfile) { 
        this.userProfile = userProfile; 
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
        this.createdAt = createdAt; }

    public Instant getReadAt() { 
        return readAt; 
    }
    public void setReadAt(Instant readAt) { 
        this.readAt = readAt; 
    }
}
