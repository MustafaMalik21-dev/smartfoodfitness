package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CompletedWorkoutResponse;
import com.mustafa.smartfoodfitness.dto.CreateCompletedWorkoutRequest;
import com.mustafa.smartfoodfitness.entity.CompletedWorkout;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.repository.CompletedWorkoutRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/completed-workouts")
public class CompletedWorkoutController {

    private final CompletedWorkoutRepository completedWorkoutRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public CompletedWorkoutController(
            CompletedWorkoutRepository completedWorkoutRepository, 
            UserProfileRepository userProfileRepository,
            WorkoutPlanRepository workoutPlanRepository
    ) {
        this.completedWorkoutRepository = completedWorkoutRepository;
        this.userProfileRepository = userProfileRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    @PostMapping // handle HTTP POST requests to create a new completed workout entry, accepting a request body containing the details of the completed workout to be created, validating the input data, and returning a response DTO representing the created completed workout to the client when they access the relevant endpoint in the application
    public CompletedWorkoutResponse createCompletedWorkout(@Valid @RequestBody CreateCompletedWorkoutRequest request) {
        Long workoutPlanId = request.getWorkoutPlanId();
        if (workoutPlanId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workoutPlanId is required.");
        }

        Long userProfileId = request.getUserId(); // validate that the associated user profile exists by fetching it from the database using the provided user ID, throwing a 404 Not Found error if it does not exist since a completed workout must be associated with an existing user profile before creating the new completed workout entry in the database and returning a response DTO representing the created completed workout to the client when they access the relevant endpoint in the application
        if (userProfileId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }

        UserProfile user = userProfileRepository.findById(userProfileId) // validate that the associated user profile exists by fetching it from the database using the provided user ID, throwing a 404 Not Found error if it does not exist since a completed workout must be associated with an existing user profile before creating the new completed workout entry in the database and returning a response DTO representing the created completed workout to the client when they access the relevant endpoint in the application
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        WorkoutPlan plan = workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        CompletedWorkout cw = new CompletedWorkout(); // create a new CompletedWorkout entity and populate it with data from the request, including setting the associated user profile and workout plan, duration, notes, and completedAt timestamp (defaulting to the current time if not provided) before saving the new completed workout entry to the database and returning a response DTO representing the created completed workout to the client when they access the relevant endpoint in the application
        cw.setUserProfile(user);
        cw.setWorkoutPlan(plan);
        cw.setDurationMinutes(request.getDurationMinutes());
        cw.setNotes(request.getNotes());

        cw.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : Instant.now());
        cw.setCreatedAt(Instant.now());

        CompletedWorkout saved = completedWorkoutRepository.save(cw);
        return toResponse(saved);
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve a list of completed workouts for a specific user identified by their user ID, returning the list of completed workouts ordered by completion date in descending order, allowing clients to view the user's workout history when they access the relevant endpoint in the application
    public List<CompletedWorkoutResponse> getCompletedWorkoutsForUser(@PathVariable @NonNull Long userId) {
        return completedWorkoutRepository.findByUserProfileIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CompletedWorkoutResponse toResponse(CompletedWorkout cw) { // convert a CompletedWorkout entity to a CompletedWorkoutResponse DTO, extracting the relevant fields from the entity and populating the response DTO accordingly before returning it to be sent back to the client when they access the relevant endpoint in the application
        CompletedWorkoutResponse r = new CompletedWorkoutResponse();
        r.setId(cw.getId());
        r.setUserId(cw.getUserProfile().getId());
        r.setWorkoutPlanId(cw.getWorkoutPlan().getId());
        r.setWorkoutPlanTitle(cw.getWorkoutPlan().getTitle());
        r.setCompletedAt(cw.getCompletedAt());
        r.setDurationMinutes(cw.getDurationMinutes());
        r.setNotes(cw.getNotes());
        return r;
    }
}
