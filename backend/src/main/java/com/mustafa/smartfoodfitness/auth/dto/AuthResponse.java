package com.mustafa.smartfoodfitness.auth.dto;

public class AuthResponse {
    private Long userId;
    private String email;
    private String displayName;
    private String token;
    private Boolean onboardingComplete;

    public Long getUserId() { 
        return userId; 
    }
    public void setUserId(Long userId) { 
        this.userId = userId; 
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

    public String getToken() { 
        return token; 
    }
    public void setToken(String token) { 
        this.token = token; 
    }
    public Boolean getOnboardingComplete() { 
        return onboardingComplete; 
    }
    public void setOnboardingComplete(Boolean onboardingComplete) { 
        this.onboardingComplete = onboardingComplete; 
    }
}
