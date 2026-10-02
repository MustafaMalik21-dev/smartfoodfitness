package com.mustafa.smartfoodfitness.dto;
// Define the UserDataExportResponse DTO carrying a full copy of everything the authenticated user owns — profile, goals, food entry logs, weight entries, water entries, workout logs, completed workouts, notifications and messages — as a single JSON document, along with getter and setter methods for each field to facilitate a data-portability download from the backend to the client
import java.time.Instant;
import java.util.List;

public class UserDataExportResponse {

    private Instant generatedAt;

    private UserProfileResponse profile;
    private UserGoalsResponse goals;

    private List<FoodEntryLogsResponse> foodEntryLogs;
    private List<WeightEntryResponse> weightEntries;
    private List<WaterEntryResponse> waterEntries;
    private List<WorkoutLogResponse> workoutLogs;
    private List<CompletedWorkoutResponse> completedWorkouts;
    private List<NotificationResponse> notifications;
    private List<MessageDto> messages;

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public UserProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(UserProfileResponse profile) {
        this.profile = profile;
    }

    public UserGoalsResponse getGoals() {
        return goals;
    }

    public void setGoals(UserGoalsResponse goals) {
        this.goals = goals;
    }

    public List<FoodEntryLogsResponse> getFoodEntryLogs() {
        return foodEntryLogs;
    }

    public void setFoodEntryLogs(List<FoodEntryLogsResponse> foodEntryLogs) {
        this.foodEntryLogs = foodEntryLogs;
    }

    public List<WeightEntryResponse> getWeightEntries() {
        return weightEntries;
    }

    public void setWeightEntries(List<WeightEntryResponse> weightEntries) {
        this.weightEntries = weightEntries;
    }

    public List<WaterEntryResponse> getWaterEntries() {
        return waterEntries;
    }

    public void setWaterEntries(List<WaterEntryResponse> waterEntries) {
        this.waterEntries = waterEntries;
    }

    public List<WorkoutLogResponse> getWorkoutLogs() {
        return workoutLogs;
    }

    public void setWorkoutLogs(List<WorkoutLogResponse> workoutLogs) {
        this.workoutLogs = workoutLogs;
    }

    public List<CompletedWorkoutResponse> getCompletedWorkouts() {
        return completedWorkouts;
    }

    public void setCompletedWorkouts(List<CompletedWorkoutResponse> completedWorkouts) {
        this.completedWorkouts = completedWorkouts;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
