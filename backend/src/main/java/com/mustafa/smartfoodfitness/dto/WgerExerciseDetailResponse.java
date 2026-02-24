package com.mustafa.smartfoodfitness.dto;
// Define the WgerExerciseDetailResponse DTO with fields for exercise details such as ID, name, description, images, muscles worked, and equipment needed, along with getter and setter methods for each field to facilitate data transfer of exercise detail information between the backend and frontend of the application
import java.util.List;

public class WgerExerciseDetailResponse {
    private Integer id;
    private String name;
    private String description;
    private List<String> images;
    private List<String> muscles;
    private List<String> equipment;

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

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

    public List<String> getImages() { 
        return images; 
    }
    public void setImages(List<String> images) { 
        this.images = images; 
    }

    public List<String> getMuscles() { 
        return muscles; 
    }
    public void setMuscles(List<String> muscles) { 
        this.muscles = muscles; 
    }

    public List<String> getEquipment() { 
        return equipment; 
    }
    public void setEquipment(List<String> equipment) { 
        this.equipment = equipment; 
    }
}
