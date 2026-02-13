package com.mustafa.smartfoodfitness.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;

public interface WorkoutPlanSessionRepository extends JpaRepository<WorkoutPlanSession, Long> {
    long countByWorkoutPlanId(Long workoutPlanId);

    List<WorkoutPlanSession> findByWorkoutPlanIdOrderBySessionIndexAsc(Long workoutPlanId);

    Optional<WorkoutPlanSession> findByWorkoutPlanIdAndSessionIndex(Long workoutPlanId, Integer sessionIndex);
}
