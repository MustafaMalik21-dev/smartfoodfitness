package com.mustafa.smartfoodfitness.dto;

public class FoodSearchResultItem {
    private Long fdcId;
    private String name;
    private String brand;

    // Macros per 100g
    private Double kcalPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;

    // Micronutrients per 100g
    private Double fiberPer100g;
    private Double sugarPer100g;
    private Double sodiumPer100g;
    private Double potassiumPer100g;
    private Double cholesterolPer100g;
    private Double saturatedFatPer100g;
    private Double vitaminAPer100g;
    private Double vitaminCPer100g;
    private Double vitaminDPer100g;
    private Double calciumPer100g;
    private Double ironPer100g;
    private Double zincPer100g;

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

    public Double getFiberPer100g() { return fiberPer100g; }
    public void setFiberPer100g(Double fiberPer100g) { this.fiberPer100g = fiberPer100g; }

    public Double getSugarPer100g() { return sugarPer100g; }
    public void setSugarPer100g(Double sugarPer100g) { this.sugarPer100g = sugarPer100g; }

    public Double getSodiumPer100g() { return sodiumPer100g; }
    public void setSodiumPer100g(Double sodiumPer100g) { this.sodiumPer100g = sodiumPer100g; }

    public Double getPotassiumPer100g() { return potassiumPer100g; }
    public void setPotassiumPer100g(Double potassiumPer100g) { this.potassiumPer100g = potassiumPer100g; }

    public Double getCholesterolPer100g() { return cholesterolPer100g; }
    public void setCholesterolPer100g(Double cholesterolPer100g) { this.cholesterolPer100g = cholesterolPer100g; }

    public Double getSaturatedFatPer100g() { return saturatedFatPer100g; }
    public void setSaturatedFatPer100g(Double saturatedFatPer100g) { this.saturatedFatPer100g = saturatedFatPer100g; }

    public Double getVitaminAPer100g() { return vitaminAPer100g; }
    public void setVitaminAPer100g(Double vitaminAPer100g) { this.vitaminAPer100g = vitaminAPer100g; }

    public Double getVitaminCPer100g() { return vitaminCPer100g; }
    public void setVitaminCPer100g(Double vitaminCPer100g) { this.vitaminCPer100g = vitaminCPer100g; }

    public Double getVitaminDPer100g() { return vitaminDPer100g; }
    public void setVitaminDPer100g(Double vitaminDPer100g) { this.vitaminDPer100g = vitaminDPer100g; }

    public Double getCalciumPer100g() { return calciumPer100g; }
    public void setCalciumPer100g(Double calciumPer100g) { this.calciumPer100g = calciumPer100g; }

    public Double getIronPer100g() { return ironPer100g; }
    public void setIronPer100g(Double ironPer100g) { this.ironPer100g = ironPer100g; }

    public Double getZincPer100g() { return zincPer100g; }
    public void setZincPer100g(Double zincPer100g) { this.zincPer100g = zincPer100g; }
}
