package com.mustafa.smartfoodfitness.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserProfile receiver;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Instant sentAt;

    /** null until the recipient opens the conversation */
    private Instant readAt;

    public Long getId() { return id; }

    public UserProfile getSender()   { return sender; }
    public void setSender(UserProfile s) { this.sender = s; }

    public UserProfile getReceiver()   { return receiver; }
    public void setReceiver(UserProfile r) { this.receiver = r; }

    public String getContent() { return content; }
    public void setContent(String c) { this.content = c; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant t) { this.sentAt = t; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant t) { this.readAt = t; }
}
