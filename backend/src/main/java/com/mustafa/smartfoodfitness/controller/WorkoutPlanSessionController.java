package com.mustafa.smartfoodfitness.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.WorkoutPlanSessionResponse;
import com.mustafa.smartfoodfitness.service.WorkoutPlanSessionService;

@RestController
@RequestMapping("/api/workout-plan-sessions")
public class WorkoutPlanSessionController {

    private final WorkoutPlanSessionService service;

    public WorkoutPlanSessionController(WorkoutPlanSessionService service) {
        this.service = service;
    }

    @GetMapping("/plan/{planId}")
    public List<WorkoutPlanSessionResponse> getSessionsForPlan(@PathVariable @NonNull Long planId) {
        return service.getSessionsForPlan(planId);
    }

    @GetMapping("/plan/{planId}/session/{sessionIndex}")
    public WorkoutPlanSessionResponse getSession(
            @PathVariable @NonNull Long planId,
            @PathVariable @NonNull Integer sessionIndex
    ) {
        return service.getSession(planId, sessionIndex)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found."));
    }
}
