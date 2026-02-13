package com.mustafa.smartfoodfitness.dto;
// Define the MacroTrendPointResponse DTO with fields for date, total proteins, total carbs, total fats, and entries count, along with getter and setter methods for each field to facilitate data transfer of macro trend point information between the backend and frontend of the application
public class MacroTrendPointResponse {

    private String date;

    private Integer proteins;
    private Integer carbs;
    private Integer fats;

    private Integer entriesCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getProteins() {
        return proteins;
    }

    public void setProteins(Integer proteins) {
        this.proteins = proteins;
    }

    public Integer getCarbs() {
        return carbs;
    }

    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }

    public Integer getFats() {
        return fats;
    }

    public void setFats(Integer fats) {
        this.fats = fats;
    }

    public Integer getEntriesCount() {
        return entriesCount;
    }

    public void setEntriesCount(Integer entriesCount) {
        this.entriesCount = entriesCount;
    }
}
