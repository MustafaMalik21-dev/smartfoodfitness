package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mustafa.smartfoodfitness.entity.WorkoutLog;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> { // define custom query methods for retrieving workout logs based on user profile ID and performed timestamp, allowing for fetching all workout logs for a user as well as filtering workout logs by a specific date range when querying the database

    List<WorkoutLog> findByUserProfileIdOrderByPerformedAtDesc(Long userProfileId);

    List<WorkoutLog> findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(
        Long userProfileId,
        Instant from,
        Instant to
    );

    long countByUserProfileId(Long userProfileId);

    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM WorkoutLog w WHERE w.userProfile.id = :userProfileId")
    void deleteByUserProfileId(@Param("userProfileId") Long userProfileId);
}
