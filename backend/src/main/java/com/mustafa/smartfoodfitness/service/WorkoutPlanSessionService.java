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

    public List<WorkoutPlanSessionResponse> getSessionsForPlan(Long planId) {
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

    private WorkoutPlanSessionResponse toResponse(WorkoutPlanSession s) {
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

    private List<WorkoutPlanExerciseItem> parseExercises(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return mapper.readValue(json, new TypeReference<List<WorkoutPlanExerciseItem>>() {});
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }
}
