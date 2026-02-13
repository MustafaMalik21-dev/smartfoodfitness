package com.mustafa.smartfoodfitness.dto;

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
