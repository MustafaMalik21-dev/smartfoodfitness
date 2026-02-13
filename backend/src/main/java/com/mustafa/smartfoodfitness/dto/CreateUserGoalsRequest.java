package com.mustafa.smartfoodfitness.dto;
// Define the CreateUserGoalsRequest DTO with fields for user ID, calorie goal, protein goal, carb goal, and fat goal, along with getter and setter methods for each field to facilitate data transfer of user goals information between the backend and frontend of the application
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateUserGoalsRequest {

    @NotNull
    private Long userId;

    @NotNull
    @Min(800)
    @Max(6000)
    private Integer calorieGoal;

    @NotNull
    @Min(0)
    @Max(500)
    private Integer proteinGoal;

    @NotNull
    @Min(0)
    @Max(700)
    private Integer carbGoal;

    @NotNull
    @Min(0)
    @Max(300)
    private Integer fatGoal;

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
}
