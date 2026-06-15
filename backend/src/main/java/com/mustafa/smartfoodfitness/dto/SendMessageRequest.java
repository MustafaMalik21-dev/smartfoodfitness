package com.mustafa.smartfoodfitness.dto;

public class SendMessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long v) { this.senderId = v; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long v) { this.receiverId = v; }

    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
}
