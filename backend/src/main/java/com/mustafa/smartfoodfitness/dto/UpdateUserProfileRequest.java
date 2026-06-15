package com.mustafa.smartfoodfitness.dto;
// Define the UpdateUserProfileRequest DTO with fields for display name, age, height value and unit, weight
import java.util.Set;
public class UpdateUserProfileRequest {

    private String displayName;


    private Integer age;

    private Double heightValue;

    private String heightUnit;

    private Double weightValue;

    private String weightUnit;

    private String gender;

    private String activityLevel;

    private String experienceLevel;

    private Boolean onboardingComplete;

    private String bodyType;

    private Set<String> aims;



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

    public Boolean getOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(Boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }
    public String getBodyType() {
        return bodyType;
    }
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public Set<String> getAims() {
        return aims;
    }
    public void setAims(Set<String> aims) {
        this.aims = aims;
    }

    private String profileVisibility;
    private Boolean shareWeight;
    private Boolean shareActivity;

    public String getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(String v) { this.profileVisibility = v; }

    public Boolean getShareWeight() { return shareWeight; }
    public void setShareWeight(Boolean v) { this.shareWeight = v; }

    public Boolean getShareActivity() { return shareActivity; }
    public void setShareActivity(Boolean v) { this.shareActivity = v; }
}
