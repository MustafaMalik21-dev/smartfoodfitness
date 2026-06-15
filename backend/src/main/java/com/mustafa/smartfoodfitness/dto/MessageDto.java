package com.mustafa.smartfoodfitness.dto;

import java.time.Instant;

public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Instant sentAt;
    private Instant readAt;

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long v) { this.senderId = v; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String v) { this.senderName = v; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long v) { this.receiverId = v; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String v) { this.receiverName = v; }

    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant v) { this.sentAt = v; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant v) { this.readAt = v; }
}
