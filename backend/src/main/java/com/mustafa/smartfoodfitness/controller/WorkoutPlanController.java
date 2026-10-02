package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.WorkoutPlanResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanSessionRepository;
import com.mustafa.smartfoodfitness.service.WorkoutPlanSeedService;

@RestController
@RequestMapping("/api/workout-plans") //Controller class responsible for handling HTTP requests related to workout plans, providing endpoints for retrieving all active workout plans, fetching details of a specific workout plan by its ID, retrieving the sessions associated with a specific workout plan, allowing users to select a workout plan, searching for workout plans based on various criteria such as level, goal, and split, and seeding the database with workout plans when users access the relevant endpoints in the application
public class WorkoutPlanController {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutPlanSessionRepository sessionRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutPlanSeedService workoutPlanSeedService;

    @Value("${app.seed.http:false}")
    private boolean seedHttpEnabled;

    public WorkoutPlanController(
            WorkoutPlanRepository workoutPlanRepository,
            WorkoutPlanSessionRepository sessionRepository,
            UserProfileRepository userProfileRepository,
            WorkoutPlanSeedService workoutPlanSeedService
    ) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.sessionRepository = sessionRepository;
        this.userProfileRepository = userProfileRepository;
        this.workoutPlanSeedService = workoutPlanSeedService;
    }

    @GetMapping // handle HTTP GET requests to retrieve all active workout plans, and returning a list of response DTOs representing the active workout plans to the client when they access the relevant endpoint in the application
    public List<WorkoutPlanResponse> getAllActivePlans() { 
        return workoutPlanRepository.findByIsActiveTrueOrderByTitleAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}") // handle HTTP GET requests to retrieve details of a specific workout plan by its ID, accepting a path variable representing the workout plan ID, validating that the workout plan exists and is active, and returning a response DTO containing the workout plan details to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan does not exist or is not active
    public WorkoutPlanResponse getPlanById(@PathVariable @NonNull Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        return toResponse(plan);
    }

    @GetMapping("/{id}/sessions") // handle HTTP GET requests to retrieve the sessions associated with a specific workout plan, accepting a path variable representing the workout plan ID, validating that the workout plan exists and is active, and returning a list of response DTOs representing the workout plan sessions to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan does not exist or is not active
    public List<WorkoutPlanSession> getPlanSessions(@PathVariable @NonNull Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        return sessionRepository.findByWorkoutPlanIdOrderBySessionIndexAsc(id);
    }

    @PostMapping("/{id}/select") // handle HTTP POST requests to allow users to select a workout plan, accepting a path variable representing the workout plan ID and a query parameter for the user ID, validating that the workout plan exists and is active, validating that the associated user profile exists, updating the user's selected workout plan in the database, and returning a response indicating the successful selection of the workout plan to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan does not exist or is not active, or if the user profile does not exist
    public String selectPlan(
            @PathVariable @NonNull Long id,
            @RequestParam Long userId
    ) {
        AuthGuard.requireSelf(userId);
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        user.setSelectedWorkoutPlanId(plan.getId());
        user.setUpdatedAt(Instant.now());
        userProfileRepository.save(user);

        return "Selected plan " + plan.getId() + " for user " + userId;
    }
 
    @GetMapping("/search") // handle HTTP GET requests to search for workout plans based on various criteria such as level, goal, and split, accepting optional query parameters for the search criteria, validating the input parameters, querying the database for workout plans that match the specified criteria, and returning a list of response DTOs representing the matching workout plans to the client when they access the relevant endpoint in the application
    public List<WorkoutPlanResponse> searchPlans(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String split
    ) {
        String levelValue = norm(level);
        String goalValue = norm(goal);
        String splitValue = norm(split);

        boolean hasLevel = levelValue != null;
        boolean hasGoal = goalValue != null;
        boolean hasSplit = splitValue != null;

        List<WorkoutPlan> plans;

        if (hasLevel && hasGoal && hasSplit) {
            plans = workoutPlanRepository
                    .findByIsActiveTrueAndLevelIgnoreCaseAndGoalIgnoreCaseAndSplitIgnoreCaseOrderByTitleAsc(
                            levelValue, goalValue, splitValue
                    );
        } else if (hasLevel && hasGoal) {
            plans = workoutPlanRepository
                    .findByIsActiveTrueAndLevelIgnoreCaseAndGoalIgnoreCaseOrderByTitleAsc(levelValue, goalValue);
        } else if (hasLevel) {
            plans = workoutPlanRepository.findByIsActiveTrueAndLevelIgnoreCaseOrderByTitleAsc(levelValue);
        } else if (hasGoal) {
            plans = workoutPlanRepository.findByIsActiveTrueAndGoalIgnoreCaseOrderByTitleAsc(goalValue);
        } else if (hasSplit) {
            plans = workoutPlanRepository.findByIsActiveTrueAndSplitIgnoreCaseOrderByTitleAsc(splitValue);
        } else {
            plans = workoutPlanRepository.findByIsActiveTrueOrderByTitleAsc();
        }

        return plans.stream().map(this::toResponse).toList();
    }

    @PostMapping("/seed") // handle HTTP POST requests to seed the database with workout plans, validating that seeding is enabled through application configuration, invoking the workout plan seed service to populate the database with workout plans if it is currently empty, and returning a response indicating the result of the seeding operation to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if seeding is not enabled
    public String seedPlans() {
        if (!seedHttpEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
        return workoutPlanSeedService.seedIfEmpty();
    }

    private String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isBlank()) return null;
        return t;
    }

    private WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        WorkoutPlanResponse r = new WorkoutPlanResponse();
        r.setId(plan.getId());
        r.setTitle(plan.getTitle());
        r.setLevel(plan.getLevel());
        r.setGoal(plan.getGoal());
        r.setSplit(plan.getSplit());
        r.setDaysPerWeek(plan.getDaysPerWeek());
        r.setEstimatedDurationMinutes(plan.getEstimatedDurationMinutes());
        r.setShortDescription(plan.getShortDescription());
        r.setPros(plan.getPros());
        r.setCons(plan.getCons());
        return r;
    }
}
