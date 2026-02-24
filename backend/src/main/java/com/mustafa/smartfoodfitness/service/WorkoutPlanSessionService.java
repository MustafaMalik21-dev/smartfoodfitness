package com.mustafa.smartfoodfitness.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustafa.smartfoodfitness.dto.WorkoutPlanExerciseItem;
import com.mustafa.smartfoodfitness.dto.WorkoutPlanSessionResponse;
import com.mustafa.smartfoodfitness.entity.WorkoutPlan;
import com.mustafa.smartfoodfitness.entity.WorkoutPlanSession;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutPlanSessionRepository;

@Service
public class WorkoutPlanSessionService {

    private final WorkoutPlanSessionRepository repo;
    private final WorkoutPlanRepository planRepo;
    private final ObjectMapper mapper;

    public WorkoutPlanSessionService(
            WorkoutPlanSessionRepository repo,
            WorkoutPlanRepository planRepo,
            ObjectMapper mapper
    ) {
        this.repo = repo;
        this.planRepo = planRepo;
        this.mapper = mapper;
    }

    public List<WorkoutPlanSessionResponse> getSessionsForPlan(Long planId) { // Method to retrieve the sessions associated with a specific workout plan, accepting a workout plan ID as a parameter, validating that the workout plan exists and is active, fetching the workout plan sessions from the database based on the provided workout plan ID, converting them to response DTOs, and returning the list of WorkoutPlanSessionResponse DTOs to be sent back to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan does not exist or is not active
        WorkoutPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found."));

        if (plan.getIsActive() == null || !plan.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found.");
        }

        return repo.findByWorkoutPlanIdOrderBySessionIndexAsc(planId).stream().map(this::toResponse).toList();
    }

    public Optional<WorkoutPlanSessionResponse> getSession(Long planId, Integer sessionIndex) {
        return repo.findByWorkoutPlanIdAndSessionIndex(planId, sessionIndex).map(this::toResponse);
    }

    private WorkoutPlanSessionResponse toResponse(WorkoutPlanSession s) { // Helper method to convert a WorkoutPlanSession entity to a WorkoutPlanSessionResponse DTO, extracting the relevant fields from the entity and populating the response DTO accordingly, including parsing the exercise JSON string into a list of WorkoutPlanExerciseItem objects before returning the response DTO to be sent back to the client when they access the relevant endpoint in the application
        WorkoutPlanSessionResponse r = new WorkoutPlanSessionResponse();
        r.setId(s.getId());
        r.setWorkoutPlanId(s.getWorkoutPlan().getId());
        r.setSessionIndex(s.getSessionIndex());
        r.setTitle(s.getTitle());
        r.setFocus(s.getFocus());
        r.setEstimatedMinutes(s.getEstimatedMinutes());
        r.setExercises(parseExercises(s.getExerciseJson()));
        return r;
    }

    private List<WorkoutPlanExerciseItem> parseExercises(String json) { // Helper method to parse a JSON string representing a list of workout plan exercises into a List of WorkoutPlanExerciseItem objects, accepting the JSON string as a parameter, checking if the string is null or blank and returning an empty list if so, attempting to parse the JSON string using the ObjectMapper and returning the resulting list of WorkoutPlanExerciseItem objects, or returning an empty list if there is an error during parsing when users access the relevant endpoint in the application
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return mapper.readValue(json, new TypeReference<List<WorkoutPlanExerciseItem>>() {});
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }
}
