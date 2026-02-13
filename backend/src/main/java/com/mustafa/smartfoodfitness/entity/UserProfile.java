package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
// Define the UserProfile entity with fields for user goals reference, selected workout plan ID, email, display name, age, height value and unit, weight value and unit
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "userProfile")
    private UserGoals userGoals;

    @Column(name = "selected_workout_plan_id")
    private Long selectedWorkoutPlanId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    private Integer age;

    private Double heightValue;
    private String heightUnit;

    private Double weightValue;
    private String weightUnit;

    private String gender;
    private String activityLevel;

    @Column(name = "experience_level")
    private String experienceLevel;

    private Instant createdAt;
    private Instant updatedAt;
    
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role = "USER";
    
    @Column(name = "onboarding_complete")
    private Boolean onboardingComplete;

    
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public UserGoals getUserGoals() { 
        return userGoals; 
    }
    public void setUserGoals(UserGoals userGoals) { 
        this.userGoals = userGoals; 
    }

    public Long getSelectedWorkoutPlanId() { 
        return selectedWorkoutPlanId; 
    }
    public void setSelectedWorkoutPlanId(Long selectedWorkoutPlanId) { 
        this.selectedWorkoutPlanId = selectedWorkoutPlanId; 
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

    public String getExperienceLevel() { 
        return experienceLevel; 
    }
    public void setExperienceLevel(String experienceLevel) { 
        this.experienceLevel = experienceLevel; 
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

    public String getPasswordHash() { 
        return passwordHash; 
    }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }

    public String getRole() { 
        return role; 
    }
    public void setRole(String role) { 
        this.role = role; 
    }
    public Boolean getOnboardingComplete() { 
        return onboardingComplete; 
    }
    public void setOnboardingComplete(Boolean onboardingComplete) { 
        this.onboardingComplete = onboardingComplete; 
    }
}
