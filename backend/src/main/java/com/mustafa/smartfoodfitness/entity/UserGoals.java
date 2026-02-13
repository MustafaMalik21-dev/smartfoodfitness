package com.mustafa.smartfoodfitness.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
// Define the UserGoals entity with fields for calorie goal, protein goal, carb goal, fat goal, created and updated timestamps, and a reference to the associated user profile, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "user_goals")
public class UserGoals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer calorieGoal;

    @Column(nullable = false)
    private Integer proteinGoal;

    @Column(nullable = false)
    private Integer carbGoal;

    @Column(nullable = false)
    private Integer fatGoal;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne
    @JoinColumn(name = "user_profile_id", nullable = false, unique = true)
    private UserProfile userProfile;

    public Long getId() { 
        return id; 
    }

    public Integer getCalorieGoal() { 
        return calorieGoal; 
    }
    public void setCalorieGoal(Integer calorieGoal) { 
        this.calorieGoal = calorieGoal; 
    }

    public Integer getProteinGoal() { 
        return proteinGoal; 
    }
    public void setProteinGoal(Integer proteinGoal) { 
        this.proteinGoal = proteinGoal; 
    }

    public Integer getCarbGoal() { 
        return carbGoal; 
    }
    public void setCarbGoal(Integer carbGoal) { 
        this.carbGoal = carbGoal; 
    }

    public Integer getFatGoal() { 
        return fatGoal; 
    }
    public void setFatGoal(Integer fatGoal) { 
        this.fatGoal = fatGoal; 
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

    public UserProfile getUserProfile() { 
        return userProfile; 
    }
    public void setUserProfile(UserProfile userProfile) { 
        this.userProfile = userProfile; 
    }
}
