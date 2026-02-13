package com.mustafa.smartfoodfitness.dto;
// Define the CreateCompletedWorkoutRequest DTO with fields for user ID, workout plan ID, duration in minutes, optional notes, and optional completion time, along with getter and setter methods for each field to facilitate data transfer of completed workout information between the backend and frontend of the application
import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateCompletedWorkoutRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long workoutPlanId;

    @NotNull
    @Positive
    private Integer durationMinutes;

    private String notes;

    private Instant completedAt;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public Long getWorkoutPlanId() { 
        return workoutPlanId; 
    }
    public void setWorkoutPlanId(Long workoutPlanId) { 
        this.workoutPlanId = workoutPlanId; 
    }

    public Integer getDurationMinutes() { 
        return durationMinutes; 
    }
    public void setDurationMinutes(Integer durationMinutes) { 
        this.durationMinutes = durationMinutes; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    public Instant getCompletedAt() { 
        return completedAt; 
    }
    public void setCompletedAt(Instant completedAt) { 
        this.completedAt = completedAt; 
    }
}
