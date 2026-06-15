package com.mustafa.smartfoodfitness.dto;

import java.util.List;

public class WaterTrendResponse {

    private List<WaterTrendPointResponse> points;

    public WaterTrendResponse(List<WaterTrendPointResponse> points) {
        this.points = points;
    }

    public List<WaterTrendPointResponse> getPoints() { return points; }
    public void setPoints(List<WaterTrendPointResponse> points) { this.points = points; }
}
