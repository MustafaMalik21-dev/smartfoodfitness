package com.mustafa.smartfoodfitness.dto;
// Define the CompletedWorkoutResponse DTO with fields for ID, user ID, workout plan ID, workout plan title, completion time, duration in minutes, and notes, along with getter and setter methods for each field to facilitate data transfer of completed workout information from the backend to the frontend of the application
import java.time.Instant;

public class CompletedWorkoutResponse {

    private Long id;
    private Long userId;
    private Long workoutPlanId;
    private String workoutPlanTitle;
    private Instant completedAt;
    private Integer durationMinutes;
    private String notes;

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

    public Long getWorkoutPlanId() { 
        return workoutPlanId; 
    }
    public void setWorkoutPlanId(Long workoutPlanId) { 
        this.workoutPlanId = workoutPlanId; 
    }

    public String getWorkoutPlanTitle() { 
        return workoutPlanTitle; 
    }
    public void setWorkoutPlanTitle(String workoutPlanTitle) { 
        this.workoutPlanTitle = workoutPlanTitle; 
    }

    public Instant getCompletedAt() { 
        return completedAt; 
    }
    public void setCompletedAt(Instant completedAt) { 
        this.completedAt = completedAt; 
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
}
