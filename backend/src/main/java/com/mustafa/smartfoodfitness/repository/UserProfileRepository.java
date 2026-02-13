package com.mustafa.smartfoodfitness.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mustafa.smartfoodfitness.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> { // define a custom query method for retrieving a user profile based on the email address, allowing for efficient lookup of user profiles by email when querying the database
    
    Optional<UserProfile> findByEmail(String email);
    boolean existsByEmail(String email);

}
