package com.mustafa.smartfoodfitness.dto;

// Define the WorkoutLogResponse DTO with fields for workout log details such as ID, user ID, workout name, type, duration, performed date, notes, details in JSON format, and timestamps for creation and update, along with getter and setter methods for each field to facilitate data transfer of workout log information between the backend and frontend of the application

import java.time.Instant;

public class WorkoutLogResponse {

    private Long id;
    private Long userId;

    private String workoutName;
    private String workoutType;
    private Integer durationMinutes;

    private Instant performedAt;

    private String notes;

    private String detailsJson;

    private Instant createdAt;
    private Instant updatedAt;

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

    public String getWorkoutName() { 
        return workoutName; 
    }
    public void setWorkoutName(String workoutName) { 
        this.workoutName = workoutName; 
    }

    public String getWorkoutType() { 
        return workoutType; 
    }
    public void setWorkoutType(String workoutType) { 
        this.workoutType = workoutType; 
    }

    public Integer getDurationMinutes() { 
        return durationMinutes; 
    }
    public void setDurationMinutes(Integer durationMinutes) { 
        this.durationMinutes = durationMinutes; 
    }

    public Instant getPerformedAt() { 
        return performedAt; 
    }
    public void setPerformedAt(Instant performedAt) { 
        this.performedAt = performedAt; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    public String getDetailsJson() { 
        return detailsJson; 
    }
    public void setDetailsJson(String detailsJson) { 
        this.detailsJson = detailsJson; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }

    public Instant getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(Instant updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}
