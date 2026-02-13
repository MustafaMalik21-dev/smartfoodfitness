package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Define the WorkoutPlan entity with fields for title, level, goal, split, days per week, estimated duration, short description, pros, cons, active status, and timestamps for creation and updates, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "workout_plan")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private String goal;

    @Column(nullable = false)
    private String split;

    private Integer daysPerWeek;
    private Integer estimatedDurationMinutes;

    @Column(length = 2000)
    private String shortDescription;

    @Column(length = 2000)
    private String pros;

    @Column(length = 2000)
    private String cons;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getLevel() { 
        return level; 
    }
    public void setLevel(String level) { 
        this.level = level; 
    }

    public String getGoal() { 
        return goal; 
    }
    public void setGoal(String goal) { 
        this.goal = goal; 
    }

    public String getSplit() { 
        return split; 
    }
    public void setSplit(String split) { 
        this.split = split; 
    }

    public Integer getDaysPerWeek() { 
        return daysPerWeek; 
    }
    public void setDaysPerWeek(Integer daysPerWeek) { 
        this.daysPerWeek = daysPerWeek; 
    }

    public Integer getEstimatedDurationMinutes() { 
        return estimatedDurationMinutes; 
    }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { 
        this.estimatedDurationMinutes = estimatedDurationMinutes; 
    }

    public String getShortDescription() { 
        return shortDescription; 
    }
    public void setShortDescription(String shortDescription) { 
        this.shortDescription = shortDescription; 
    }

    public String getPros() { 
        return pros; 
    }
    public void setPros(String pros) { 
        this.pros = pros; 
    }

    public String getCons() { 
        return cons; 
    }
    public void setCons(String cons) { 
        this.cons = cons; 
    }

    public Boolean getIsActive() { 
        return isActive; 
    }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }

    public Instant getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(Instant updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}
