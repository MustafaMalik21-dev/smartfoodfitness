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

import com.mustafa.smartfoodfitness.dto.WorkoutPlanResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanSessionRepository;
import com.mustafa.smartfoodfitness.service.WorkoutPlanSeedService;

@RestController
@RequestMapping("/api/workout-plans")
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

    @GetMapping
    public List<WorkoutPlanResponse> getAllActivePlans() {
        return workoutPlanRepository.findByIsActiveTrueOrderByTitleAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public WorkoutPlanResponse getPlanById(@PathVariable @NonNull Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        return toResponse(plan);
    }

    @GetMapping("/{id}/sessions")
    public List<WorkoutPlanSession> getPlanSessions(@PathVariable @NonNull Long id) {
        WorkoutPlan plan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        return sessionRepository.findByWorkoutPlanIdOrderBySessionIndexAsc(id);
    }

    @PostMapping("/{id}/select")
    public String selectPlan(
            @PathVariable @NonNull Long id,
            @RequestParam Long userId
    ) {
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

    @GetMapping("/search")
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

    @PostMapping("/seed")
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
