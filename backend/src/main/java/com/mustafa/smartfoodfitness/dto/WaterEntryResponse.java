package com.mustafa.smartfoodfitness.dto;

import java.time.Instant;

public class WaterEntryResponse {

    private Long id;
    private Long userId;
    private Double waterMl;
    private Instant loggedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getWaterMl() { return waterMl; }
    public void setWaterMl(Double waterMl) { this.waterMl = waterMl; }

    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
