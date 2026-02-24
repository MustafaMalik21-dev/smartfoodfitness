package com.mustafa.smartfoodfitness.dto;
//Define the WorkoutPlanExerciseItem DTO with fields for exercise details such as wger ID, name, sets, reps, and notes, along with getter and setter methods for each field to facilitate data transfer of workout plan exercise item information between the backend and frontend of the application
public class WorkoutPlanExerciseItem {

    private Integer wgerId;
    private String name;
    private Integer sets;
    private Integer reps;
    private String notes;

    public Integer getWgerId() { 
        return wgerId; 
    }
    public void setWgerId(Integer wgerId) { 
        this.wgerId = wgerId; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public Integer getSets() { 
        return sets; 
    }
    public void setSets(Integer sets) { 
        this.sets = sets; 
    }

    public Integer getReps() { 
        return reps; 
    }
    public void setReps(Integer reps) { 
        this.reps = reps; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }
}
