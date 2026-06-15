package com.mustafa.smartfoodfitness.dto;

import java.time.Instant;

public class CreateWaterEntryRequest {

    private Long userId;
    private Double waterMl;
    private Instant loggedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getWaterMl() { return waterMl; }
    public void setWaterMl(Double waterMl) { this.waterMl = waterMl; }

    public Instant getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Instant loggedAt) { this.loggedAt = loggedAt; }
}
