package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.CreateWorkoutLogRequest;
import com.mustafa.smartfoodfitness.dto.WorkoutLogResponse;
import com.mustafa.smartfoodfitness.dto.WorkoutStreakResponse;
import com.mustafa.smartfoodfitness.service.WorkoutLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/workout-logs") // define a REST controller for handling HTTP requests related to workout logs, with a base URL of "/api/workout-logs" to group all workout log-related endpoints together and allow for organized routing within the application
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;

    public WorkoutLogController(WorkoutLogService workoutLogService) {
        this.workoutLogService = workoutLogService;
    }

    @PostMapping // handle HTTP POST requests to create a new workout log entry, accepting a request body containing the details of the workout log to be created, validating the input data, and returning a response DTO representing the created workout log entry to the client
    public WorkoutLogResponse createWorkoutLog(@Valid @RequestBody CreateWorkoutLogRequest request) {
        AuthGuard.requireSelf(request.getUserId());
        return workoutLogService.createWorkoutLog(request);
    }

    @GetMapping("/user/{userId}") // retrieve workout logs for a specific user, optionally filtered by a date range specified by the "from" and "to" query parameters, allowing clients to fetch workout logs for a user and narrow down the results based on a specific time period when they access the relevant endpoint in the application
    public List<WorkoutLogResponse> getWorkoutLogsForUser(
            @PathVariable long userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        AuthGuard.requireSelf(userId);
        return workoutLogService.getWorkoutLogsForUser(userId, from, to);
    }

    @GetMapping("/user/{userId}/streak") // retrieve the current workout streak for a specific user based on an optional date and timezone, allowing clients to view the user's workout streak information when they access the relevant endpoint in the application, with the ability to specify a date and timezone for accurate streak calculation
    public WorkoutStreakResponse getWorkoutStreak(
            @PathVariable long userId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        AuthGuard.requireSelf(userId);
        return workoutLogService.getWorkoutStreak(userId, date, timezone);
    }
}
