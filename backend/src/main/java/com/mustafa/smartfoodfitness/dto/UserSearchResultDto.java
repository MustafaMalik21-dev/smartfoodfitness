package com.mustafa.smartfoodfitness.dto;

public class UserSearchResultDto {
    private Long userId;
    private String displayName;
    /** Always partially hidden ("m***@gmail.com") — search results never carry a real address. */
    private String email;
    /** null | PENDING_SENT | PENDING_RECEIVED | ACCEPTED */
    private String friendStatus;

    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getFriendStatus() { return friendStatus; }
    public void setFriendStatus(String v) { this.friendStatus = v; }
}
