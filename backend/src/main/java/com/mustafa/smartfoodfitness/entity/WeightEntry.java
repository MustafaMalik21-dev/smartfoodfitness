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
// Define the WeightEntry entity with fields for user profile reference, weight value, weight unit, recorded timestamp, and timestamps for creation and updates, along with appropriate JPA annotations for mapping to the database table and columns
@Entity
@Table(name = "weight_entry")
public class WeightEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false)
    private Double weightValue;

    @Column(nullable = false)
    private String weightUnit;

    @Column(nullable = false)
    private Instant recordedAt;

    // Body composition — nullable; only populated from smart scale readings
    private Double bodyFatPercent;
    private Double proteinPercent;
    private Double muscleMassKg;
    private Double visceralFatLevel;
    private Double bmi;
    private Double boneMassKg;
    private Double waterPercent;
    private Integer bmr;
    private Double waistCm;
    private Double hipCm;
    private Double chestCm;

    // Detailed body measurements (BodyMeasurementsScreen)
    private Double neckCm;
    private Double shoulderCm;
    private Double leftBicepCm;
    private Double rightBicepCm;
    private Double leftForearmCm;
    private Double rightForearmCm;
    private Double abdomenCm;
    private Double leftThighCm;
    private Double rightThighCm;
    private Double leftCalfCm;
    private Double rightCalfCm;

    @Column(length = 20)
    private String source; // "manual" | "scale" | "scan"

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { 
        return id; 
    }

    public UserProfile getUserProfile() { 
        return userProfile; 
    }
    public void setUserProfile(UserProfile userProfile) { 
        this.userProfile = userProfile; 
    }

    public Double getWeightValue() { 
        return weightValue; 
    }
    public void setWeightValue(Double weightValue) { 
        this.weightValue = weightValue; 
    }

    public String getWeightUnit() { 
        return weightUnit; 
    }
    public void setWeightUnit(String weightUnit) { 
        this.weightUnit = weightUnit; 
    }

    public Instant getRecordedAt() { 
        return recordedAt; 
    }
    public void setRecordedAt(Instant recordedAt) { 
        this.recordedAt = recordedAt; 
    }

    public Double getBodyFatPercent() { return bodyFatPercent; }
    public void setBodyFatPercent(Double v) { this.bodyFatPercent = v; }

    public Double getProteinPercent() { return proteinPercent; }
    public void setProteinPercent(Double v) { this.proteinPercent = v; }

    public Double getMuscleMassKg() { return muscleMassKg; }
    public void setMuscleMassKg(Double v) { this.muscleMassKg = v; }

    public Double getVisceralFatLevel() { return visceralFatLevel; }
    public void setVisceralFatLevel(Double v) { this.visceralFatLevel = v; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double v) { this.bmi = v; }

    public Double getBoneMassKg() { return boneMassKg; }
    public void setBoneMassKg(Double v) { this.boneMassKg = v; }

    public Double getWaterPercent() { return waterPercent; }
    public void setWaterPercent(Double v) { this.waterPercent = v; }

    public Integer getBmr() { return bmr; }
    public void setBmr(Integer v) { this.bmr = v; }

    public Double getWaistCm() { return waistCm; }
    public void setWaistCm(Double v) { this.waistCm = v; }

    public Double getHipCm() { return hipCm; }
    public void setHipCm(Double v) { this.hipCm = v; }

    public Double getChestCm() { return chestCm; }
    public void setChestCm(Double v) { this.chestCm = v; }

    public Double getNeckCm() { return neckCm; }
    public void setNeckCm(Double v) { this.neckCm = v; }

    public Double getShoulderCm() { return shoulderCm; }
    public void setShoulderCm(Double v) { this.shoulderCm = v; }

    public Double getLeftBicepCm() { return leftBicepCm; }
    public void setLeftBicepCm(Double v) { this.leftBicepCm = v; }

    public Double getRightBicepCm() { return rightBicepCm; }
    public void setRightBicepCm(Double v) { this.rightBicepCm = v; }

    public Double getLeftForearmCm() { return leftForearmCm; }
    public void setLeftForearmCm(Double v) { this.leftForearmCm = v; }

    public Double getRightForearmCm() { return rightForearmCm; }
    public void setRightForearmCm(Double v) { this.rightForearmCm = v; }

    public Double getAbdomenCm() { return abdomenCm; }
    public void setAbdomenCm(Double v) { this.abdomenCm = v; }

    public Double getLeftThighCm() { return leftThighCm; }
    public void setLeftThighCm(Double v) { this.leftThighCm = v; }

    public Double getRightThighCm() { return rightThighCm; }
    public void setRightThighCm(Double v) { this.rightThighCm = v; }

    public Double getLeftCalfCm() { return leftCalfCm; }
    public void setLeftCalfCm(Double v) { this.leftCalfCm = v; }

    public Double getRightCalfCm() { return rightCalfCm; }
    public void setRightCalfCm(Double v) { this.rightCalfCm = v; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

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
