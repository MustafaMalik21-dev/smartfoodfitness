package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.CompletedWorkout;

public interface CompletedWorkoutRepository extends JpaRepository<CompletedWorkout, Long> { // define custom query methods for retrieving completed workouts based on user profile ID and completed timestamp, allowing for fetching all completed workouts for a user as well as filtering completed workouts by a specific date range when querying the database, and also counting the total number of completed workouts for a user and counting the number of completed workouts after a specific date

    List<CompletedWorkout> findByUserProfileIdOrderByCompletedAtDesc(Long userId);

    long countByUserProfileId(Long userId);

    long countByUserProfileIdAndCompletedAtAfter(Long userId, Instant after);
}
