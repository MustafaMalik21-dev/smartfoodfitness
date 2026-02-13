package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateUserGoalsRequest;
import com.mustafa.smartfoodfitness.dto.UserGoalsResponse;
import com.mustafa.smartfoodfitness.entity.UserGoals;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserGoalsRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user-goals") // define a REST controller for handling HTTP requests related to user goals, with a base URL of "/api/user-goals" to group all user goal-related endpoints together and allow for organized routing within the application
public class UserGoalsController {

    private final UserGoalsRepository userGoalsRepository;
    private final UserProfileRepository userProfileRepository;

    public UserGoalsController(UserGoalsRepository userGoalsRepository, UserProfileRepository userProfileRepository) {
        this.userGoalsRepository = userGoalsRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping
    public UserGoalsResponse createUserGoals(@Valid @RequestBody CreateUserGoalsRequest userGoals) { // handle HTTP POST requests to create a new set of user goals, accepting a request body containing the details of the user goals to be created, validating the input data, and returning a response DTO representing the created user goals to the client when they access the relevant endpoint in the application
        Long userId = userGoals.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }

        UserProfile userProfile = userProfileRepository.findById(userId) // validate that the associated user profile exists by fetching it from the database using the provided user ID, throwing a 404 Not Found error if it does not exist since user goals must be associated with an existing user profile before creating the new user goals entry in the database and returning a response DTO representing the created user goals to the client when they access the relevant endpoint in the application
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        UserGoals entity = new UserGoals(); // create a new UserGoals entity and populate it with data from the request, including setting the associated user profile and the calorie, protein, carb, and fat goals, then set the createdAt and updatedAt timestamps to the current time before saving the new user goals entry to the database and returning a response DTO representing the created user goals to the client when they access the relevant endpoint in the application
        entity.setUserProfile(userProfile);
        entity.setCalorieGoal(userGoals.getCalorieGoal());
        entity.setProteinGoal(userGoals.getProteinGoal());
        entity.setCarbGoal(userGoals.getCarbGoal());
        entity.setFatGoal(userGoals.getFatGoal());

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        UserGoals saved = userGoalsRepository.save(entity); // save the new user goals entry to the database and return a response DTO representing the created user goals to the client when they access the relevant endpoint in the application
        return toResponse(saved);
    }

    @PutMapping("/user/{userId}") // handle HTTP PUT requests to update an existing set of user goals for a specific user identified by their user ID, accepting a request body containing the updated details of the user goals, validating the input data and ensuring that the associated user profile and existing user goals entry exist, then updating the user goals entry with the new data and saving it back to the database before returning a response DTO representing the updated user goals to the client when they access the relevant endpoint in the application
    public UserGoalsResponse updateUserGoals( 
            @PathVariable @NonNull Long userId,
            @Valid @RequestBody CreateUserGoalsRequest userGoals
    ) { 
        UserGoals goals = userGoalsRepository.findByUserProfileId(userId) // validate that the existing user goals entry for the specified user exists by fetching it from the database using the provided user ID, throwing a 404 Not Found error if it does not exist since user goals must exist for the user before they can be updated, then update the existing user goals entry with data from the request and save the updated entry back to the database before returning a response DTO representing the updated user goals to the client when they access the relevant endpoint in the application
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User goals not found."));

        goals.setCalorieGoal(userGoals.getCalorieGoal());
        goals.setProteinGoal(userGoals.getProteinGoal());
        goals.setCarbGoal(userGoals.getCarbGoal());
        goals.setFatGoal(userGoals.getFatGoal());
        goals.setUpdatedAt(Instant.now());

        UserGoals saved = userGoalsRepository.save(goals); // save the updated user goals entry back to the database and return a response DTO representing the updated user goals to the client when they access the relevant endpoint in the application
        return toResponse(saved);
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve the user goals for a specific user identified by their user ID, validating that the user goals entry exists for the specified user and returning a response DTO representing the user goals to the client when they access the relevant endpoint in the application
    public UserGoalsResponse getUserGoalsByUserId(@PathVariable @NonNull Long userId) {
        UserGoals goals = userGoalsRepository.findByUserProfileId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User goals not found."));
        return toResponse(goals);
    }

    private UserGoalsResponse toResponse(UserGoals saved) { // convert a UserGoals entity to a UserGoalsResponse DTO by mapping the relevant fields from the entity to the response object, allowing for a clean separation between the internal data model and the data exposed to clients when they access the relevant endpoints in the application
        UserGoalsResponse response = new UserGoalsResponse();
        response.setId(saved.getId());
        response.setUserId(saved.getUserProfile().getId());
        response.setCalorieGoal(saved.getCalorieGoal());
        response.setProteinGoal(saved.getProteinGoal());
        response.setCarbGoal(saved.getCarbGoal());
        response.setFatGoal(saved.getFatGoal());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        return response;
    }
}
