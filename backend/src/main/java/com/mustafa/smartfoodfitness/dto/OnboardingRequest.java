package com.mustafa.smartfoodfitness.dto;

import jakarta.validation.constraints.NotNull;

public class OnboardingRequest {

  @NotNull
  private Long userId;

  private Integer age;
  private String gender;

  private Double heightValue;
  private String heightUnit;

  @NotNull
  private Double weightValue;

  @NotNull
  private String weightUnit;

  @NotNull
  private Integer calorieGoal;

  @NotNull
  private Integer proteinGoal;

  @NotNull
  private Integer carbGoal;

  @NotNull
  private Integer fatGoal;

  private String activityLevel;
  private String experienceLevel;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public Integer getAge() { 
        return age; 
    }
    public void setAge(Integer age) { 
        this.age = age; 
    }

    public String getGender() { 
        return gender; 
    }
    public void setGender(String gender) { 
        this.gender = gender; 
    }

    public Double getHeightValue() { 
        return heightValue; 
    }
    public void setHeightValue(Double heightValue) { 
        this.heightValue = heightValue; 
    }

    public String getHeightUnit() { 
        return heightUnit; 
    }
    public void setHeightUnit(String heightUnit) { 
        this.heightUnit = heightUnit; 
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

    public String getActivityLevel() { 
        return activityLevel; 
    }
    public void setActivityLevel(String activityLevel) { 
        this.activityLevel = activityLevel; 
    }

    public String getExperienceLevel() { 
        return experienceLevel; 
    }
    public void setExperienceLevel(String experienceLevel) { 
        this.experienceLevel = experienceLevel; 
    }
}
