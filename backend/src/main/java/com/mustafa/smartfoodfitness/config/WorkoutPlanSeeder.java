package com.mustafa.smartfoodfitness.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mustafa.smartfoodfitness.service.WorkoutPlanSeedService;

//Automatically put workout plans into the database on startup
@Component
public class WorkoutPlanSeeder implements CommandLineRunner {

    private final WorkoutPlanSeedService workoutPlanSeedService;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    public WorkoutPlanSeeder(WorkoutPlanSeedService workoutPlanSeedService) {
        this.workoutPlanSeedService = workoutPlanSeedService;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) return;
        workoutPlanSeedService.seedIfEmpty();
    }
}
