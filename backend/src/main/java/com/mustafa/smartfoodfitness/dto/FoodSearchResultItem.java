package com.mustafa.smartfoodfitness.dto;

public class FoodSearchResultItem {
    private Long fdcId;
    private String name;
    private String brand;
    private Double kcalPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;

    public Long getFdcId() { return fdcId; }
    public void setFdcId(Long fdcId) { this.fdcId = fdcId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getKcalPer100g() { return kcalPer100g; }
    public void setKcalPer100g(Double kcalPer100g) { this.kcalPer100g = kcalPer100g; }

    public Double getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(Double proteinPer100g) { this.proteinPer100g = proteinPer100g; }

    public Double getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(Double carbsPer100g) { this.carbsPer100g = carbsPer100g; }

    public Double getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(Double fatPer100g) { this.fatPer100g = fatPer100g; }
}
