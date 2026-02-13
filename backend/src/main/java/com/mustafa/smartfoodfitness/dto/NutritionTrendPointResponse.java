package com.mustafa.smartfoodfitness.dto;
// Define the NutritionTrendPointResponse DTO with fields for date, total calories, total proteins, total carbs, total fats, and entries count, along with getter and setter methods for each field to facilitate data transfer of nutrition trend point information between the backend and frontend of the application
public class NutritionTrendPointResponse {

    private String date;

    private Integer totalCalories;
    private Integer totalProteins;
    private Integer totalCarbs;
    private Integer totalFats;

    private Integer entriesCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
