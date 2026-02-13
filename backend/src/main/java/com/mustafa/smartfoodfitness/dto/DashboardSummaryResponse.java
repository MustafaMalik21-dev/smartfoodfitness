package com.mustafa.smartfoodfitness.dto;
// Define the DashboardSummaryResponse DTO with fields for user ID, date range, nutrition summary details (total calories, proteins, carbs, fats) compared to goals, current streak days, total workouts, and workouts in the last 7 days, along with getter and setter methods for each field to facilitate data transfer of dashboard summary information between the backend and frontend of the application
import java.time.Instant;

public class DashboardSummaryResponse {

    private Long userId;

    private String timezone;
    private String date;

    private Instant from;
    private Instant to;

    private Integer entriesCount;

    private Integer totalCalories;
    private Integer caloriesGoal;
    private Integer caloriesRemaining;
    private Integer caloriesPercent;

    private Integer totalProteins;
    private Integer proteinsGoal;
    private Integer proteinsRemaining;
    private Integer proteinsPercent;

    private Integer totalCarbs;
    private Integer carbsGoal;
    private Integer carbsRemaining;
    private Integer carbsPercent;

    private Integer totalFats;
    private Integer fatsGoal;
    private Integer fatsRemaining;
    private Integer fatsPercent;

    private Integer currentStreakDays;
    private Long totalWorkouts;
    private Long workoutsLast7Days;


    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public String getTimezone() { 
        return timezone; 
    }
    public void setTimezone(String timezone) { 
        this.timezone = timezone; 
    }

    public String getDate() { 
        return date; 
    }
    public void setDate(String date) { 
        this.date = date; 
    }

    public Instant getFrom() { 
        return from; 
    }
    public void setFrom(Instant from) { 
        this.from = from; 
    }

    public Instant getTo() { 
        return to; 
    }
    public void setTo(Instant to) { 
        this.to = to; 
    }

    public Integer getEntriesCount() { 
        return entriesCount; 
    }
    public void setEntriesCount(Integer entriesCount) { 
        this.entriesCount = entriesCount; 
    }

    public Integer getTotalCalories() { 
        return totalCalories; 
    }
    public void setTotalCalories(Integer totalCalories) { 
        this.totalCalories = totalCalories; 
    }

    public Integer getCaloriesGoal() { 
        return caloriesGoal; 
    }
    public void setCaloriesGoal(Integer caloriesGoal) { 
        this.caloriesGoal = caloriesGoal; 
    }

    public Integer getCaloriesRemaining() { 
        return caloriesRemaining; 
    }
    public void setCaloriesRemaining(Integer caloriesRemaining) { 
        this.caloriesRemaining = caloriesRemaining; 
    }

    public Integer getCaloriesPercent() { 
        return caloriesPercent; 
    }
    public void setCaloriesPercent(Integer caloriesPercent) { 
        this.caloriesPercent = caloriesPercent; 
    }

    public Integer getTotalProteins() { 
        return totalProteins; 
    }
    public void setTotalProteins(Integer totalProteins) { 
        this.totalProteins = totalProteins; 
    }

    public Integer getProteinsGoal() { 
        return proteinsGoal; 
    }
    public void setProteinsGoal(Integer proteinsGoal) { 
        this.proteinsGoal = proteinsGoal; 
    }

    public Integer getProteinsRemaining() { 
        return proteinsRemaining; 
    }
    public void setProteinsRemaining(Integer proteinsRemaining) { 
        this.proteinsRemaining = proteinsRemaining; 
    }

    public Integer getProteinsPercent() { 
        return proteinsPercent; 
    }
    public void setProteinsPercent(Integer proteinsPercent) { 
        this.proteinsPercent = proteinsPercent; 
    }

    public Integer getTotalCarbs() { 
        return totalCarbs; 
    }
    public void setTotalCarbs(Integer totalCarbs) { 
        this.totalCarbs = totalCarbs; 
    }

    public Integer getCarbsGoal() { 
        return carbsGoal; 
    }
    public void setCarbsGoal(Integer carbsGoal) { 
        this.carbsGoal = carbsGoal; 
    }

    public Integer getCarbsRemaining() { 
        return carbsRemaining; 
    }
    public void setCarbsRemaining(Integer carbsRemaining) { 
        this.carbsRemaining = carbsRemaining; 
    }

    public Integer getCarbsPercent() { 
        return carbsPercent; 
    }
    public void setCarbsPercent(Integer carbsPercent) { 
        this.carbsPercent = carbsPercent; 
    }

    public Integer getTotalFats() { 
        return totalFats; 
    }
    public void setTotalFats(Integer totalFats) { 
        this.totalFats = totalFats; 
    }

    public Integer getFatsGoal() { 
        return fatsGoal; 
    }
    public void setFatsGoal(Integer fatsGoal) { 
        this.fatsGoal = fatsGoal; 
    }

    public Integer getFatsRemaining() { 
        return fatsRemaining; 
    }
    public void setFatsRemaining(Integer fatsRemaining) { 
        this.fatsRemaining = fatsRemaining; 
    }

    public Integer getFatsPercent() { 
        return fatsPercent; 
    }
    public void setFatsPercent(Integer fatsPercent) { 
        this.fatsPercent = fatsPercent; 
    }

    public Integer getCurrentStreakDays() { 
        return currentStreakDays; 
    }
    public void setCurrentStreakDays(Integer currentStreakDays) { 
        this.currentStreakDays = currentStreakDays; 
    }

    public Long getTotalWorkouts() { 
        return totalWorkouts; 
    }
    public void setTotalWorkouts(Long totalWorkouts) { 
        this.totalWorkouts = totalWorkouts; 
    }

    public Long getWorkoutsLast7Days() { 
        return workoutsLast7Days; 
    }
    public void setWorkoutsLast7Days(Long workoutsLast7Days) { 
        this.workoutsLast7Days = workoutsLast7Days; 
    }

}
