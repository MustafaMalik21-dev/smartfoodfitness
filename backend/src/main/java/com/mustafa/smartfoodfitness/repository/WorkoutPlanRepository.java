package com.mustafa.smartfoodfitness.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WorkoutPlan;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> { // define custom query methods for retrieving workout plans based on various criteria such as active status, level, goal, and split, allowing for flexible filtering of workout plans when fetching them from the database

    List<WorkoutPlan> findByIsActiveTrueOrderByTitleAsc();

    List<WorkoutPlan> findByIsActiveTrueAndLevelIgnoreCaseOrderByTitleAsc(String level);

    List<WorkoutPlan> findByIsActiveTrueAndGoalIgnoreCaseOrderByTitleAsc(String goal);

    List<WorkoutPlan> findByIsActiveTrueAndSplitIgnoreCaseOrderByTitleAsc(String split);

    List<WorkoutPlan> findByIsActiveTrueAndLevelIgnoreCaseAndGoalIgnoreCaseOrderByTitleAsc(String level, String goal);

    List<WorkoutPlan> findByIsActiveTrueAndLevelIgnoreCaseAndGoalIgnoreCaseAndSplitIgnoreCaseOrderByTitleAsc(
        String level,
        String goal,
        String split
    );
}
