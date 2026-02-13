package com.mustafa.smartfoodfitness.dto;
// Define the WorkoutPlanResponse DTO with fields for workout plan details such as ID, title, level, goal, split, days per week, estimated duration, short description, 
// pros, and cons, along with getter and setter methods for each field to facilitate data transfer of workout plan information between the backend and frontend of the 
// application
public class WorkoutPlanResponse {

    private Long id;

    private String title;
    private String level;
    private String goal;
    private String split;

    private Integer daysPerWeek;
    private Integer estimatedDurationMinutes;

    private String shortDescription;
    private String pros;
    private String cons;

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getLevel() { 
        return level; 
    }
    public void setLevel(String level) { 
        this.level = level; 
    }

    public String getGoal() { 
        return goal; 
    }
    public void setGoal(String goal) {
        this.goal = goal; 
    }

    public String getSplit() { 
        return split; 
    }
    public void setSplit(String split) { 
        this.split = split; 
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek; 
    }
    public void setDaysPerWeek(Integer daysPerWeek) { 
        this.daysPerWeek = daysPerWeek; 
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes; 
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { 
        this.estimatedDurationMinutes = estimatedDurationMinutes; 
    }

    public String getShortDescription() {
        return shortDescription; 
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription; 
    }

    public String getPros() {
        return pros; 
    }
    public void setPros(String pros) { 
        this.pros = pros; 
    }

    public String getCons() {
        return cons; 
    }
    public void setCons(String cons) {
        this.cons = cons; 
    }
}
