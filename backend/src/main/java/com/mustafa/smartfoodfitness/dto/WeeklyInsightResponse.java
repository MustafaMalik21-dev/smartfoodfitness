package com.mustafa.smartfoodfitness.dto;

public class WeeklyInsightResponse {
    private String insight;
    private String generatedAt;

    public WeeklyInsightResponse() {}

    public WeeklyInsightResponse(String insight, String generatedAt) {
        this.insight = insight;
        this.generatedAt = generatedAt;
    }

    public String getInsight() { return insight; }
    public void setInsight(String insight) { this.insight = insight; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
