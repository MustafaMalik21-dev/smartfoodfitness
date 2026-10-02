package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mustafa.smartfoodfitness.entity.FoodEntryLogs;

public interface FoodEntryLogsRepository extends JpaRepository<FoodEntryLogs, Long> { // define custom query methods for retrieving food entry logs based on user profile ID and logged timestamp, allowing for fetching all food entry logs for a user as well as filtering food entry logs by a specific date range when querying the database

    List<FoodEntryLogs> findByUserProfileIdOrderByLoggedAtDesc(Long userProfileId);

    List<FoodEntryLogs> findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtDesc(
        Long userProfileId,
        Instant from,
        Instant to
    );
    List<FoodEntryLogs> findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(
        Long userProfileId,
        Instant from,
        Instant to
    );

    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM FoodEntryLogs f WHERE f.userProfile.id = :userProfileId")
    void deleteByUserProfileId(@Param("userProfileId") Long userProfileId);

}
