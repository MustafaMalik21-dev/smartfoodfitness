package com.mustafa.smartfoodfitness.dto;

public class WgerExerciseTile {
    private Integer id;
    private String name;
    private String category;     // Chest, Back, etc (our mapped label)
    private String difficulty;   // optional if you want later
    private String imageUrl;     // first image if available

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
