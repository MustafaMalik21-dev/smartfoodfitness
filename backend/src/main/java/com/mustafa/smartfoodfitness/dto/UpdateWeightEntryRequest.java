package com.mustafa.smartfoodfitness.dto;
// Define the UpdateWeightEntryRequest DTO with fields for weight value, weight unit, and recorded timestamp, along with getter and setter methods for each field to facilitate data transfer of weight entry update information between the backend and frontend of the application
import java.time.Instant;

import jakarta.validation.constraints.Positive;

public class UpdateWeightEntryRequest {

    @Positive
    private Double weightValue;

    private String weightUnit;

    private Instant recordedAt;

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
}
