package com.mustafa.smartfoodfitness.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Inputs for AI workout-plan generation. The exercise menu is built client-side
 * from the app's bundled exercise database; the backend assembles the prompt and
 * holds the Anthropic API key.
 */
public class GeneratePlanRequest {
    // Null is allowed — the service defaults to 3 — but a supplied value must be a real training week.
    @Min(value = 1, message = "Days per week must be between 1 and 7.")
    @Max(value = 7, message = "Days per week must be between 1 and 7.")
    private Integer daysPerWeek;

    private String equipment;
    private String experienceLevel;
    private String activityLevel;
    private List<String> aims;
    private String exerciseMenu;

    public Integer getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(Integer daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public List<String> getAims() { return aims; }
    public void setAims(List<String> aims) { this.aims = aims; }

    public String getExerciseMenu() { return exerciseMenu; }
    public void setExerciseMenu(String exerciseMenu) { this.exerciseMenu = exerciseMenu; }
}
