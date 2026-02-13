package com.mustafa.smartfoodfitness.dto;
// Define the UpdateSelectedWorkoutPlanRequest DTO with a field for workout plan ID, along with getter and setter methods for the field to facilitate data transfer of selected workout plan update information between the backend and frontend of the application
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UpdateSelectedWorkoutPlanRequest {

    @NotNull(message = "workoutPlanId is required.")
    @Positive(message = "workoutPlanId must be greater than 0.")
    private Long workoutPlanId;

    public Long getWorkoutPlanId() { 
        return workoutPlanId; 
    }
    public void setWorkoutPlanId(Long workoutPlanId) { 
        this.workoutPlanId = workoutPlanId; 
    }
}
