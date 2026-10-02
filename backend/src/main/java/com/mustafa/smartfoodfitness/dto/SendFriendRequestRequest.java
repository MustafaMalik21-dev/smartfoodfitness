package com.mustafa.smartfoodfitness.dto;

import jakarta.validation.constraints.NotNull;

/** Field names are the wire contract for POST /api/friends/request — the app sends { senderId, receiverId }. */
public class SendFriendRequestRequest {

    @NotNull(message = "senderId is required.")
    private Long senderId;

    @NotNull(message = "receiverId is required.")
    private Long receiverId;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long v) { this.senderId = v; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long v) { this.receiverId = v; }
}
