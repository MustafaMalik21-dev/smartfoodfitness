package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// Define the FoodEntryLogs entity with fields for user profile reference, food name, weight value and unit, calories, macronutrients, meal type, logged timestamp, and timestamps for creation and updates, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "food_entry_logs")
public class FoodEntryLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private Double weightValue;

    @Column(nullable = false)
    private String weightUnit;

    @Column(nullable = false)
    private Integer calories;

    private Integer proteins;
    private Integer carbs;
    private Integer fats;

    // Micronutrients — all nullable (only present when data is available)
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

    @Column(nullable = false)
    private Instant loggedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { 
        return id; 
    }

    public UserProfile getUserProfile() { 
        return userProfile; 
    }
    public void setUserProfile(UserProfile userProfile) { 
        this.userProfile = userProfile; 
    }

    public String getFoodName() { 
        return foodName; 
    }
    public void setFoodName(String foodName) { 
        this.foodName = foodName; 
    }

    public Double getWeightValue() { return weightValue; }
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
