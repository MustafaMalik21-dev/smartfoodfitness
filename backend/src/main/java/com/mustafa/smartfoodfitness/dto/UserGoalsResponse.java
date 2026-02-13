package com.mustafa.smartfoodfitness.dto;
// Define the UserGoalsResponse DTO with fields for user goals details such as ID, user ID, calorie goal, protein goal, carb goal, fat goal, created timestamp, and updated timestamp, along with getter and setter methods for each field to facilitate data transfer of user goals information between the backend and frontend of the application
import java.time.Instant;

public class UserGoalsResponse {

    private Long id;
    private Long userId;
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbGoal;
    private Integer fatGoal;
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

    public Integer getCalorieGoal() {
        return calorieGoal;
    }

    public void setCalorieGoal(Integer calorieGoal) {
        this.calorieGoal = calorieGoal;
    }

    public Integer getProteinGoal() {
        return proteinGoal;
    }

    public void setProteinGoal(Integer proteinGoal) {
        this.proteinGoal = proteinGoal;
    }

    public Integer getCarbGoal() {
        return carbGoal;
    }

    public void setCarbGoal(Integer carbGoal) {
        this.carbGoal = carbGoal;
    }

    public Integer getFatGoal() {
        return fatGoal;
    }

    public void setFatGoal(Integer fatGoal) {
        this.fatGoal = fatGoal;
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
