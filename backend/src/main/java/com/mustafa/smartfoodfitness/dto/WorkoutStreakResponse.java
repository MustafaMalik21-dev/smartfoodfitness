package com.mustafa.smartfoodfitness.dto;
// Define the WorkoutStreakResponse DTO with fields for user ID, timezone, base date, current streak days, total workouts, and workouts in the last 7 days, along with getter and setter methods for each field to facilitate data transfer of workout streak information between the backend and frontend of the application
public class WorkoutStreakResponse {

    private Long userId;

    private String timezone;
    private String baseDate;

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

    public String getBaseDate() { 
        return baseDate; 
    }
    public void setBaseDate(String baseDate) { 
        this.baseDate = baseDate; 
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
