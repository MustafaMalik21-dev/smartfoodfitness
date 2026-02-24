package com.mustafa.smartfoodfitness.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.WorkoutPlanSessionResponse;
import com.mustafa.smartfoodfitness.service.WorkoutPlanSessionService;

@RestController
@RequestMapping("/api/workout-plan-sessions") //Controller class responsible for handling HTTP requests related to workout plan sessions, providing endpoints for retrieving the sessions associated with a specific workout plan and fetching details of a specific workout plan session by its index when users access the relevant endpoints in the application
public class WorkoutPlanSessionController {

    private final WorkoutPlanSessionService service;

    public WorkoutPlanSessionController(WorkoutPlanSessionService service) {
        this.service = service;
    }

    @GetMapping("/plan/{planId}") // handle HTTP GET requests to retrieve the sessions associated with a specific workout plan, accepting a path variable representing the workout plan ID, validating that the workout plan exists and is active, and returning a list of response DTOs representing the workout plan sessions to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan does not exist or is not active
    public List<WorkoutPlanSessionResponse> getSessionsForPlan(@PathVariable @NonNull Long planId) {
        return service.getSessionsForPlan(planId);
    }

    @GetMapping("/plan/{planId}/session/{sessionIndex}") // handle HTTP GET requests to retrieve details of a specific workout plan session by its index, accepting path variables representing the workout plan ID and session index, validating that the workout plan and session exist, and returning a response DTO containing the workout plan session details to the client when they access the relevant endpoint in the application, throwing a 404 Not Found error if the workout plan or session does not exist
    public WorkoutPlanSessionResponse getSession(
            @PathVariable @NonNull Long planId,
            @PathVariable @NonNull Integer sessionIndex
    ) {
        return service.getSession(planId, sessionIndex)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found."));
    }
}
