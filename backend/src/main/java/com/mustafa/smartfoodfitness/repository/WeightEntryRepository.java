package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mustafa.smartfoodfitness.entity.WeightEntry;

public interface WeightEntryRepository extends JpaRepository<WeightEntry, Long> { // define custom query methods for retrieving weight entries based on user profile ID and recorded timestamp, allowing for fetching all weight entries for a user as well as filtering weight entries by a specific date range when querying the database, and also retrieving the most recent weight entry for a user

    List<WeightEntry> findByUserProfileIdOrderByRecordedAtAsc(Long userId);

    List<WeightEntry> findByUserProfileIdAndRecordedAtBetweenOrderByRecordedAtAsc(
        Long userId,
        Instant from,
        Instant to
    );

    Optional<WeightEntry> findTopByUserProfileIdOrderByRecordedAtDesc(Long userId);

    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM WeightEntry w WHERE w.userProfile.id = :userId")
    void deleteByUserProfileId(@Param("userId") Long userId);
}
