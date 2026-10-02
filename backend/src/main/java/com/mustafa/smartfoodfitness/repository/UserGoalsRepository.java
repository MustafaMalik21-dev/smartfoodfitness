package com.mustafa.smartfoodfitness.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mustafa.smartfoodfitness.entity.UserGoals;

public interface UserGoalsRepository extends JpaRepository<UserGoals, Long> { // define a custom query method for retrieving user goals based on the user profile ID, allowing for efficient lookup of a user's goals when querying the database
    
    Optional<UserGoals> findByUserProfileId(Long userId);

    // Deliberately not @Transactional: account deletion must roll back as one unit,
    // so this may only run inside the caller's transaction.
    @Modifying
    @Query("DELETE FROM UserGoals g WHERE g.userProfile.id = :userId")
    void deleteByUserProfileId(@Param("userId") Long userId);

}
