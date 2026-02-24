package com.mustafa.smartfoodfitness.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WorkoutPlan;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> { // Define the WorkoutPlanRepository interface extending JpaRepository to provide CRUD operations for WorkoutPlan entities, along with custom query methods to retrieve active workout plans based on various criteria such as level, goal, and split, and to check for the existence of workout plans by title

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

    boolean existsByTitleIgnoreCase(String title);

    Optional<WorkoutPlan> findByTitleIgnoreCase(String title);
}
