package com.mustafa.smartfoodfitness.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.WaterEntry;

public interface WaterEntryRepository extends JpaRepository<WaterEntry, Long> {

    List<WaterEntry> findByUserProfileIdOrderByLoggedAtAsc(long userId);

    List<WaterEntry> findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            long userId, Instant from, Instant to);
}
