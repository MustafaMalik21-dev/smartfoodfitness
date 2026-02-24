package com.mustafa.smartfoodfitness.dto;
// Define the WgerExerciseTile DTO with fields for exercise ID, name, category, difficulty, and image URL, along with getter and setter methods for each field to facilitate data transfer of exercise tile information between the backend and frontend of the application
public class WgerExerciseTile {
    private Integer id;
    private String name;
    private String category;   
    private String difficulty;
    private String imageUrl; 

    public Integer getId() { 
        return id; 
    }
    public void setId(Integer id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getCategory() { 
        return category; 
    }
    public void setCategory(String category) { 
        this.category = category; 
    }

    public String getDifficulty() { 
        return difficulty; 
    }
    public void setDifficulty(String difficulty) { 
        this.difficulty = difficulty; 
    }

    public String getImageUrl() { 
        return imageUrl; 
    }
    public void setImageUrl(String imageUrl) { 
        this.imageUrl = imageUrl; 
    }
}
