package com.mustafa.smartfoodfitness.dto;
// Define the CreateWorkoutLogRequest DTO with fields for user ID, workout name, workout type, duration in minutes, performed time, notes, and details in JSON format, along with getter and setter methods for each field to facilitate data transfer of workout log creation information between the backend and frontend of the application
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateWorkoutLogRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String workoutName;

    @NotBlank
    private String workoutType;

    @Positive
    private Integer durationMinutes;

    @NotNull
    private Instant performedAt;

    private String notes;

    private String detailsJson;

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
}
