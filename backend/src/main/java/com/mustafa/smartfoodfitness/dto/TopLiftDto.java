package com.mustafa.smartfoodfitness.dto;

import java.time.Instant;

public class TopLiftDto {
    private String exerciseName;
    private double maxWeightKg;
    private int    repsAtMax;
    private Instant achievedAt;

    public TopLiftDto() {}
    public TopLiftDto(String exerciseName, double maxWeightKg, int repsAtMax, Instant achievedAt) {
        this.exerciseName = exerciseName;
        this.maxWeightKg  = maxWeightKg;
        this.repsAtMax    = repsAtMax;
        this.achievedAt   = achievedAt;
    }

    public String  getExerciseName()  { return exerciseName; }
    public void    setExerciseName(String v)  { this.exerciseName = v; }
    public double  getMaxWeightKg()   { return maxWeightKg; }
    public void    setMaxWeightKg(double v)   { this.maxWeightKg = v; }
    public int     getRepsAtMax()     { return repsAtMax; }
    public void    setRepsAtMax(int v)        { this.repsAtMax = v; }
    public Instant getAchievedAt()    { return achievedAt; }
    public void    setAchievedAt(Instant v)   { this.achievedAt = v; }
}
