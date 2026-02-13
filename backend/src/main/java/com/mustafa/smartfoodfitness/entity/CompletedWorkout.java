package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// Define the CompletedWorkout entity with fields for user profile reference, workout plan reference, completed timestamp, duration, notes, and created timestamp, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "completed_workout")
public class CompletedWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @Column(nullable = false)
    private Instant completedAt;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { 
        return id; 
    }

    public UserProfile getUserProfile() { 
        return userProfile; 
    }
    public void setUserProfile(UserProfile userProfile) { 
        this.userProfile = userProfile; 
    }

    public WorkoutPlan getWorkoutPlan() { 
        return workoutPlan; 
    }
    public void setWorkoutPlan(WorkoutPlan workoutPlan) { 
        this.workoutPlan = workoutPlan; 
    }

    public Instant getCompletedAt() { 
        return completedAt; 
    }
    public void setCompletedAt(Instant completedAt) { 
        this.completedAt = completedAt; 
    }

    public Integer getDurationMinutes() { 
        return durationMinutes; 
    }
    public void setDurationMinutes(Integer durationMinutes) { 
        this.durationMinutes = durationMinutes; 
    }

    public String getNotes() { 
        return notes; 
    }
    public void setNotes(String notes) { 
        this.notes = notes; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }
}
