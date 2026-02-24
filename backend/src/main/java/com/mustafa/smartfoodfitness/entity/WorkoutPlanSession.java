package com.mustafa.smartfoodfitness.entity;
// Define the WorkoutPlanSession entity representing a workout session within a workout plan in the database, with fields for ID, workout plan association, session index, title, focus, exercise details in JSON format, estimated minutes, timestamps for creation and update, and corresponding getter and setter methods for each field to facilitate data persistence and retrieval of workout plan session information within the application
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "workout_plan_session",
    uniqueConstraints = @UniqueConstraint(columnNames = { "workout_plan_id", "session_index" })
)
public class WorkoutPlanSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    @JsonIgnore
    private WorkoutPlan workoutPlan;

    @Column(name = "session_index", nullable = false)
    private Integer sessionIndex;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String focus;

    @Column(name = "exercise_json", nullable = false, length = 12000)
    private String exerciseJson;

    private Integer estimatedMinutes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { 
        return id; 
    }

    public WorkoutPlan getWorkoutPlan() { 
        return workoutPlan; 
    }
    public void setWorkoutPlan(WorkoutPlan workoutPlan) { 
        this.workoutPlan = workoutPlan; 
    }

    public Integer getSessionIndex() { 
        return sessionIndex; 
    }
    public void setSessionIndex(Integer sessionIndex) { 
        this.sessionIndex = sessionIndex; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public String getFocus() { 
        return focus; 
    }
    public void setFocus(String focus) { 
        this.focus = focus; 
    }

    public String getExerciseJson() { 
        return exerciseJson; 
    }
    public void setExerciseJson(String exerciseJson) { 
        this.exerciseJson = exerciseJson; 
    }

    public Integer getEstimatedMinutes() { 
        return estimatedMinutes; 
    }
    public void setEstimatedMinutes(Integer estimatedMinutes) { 
        this.estimatedMinutes = estimatedMinutes; 
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
