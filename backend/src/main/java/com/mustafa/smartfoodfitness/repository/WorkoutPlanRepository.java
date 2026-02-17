package com.mustafa.smartfoodfitness.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WorkoutPlan;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

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
