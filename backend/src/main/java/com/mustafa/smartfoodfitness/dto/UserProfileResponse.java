package com.mustafa.smartfoodfitness.dto;
// Define the UserProfileResponse DTO with fields for user profile details such as ID, email, display name, age, height, weight
import java.time.Instant;
import java.util.Set;

public class UserProfileResponse {

    private Long id;
    private String email;
    private String displayName;

    private Integer age;

    private Double heightValue;
    private String heightUnit;

    private Double weightValue;
    private String weightUnit;

    private String gender;
    private String activityLevel;

    private Instant createdAt;
    private Instant updatedAt;

    private Long selectedWorkoutPlanId;
    private String experienceLevel;
    
    private Boolean onboardingComplete;

    private String bodyType;
    private Set<String> aims;
    private String profileVisibility;
    private Boolean shareWeight;
    private Boolean shareActivity;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getSelectedWorkoutPlanId() {
        return selectedWorkoutPlanId;
    }

    public void setSelectedWorkoutPlanId(Long selectedWorkoutPlanId) {
        this.selectedWorkoutPlanId = selectedWorkoutPlanId;
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

    public String getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(String v) { this.profileVisibility = v; }

    public Boolean getShareWeight() { return shareWeight; }
    public void setShareWeight(Boolean v) { this.shareWeight = v; }

    public Boolean getShareActivity() { return shareActivity; }
    public void setShareActivity(Boolean v) { this.shareActivity = v; }
}
