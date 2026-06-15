package com.mustafa.smartfoodfitness.dto;

public class WaterTrendPointResponse {

    private String date;
    private Double waterMl;

    public WaterTrendPointResponse(String date, Double waterMl) {
        this.date = date;
        this.waterMl = waterMl;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getWaterMl() { return waterMl; }
    public void setWaterMl(Double waterMl) { this.waterMl = waterMl; }
}
