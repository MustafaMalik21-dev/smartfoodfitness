package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class FriendDto {
    private Long requestId;
    private Long userId;
    private String displayName;
    private String email;
    private String status;       // PENDING | ACCEPTED
    private String direction;    // INCOMING | OUTGOING (for pending requests)
    private String profileVisibility;
    private Boolean shareWeight;
    private Boolean shareActivity;

    // Latest shared stats (null if not sharing or not measured)
    private Double latestWeightKg;
    private Double latestBmi;
    private Double latestBodyFatPercent;
    private Integer workoutsThisWeek;
    private String  activePlanName;
    private String  activePlanSplit;
    private Integer activePlanDaysPerWeek;
    private Integer totalWorkouts;
    private List<TopLiftDto> topLifts;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long v) { this.requestId = v; }

    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getDirection() { return direction; }
    public void setDirection(String v) { this.direction = v; }

    public String getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(String v) { this.profileVisibility = v; }

    public Boolean getShareWeight() { return shareWeight; }
    public void setShareWeight(Boolean v) { this.shareWeight = v; }

    public Boolean getShareActivity() { return shareActivity; }
    public void setShareActivity(Boolean v) { this.shareActivity = v; }

    public Double getLatestWeightKg() { return latestWeightKg; }
    public void setLatestWeightKg(Double v) { this.latestWeightKg = v; }

    public Double getLatestBmi() { return latestBmi; }
    public void setLatestBmi(Double v) { this.latestBmi = v; }

    public Double getLatestBodyFatPercent() { return latestBodyFatPercent; }
    public void setLatestBodyFatPercent(Double v) { this.latestBodyFatPercent = v; }

    public Integer getWorkoutsThisWeek() { return workoutsThisWeek; }
    public void setWorkoutsThisWeek(Integer v) { this.workoutsThisWeek = v; }

    public String getActivePlanName() { return activePlanName; }
    public void setActivePlanName(String v) { this.activePlanName = v; }

    public String getActivePlanSplit() { return activePlanSplit; }
    public void setActivePlanSplit(String v) { this.activePlanSplit = v; }

    public Integer getActivePlanDaysPerWeek() { return activePlanDaysPerWeek; }
    public void setActivePlanDaysPerWeek(Integer v) { this.activePlanDaysPerWeek = v; }

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer v) { this.totalWorkouts = v; }

    public List<TopLiftDto> getTopLifts() { return topLifts; }
    public void setTopLifts(List<TopLiftDto> v) { this.topLifts = v; }
}
