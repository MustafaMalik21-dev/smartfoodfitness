package com.mustafa.smartfoodfitness.dto;
// Define the OnboardingResponse DTO with fields for user ID and onboarding completion status, along with getter and setter methods for each field to facilitate data transfer of onboarding response information between the backend and frontend of the application
public class OnboardingResponse {
  private Long userId;
  private Boolean onboardingComplete;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public Boolean getOnboardingComplete() { 
        return onboardingComplete; 
    }
    public void setOnboardingComplete(Boolean onboardingComplete) { 
        this.onboardingComplete = onboardingComplete; 
    }
}
