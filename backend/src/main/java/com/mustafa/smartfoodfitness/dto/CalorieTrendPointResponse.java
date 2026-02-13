package com.mustafa.smartfoodfitness.dto;
// Define the CalorieTrendPointResponse DTO with fields for date, total calories, and entries count, along with getter and setter methods for each field to facilitate data transfer of calorie trend point information between the backend and frontend of the application
public class CalorieTrendPointResponse {

    private String date;
    private Integer calories;
    private Integer entriesCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getEntriesCount() {
        return entriesCount;
    }

    public void setEntriesCount(Integer entriesCount) {
        this.entriesCount = entriesCount;
    }
}
