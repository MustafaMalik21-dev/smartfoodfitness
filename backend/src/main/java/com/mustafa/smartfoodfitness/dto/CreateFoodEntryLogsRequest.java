package com.mustafa.smartfoodfitness.dto;
// Define the CreateFoodEntryLogsRequest DTO with fields for user ID, food name, weight value and unit, calories, proteins, carbs, fats, meal type, and logged time, along with getter and setter methods for each field to facilitate data transfer of food entry log creation information between the backend and frontend of the application
import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateFoodEntryLogsRequest {

    @NotNull
    private Long userId;

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

    // Micronutrients — all optional
    private Double fiberG;
    private Double sugarG;
    private Double sodiumMg;
    private Double potassiumMg;
    private Double cholesterolMg;
    private Double saturatedFatG;
    private Double vitaminAMcg;
    private Double vitaminCMg;
    private Double vitaminDMcg;
    private Double calciumMg;
    private Double ironMg;
    private Double zincMg;

    private String mealType;

    @NotNull
    private Instant loggedAt;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public String getFoodName() { 
        return foodName; 
    }
    public void setFoodName(String foodName) { 
        this.foodName = foodName; }

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

    public Double getFiberG() { return fiberG; }
    public void setFiberG(Double fiberG) { this.fiberG = fiberG; }

    public Double getSugarG() { return sugarG; }
    public void setSugarG(Double sugarG) { this.sugarG = sugarG; }

    public Double getSodiumMg() { return sodiumMg; }
    public void setSodiumMg(Double sodiumMg) { this.sodiumMg = sodiumMg; }

    public Double getPotassiumMg() { return potassiumMg; }
    public void setPotassiumMg(Double potassiumMg) { this.potassiumMg = potassiumMg; }

    public Double getCholesterolMg() { return cholesterolMg; }
    public void setCholesterolMg(Double cholesterolMg) { this.cholesterolMg = cholesterolMg; }

    public Double getSaturatedFatG() { return saturatedFatG; }
    public void setSaturatedFatG(Double saturatedFatG) { this.saturatedFatG = saturatedFatG; }

    public Double getVitaminAMcg() { return vitaminAMcg; }
    public void setVitaminAMcg(Double vitaminAMcg) { this.vitaminAMcg = vitaminAMcg; }

    public Double getVitaminCMg() { return vitaminCMg; }
    public void setVitaminCMg(Double vitaminCMg) { this.vitaminCMg = vitaminCMg; }

    public Double getVitaminDMcg() { return vitaminDMcg; }
    public void setVitaminDMcg(Double vitaminDMcg) { this.vitaminDMcg = vitaminDMcg; }

    public Double getCalciumMg() { return calciumMg; }
    public void setCalciumMg(Double calciumMg) { this.calciumMg = calciumMg; }

    public Double getIronMg() { return ironMg; }
    public void setIronMg(Double ironMg) { this.ironMg = ironMg; }

    public Double getZincMg() { return zincMg; }
    public void setZincMg(Double zincMg) { this.zincMg = zincMg; }

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
