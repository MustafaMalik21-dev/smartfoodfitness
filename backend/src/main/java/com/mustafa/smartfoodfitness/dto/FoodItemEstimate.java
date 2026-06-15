package com.mustafa.smartfoodfitness.dto;

public class FoodItemEstimate {
    private String name;
    private int estimatedGrams;
    private int calories;
    private int protein;
    private int carbs;
    private int fat;

    public FoodItemEstimate() {}

    public FoodItemEstimate(String name, int estimatedGrams, int calories, int protein, int carbs, int fat) {
        this.name = name;
        this.estimatedGrams = estimatedGrams;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getEstimatedGrams() { return estimatedGrams; }
    public void setEstimatedGrams(int estimatedGrams) { this.estimatedGrams = estimatedGrams; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }
    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }
    public int getFat() { return fat; }
    public void setFat(int fat) { this.fat = fat; }
}
