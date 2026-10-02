package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mustafa.smartfoodfitness.entity.WaterEntry;

public interface WaterEntryRepository extends JpaRepository<WaterEntry, Long> {

    List<WaterEntry> findByUserProfileIdOrderByLoggedAtAsc(long userId);

    List<WaterEntry> findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            long userId, Instant from, Instant to);

    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM WaterEntry w WHERE w.userProfile.id = :userId")
    void deleteByUserProfileId(@Param("userId") long userId);
}
