package com.mustafa.smartfoodfitness.dto;
// Define the CreateUserProfileRequest DTO with fields for email, display name, age, height value and unit, weight value and
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateUserProfileRequest {

    @NotBlank
    @Email
    private String email;

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

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

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
}
