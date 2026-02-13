package com.mustafa.smartfoodfitness.dto;
 // Define the NutritionSummaryResponse DTO with fields for user ID, date range, nutrition summary details (total calories, proteins, carbs, fats) and entries count, along with getter and setter methods for each field to facilitate data transfer of nutrition summary information between the backend and frontend of the application
import java.time.Instant;

public class NutritionSummaryResponse {

    private Long userId;

    private String period;
    private Instant from;
    private Instant to;

    private Integer totalCalories;
    private Integer totalProteins;
    private Integer totalCarbs;
    private Integer totalFats;

    private Integer entriesCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Integer getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Integer totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Integer getTotalProteins() {
        return totalProteins;
    }

    public void setTotalProteins(Integer totalProteins) {
        this.totalProteins = totalProteins;
    }

    public Integer getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(Integer totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public Integer getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(Integer totalFats) {
        this.totalFats = totalFats;
    }

    public Integer getEntriesCount() {
        return entriesCount;
    }

    public void setEntriesCount(Integer entriesCount) {
        this.entriesCount = entriesCount;
    }
}
