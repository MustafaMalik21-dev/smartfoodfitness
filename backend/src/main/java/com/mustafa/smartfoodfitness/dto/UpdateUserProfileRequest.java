package com.mustafa.smartfoodfitness.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UpdateUserProfileRequest {

    @NotBlank
    private String displayName;

    @Min(13)
    @Max(120)
    private Integer age;

    @Positive
    private Double heightValue;

    private String heightUnit;

    @Positive
    private Double weightValue;

    private String weightUnit;

    private String gender;

    private String activityLevel;

    private String experienceLevel;

    // ✅ ADD THIS
    private Boolean onboardingComplete;

    public String getDisplayName() { 
        return displayName; 
    }
    public void setDisplayName(String displayName) { 
        this.displayName = displayName; 
    }

    public Integer getAge() { 
        return age; 
    }
    public void setAge(Integer age) { 
        this.age = age; 
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

    public String getGender() { 
        return gender; 
    }
    public void setGender(String gender) { 
        this.gender = gender; 
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

    // ✅ ADD THESE
    public Boolean getOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(Boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }
}
