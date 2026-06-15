package com.mustafa.smartfoodfitness.dto;
// Define the WeightEntryResponse DTO with fields for weight entry details such as ID, user ID, weight value, weight unit, recorded timestamp, created timestamp, and updated timestamp, along with getter and setter methods for each field to facilitate data transfer of weight entry information between the backend and frontend of the application
import java.time.Instant;

public class WeightEntryResponse {

    private Long id;
    private Long userId;
    private Double weightValue;
    private String weightUnit;
    private Instant recordedAt;
    private Instant createdAt;
    private Instant updatedAt;

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
    private String source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
