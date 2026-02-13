package com.mustafa.smartfoodfitness.dto;
// Define the UpdateFoodEntryLogsRequest DTO with fields for food entry log details such as food name, weight value, weight unit, calories, proteins, carbs, fats, meal type, and logged timestamp, along with getter and setter methods for each field to facilitate data transfer of food entry log update information between the backend and frontend of the application
import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UpdateFoodEntryLogsRequest {

    @NotBlank
    private String foodName;

    @NotNull
    @Positive
    private Double weightValue;

    @NotBlank
    private String weightUnit;

    @NotNull
    @Min(0)
    private Integer calories;

    @Min(0)
    private Integer proteins;

    @Min(0)
    private Integer carbs;

    @Min(0)
    private Integer fats;

    private String mealType;

    @NotNull
    private Instant loggedAt;

    public String getFoodName() { 
        return foodName; 
    }
    public void setFoodName(String foodName) { 
        this.foodName = foodName; 
    }

    public Double getWeightValue() { 
        return weightValue; 
    }
    public void setWeightValue(Double weightValue) { 
        this.weightValue = weightValue; 
    }

    public String getWeightUnit() { 
        return weightUnit; 
    }
    public void setWeightUnit(String weightUnit) { 
        this.weightUnit = weightUnit; 
    }

    public Integer getCalories() { 
        return calories; 
    }
    public void setCalories(Integer calories) { 
        this.calories = calories; 
    }

    public Integer getProteins() { 
        return proteins; 
    }
    public void setProteins(Integer proteins) { 
        this.proteins = proteins; 
    }

    public Integer getCarbs() { 
        return carbs; 
    }
    public void setCarbs(Integer carbs) { 
        this.carbs = carbs; 
    }

    public Integer getFats() { 
        return fats; 
    }
    public void setFats(Integer fats) { 
        this.fats = fats; 
    }

    public String getMealType() { 
        return mealType; 
    }
    public void setMealType(String mealType) { 
        this.mealType = mealType; 
    }

    public Instant getLoggedAt() { 
        return loggedAt; 
    }
    public void setLoggedAt(Instant loggedAt) { 
        this.loggedAt = loggedAt; 
    }
}
