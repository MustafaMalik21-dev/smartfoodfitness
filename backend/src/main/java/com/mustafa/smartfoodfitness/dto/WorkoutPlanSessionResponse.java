package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class WorkoutPlanSessionResponse {

    private Long id;
    private Long workoutPlanId;
    private Integer sessionIndex;
    private String title;
    private String focus;
    private Integer estimatedMinutes;
    private List<WorkoutPlanExerciseItem> exercises;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkoutPlanId() { return workoutPlanId; }
    public void setWorkoutPlanId(Long workoutPlanId) { this.workoutPlanId = workoutPlanId; }

    public Integer getSessionIndex() { return sessionIndex; }
    public void setSessionIndex(Integer sessionIndex) { this.sessionIndex = sessionIndex; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public List<WorkoutPlanExerciseItem> getExercises() { return exercises; }
    public void setExercises(List<WorkoutPlanExerciseItem> exercises) { this.exercises = exercises; }
}
