package com.mustafa.smartfoodfitness.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.UserGoals;

public interface UserGoalsRepository extends JpaRepository<UserGoals, Long> { // define a custom query method for retrieving user goals based on the user profile ID, allowing for efficient lookup of a user's goals when querying the database
    
    Optional<UserGoals> findByUserProfileId(Long userId);
    
}
