package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.CreateUserProfileRequest;
import com.mustafa.smartfoodfitness.dto.PrivacySettingsRequest;
import com.mustafa.smartfoodfitness.dto.UpdateAimsRequest;
import com.mustafa.smartfoodfitness.dto.UpdateSelectedWorkoutPlanRequest;
import com.mustafa.smartfoodfitness.dto.UpdateUserProfileRequest;
import com.mustafa.smartfoodfitness.dto.UserProfileResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user-profile") // define a REST controller for handling HTTP requests related to user profiles, with a base URL of "/api/user-profile" to group all user profile-related endpoints together and allow for organized routing within the application
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public UserProfileController(UserProfileRepository userProfileRepository, WorkoutPlanRepository workoutPlanRepository) {
        this.userProfileRepository = userProfileRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    @PostMapping // handle HTTP POST requests to create a new user profile, accepting a request body containing the details of the user profile to be created, validating the input data, and returning a response DTO representing the created user profile to the client
    public UserProfileResponse createUserProfile(@Valid @RequestBody CreateUserProfileRequest userProfile) {
        AuthGuard.requireSelfEmail(userProfile.getEmail());
        UserProfile entity = new UserProfile();
        entity.setEmail(userProfile.getEmail());
        entity.setDisplayName(userProfile.getDisplayName());
        entity.setAge(userProfile.getAge());
        entity.setHeightValue(userProfile.getHeightValue());
        entity.setHeightUnit(userProfile.getHeightUnit());
        entity.setWeightValue(userProfile.getWeightValue());
        entity.setWeightUnit(userProfile.getWeightUnit());
        entity.setGender(userProfile.getGender());
        entity.setActivityLevel(userProfile.getActivityLevel());
        entity.setExperienceLevel(userProfile.getExperienceLevel());

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        UserProfile saved = userProfileRepository.save(entity); // save the new user profile to the database and return a response DTO representing the saved profile to the client when they access the relevant endpoint in the application
        return toResponse(saved);
    }

    @PutMapping("/{id}") // handle HTTP PUT requests to update an existing user profile identified by its ID, accepting a request body containing the updated details of the user profile, validating the input data, and returning a response DTO representing the updated user profile to the client when they access the relevant endpoint in the application
    public UserProfileResponse updateUserProfile(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        AuthGuard.requireSelf(id);
        UserProfile userProfile = userProfileRepository.findById(id) // validate that the user profile to be updated exists by fetching it from the database using the provided ID, throwing a 404 Not Found error if it does not exist before updating the profile with data from the request and saving the updated profile back to the database
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        if (request.getDisplayName() != null) userProfile.setDisplayName(request.getDisplayName());
        if (request.getAge() != null) userProfile.setAge(request.getAge());
        if (request.getHeightValue() != null) userProfile.setHeightValue(request.getHeightValue());
        if (request.getHeightUnit() != null) userProfile.setHeightUnit(request.getHeightUnit());
        if (request.getWeightValue() != null) userProfile.setWeightValue(request.getWeightValue());
        if (request.getWeightUnit() != null) userProfile.setWeightUnit(request.getWeightUnit());
        if (request.getGender() != null) userProfile.setGender(request.getGender());
        if (request.getActivityLevel() != null) userProfile.setActivityLevel(request.getActivityLevel());
        if (request.getExperienceLevel() != null) userProfile.setExperienceLevel(request.getExperienceLevel());
        if (request.getOnboardingComplete() != null) {
            userProfile.setOnboardingComplete(request.getOnboardingComplete());
        }
        if (request.getAims() != null) {
            userProfile.setAims(request.getAims());
        }
        if (request.getBodyType()          != null) userProfile.setBodyType(request.getBodyType());
        if (request.getProfileVisibility() != null) userProfile.setProfileVisibility(request.getProfileVisibility());
        if (request.getShareWeight()        != null) userProfile.setShareWeight(request.getShareWeight());
        if (request.getShareActivity()      != null) userProfile.setShareActivity(request.getShareActivity());

        userProfile.setUpdatedAt(Instant.now());

        UserProfile saved = userProfileRepository.save(userProfile);
        return toResponse(saved);
        
    }

    @GetMapping("/{id}") // handle HTTP GET requests to retrieve a specific user profile by its ID, validating that the profile exists and returning a response DTO representing the user profile to the client when they access the relevant endpoint in the application
    public UserProfileResponse getUserProfileById(@PathVariable @NonNull Long id) {
        AuthGuard.requireSelf(id);
        UserProfile userProfile = userProfileRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));
        return toResponse(userProfile);
    }

    @GetMapping // handle HTTP GET requests to retrieve a specific user profile by its email, validating that the profile exists and returning a response DTO representing the user profile to the client when they access the relevant endpoint in the application, with the email provided as a query parameter
    public UserProfileResponse getUserProfileByEmail(@RequestParam String email) {
        AuthGuard.requireSelfEmail(email);
        UserProfile userProfile = userProfileRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));
        return toResponse(userProfile);
    }

    @PutMapping("/{id}/selected-workout-plan") // handle HTTP PUT requests to update the selected workout plan for a user profile identified by its ID, accepting a request body containing the ID of the workout plan to be selected, validating the input data and ensuring that both the user profile and workout plan exist, then updating the user profile with the new selected workout plan and saving it back to the database before returning a response DTO representing the updated user profile to the client when they access the relevant endpoint in the application
    public UserProfileResponse updateSelectedWorkoutPlan(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateSelectedWorkoutPlanRequest request
    ) {
        AuthGuard.requireSelf(id);
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        Long workoutPlanId = request.getWorkoutPlanId(); // validate that the workoutPlanId is provided in the request, throwing a 400 Bad Request error if it is not provided since it is required to update the selected workout plan for the user profile
        if (workoutPlanId == null) { 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workoutPlanId is required.");
        }

        WorkoutPlan plan = workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) { // validate that the workout plan to be selected is active, throwing a 400 Bad Request error if it is not active since users should only be able to select active workout plans for their profile
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout plan is not active.");
        }

        userProfile.setSelectedWorkoutPlanId(plan.getId()); // update the user profile with the new selected workout plan ID and set the updatedAt timestamp to the current time before saving the updated profile back to the database and returning a response DTO representing the updated user profile to the client when they access the relevant endpoint in the application
        userProfile.setUpdatedAt(Instant.now());

        UserProfile saved = userProfileRepository.save(userProfile);
        return toResponse(saved);
    }
    @PutMapping("/{id}/aims")
    public UserProfileResponse updateAims(
            @PathVariable @NonNull Long id,
            @RequestBody UpdateAimsRequest request
    ) {
        AuthGuard.requireSelf(id);
        UserProfile userProfile = userProfileRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        Set<String> cleaned = new LinkedHashSet<>();
        if (request != null && request.getAims() != null) {
            for (String a : request.getAims()) {
                if (a == null) continue;
                String s = a.trim();
                if (!s.isEmpty()) cleaned.add(s);
            }
        }

        if (cleaned.size() > 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can select a maximum of 9 aims.");
        }

        userProfile.setAims(cleaned);
        userProfile.setUpdatedAt(Instant.now());

        UserProfile saved = userProfileRepository.save(userProfile);
        return toResponse(saved);
    }


    @PutMapping("/{id}/privacy")
    public UserProfileResponse updatePrivacy(
            @PathVariable @NonNull Long id,
            @RequestBody PrivacySettingsRequest req) {
        AuthGuard.requireSelf(id);
        UserProfile p = userProfileRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));
        if (req.getProfileVisibility() != null) p.setProfileVisibility(req.getProfileVisibility());
        if (req.getShareWeight()        != null) p.setShareWeight(req.getShareWeight());
        if (req.getShareActivity()      != null) p.setShareActivity(req.getShareActivity());
        p.setUpdatedAt(Instant.now());
        return toResponse(userProfileRepository.save(p));
    }

    private UserProfileResponse toResponse(UserProfile saved) { // convert a UserProfile entity to a UserProfileResponse DTO by mapping the relevant fields from the entity to the response object, allowing for a clean separation between the internal data model and the data exposed to clients when they access the relevant endpoints in the application
        UserProfileResponse response = new UserProfileResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setDisplayName(saved.getDisplayName());
        response.setAge(saved.getAge());
        response.setHeightValue(saved.getHeightValue());
        response.setHeightUnit(saved.getHeightUnit());
        response.setWeightValue(saved.getWeightValue());
        response.setWeightUnit(saved.getWeightUnit());
        response.setGender(saved.getGender());
        response.setActivityLevel(saved.getActivityLevel());
        response.setExperienceLevel(saved.getExperienceLevel());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        response.setSelectedWorkoutPlanId(saved.getSelectedWorkoutPlanId());
        response.setOnboardingComplete(saved.getOnboardingComplete());
        response.setBodyType(saved.getBodyType());
        response.setAims(saved.getAims());
        response.setProfileVisibility(saved.getProfileVisibility());
        response.setShareWeight(saved.getShareWeight());
        response.setShareActivity(saved.getShareActivity());
        return response;
    }
}
