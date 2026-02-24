package com.mustafa.smartfoodfitness.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;

public interface WorkoutPlanSessionRepository extends JpaRepository<WorkoutPlanSession, Long> { // Define the WorkoutPlanSessionRepository interface extending JpaRepository to provide CRUD operations for WorkoutPlanSession entities, along with custom query methods to count sessions by workout plan ID, retrieve sessions for a specific workout plan ordered by session index, and find a specific session by workout plan ID and session index
    long countByWorkoutPlanId(Long workoutPlanId);

    List<WorkoutPlanSession> findByWorkoutPlanIdOrderBySessionIndexAsc(Long workoutPlanId);

    Optional<WorkoutPlanSession> findByWorkoutPlanIdAndSessionIndex(Long workoutPlanId, Integer sessionIndex);
}
