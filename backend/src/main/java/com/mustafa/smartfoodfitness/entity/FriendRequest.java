package com.mustafa.smartfoodfitness.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friend_request",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserProfile receiver;

    /** PENDING | ACCEPTED | DECLINED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }

    public UserProfile getSender()   { return sender; }
    public void setSender(UserProfile s) { this.sender = s; }

    public UserProfile getReceiver()   { return receiver; }
    public void setReceiver(UserProfile r) { this.receiver = r; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant t) { this.createdAt = t; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant t) { this.updatedAt = t; }
}
