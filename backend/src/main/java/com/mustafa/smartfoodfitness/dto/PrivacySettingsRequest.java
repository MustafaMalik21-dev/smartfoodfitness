package com.mustafa.smartfoodfitness.dto;

public class PrivacySettingsRequest {
    /** "public" | "friends" | "private" */
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
