package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateWorkoutLogRequest;
import com.mustafa.smartfoodfitness.dto.WorkoutLogResponse;
import com.mustafa.smartfoodfitness.dto.WorkoutStreakResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WorkoutLog;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WorkoutLogRepository;

@Service
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserProfileRepository userProfileRepository;

    public WorkoutLogService(WorkoutLogRepository workoutLogRepository, UserProfileRepository userProfileRepository) {
        this.workoutLogRepository = workoutLogRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public WorkoutLogResponse createWorkoutLog(CreateWorkoutLogRequest request) { // Method to create a new workout log entry, accepting a CreateWorkoutLogRequest DTO containing the details of the workout log to be created, validating the input data including checking for the existence of the associated user profile, creating a new WorkoutLog entity based on the request data, saving it to the database, and returning a WorkoutLogResponse DTO representing the created workout log to be sent back to the client when they access the relevant endpoint in the application
        Long userId = request.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        WorkoutLog entity = new WorkoutLog();
        entity.setUserProfile(userProfile);
        entity.setWorkoutName(request.getWorkoutName());
        entity.setWorkoutType(request.getWorkoutType());
        entity.setDurationMinutes(request.getDurationMinutes());
        entity.setPerformedAt(request.getPerformedAt());
        entity.setNotes(request.getNotes());
        entity.setDetailsJson(request.getDetailsJson());

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        WorkoutLog saved = workoutLogRepository.save(entity);
        return mapToResponse(saved);
    }

    public List<WorkoutLogResponse> getWorkoutLogsForUser(long userId, Instant from, Instant to) { // Method to retrieve workout logs for a specific user, accepting the user ID and optional from and to timestamps to filter the logs by performed date, validating the existence of the associated user profile, fetching the workout logs from the database based on the provided criteria, and returning a list of WorkoutLogResponse DTOs representing the workout logs to be sent back to the client when they access the relevant endpoint in the application
        List<WorkoutLog> logs;
        if (from != null && to != null) {
            logs = workoutLogRepository.findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(userId, from, to);
        } else {
            logs = workoutLogRepository.findByUserProfileIdOrderByPerformedAtDesc(userId);
        }
        return logs.stream().map(this::mapToResponse).toList();
    }

    public WorkoutStreakResponse getWorkoutStreak(long userId, String date, String timezone) { // Method to calculate the workout streak for a specific user, accepting the user ID, an optional date to use as the reference point for calculating the streak (defaulting to the current date), and an optional timezone to interpret the dates (defaulting to the system timezone), validating the existence of the associated user profile, fetching the workout logs for the user within a relevant date range, calculating the current streak of consecutive workout days up to the reference date, counting the total workouts and workouts in the last 7 days, and returning a WorkoutStreakResponse DTO representing the calculated streak information to be sent back to the client when they access the relevant endpoint in the application
        ZoneId zoneId;
        try {
            zoneId = (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone.");
        }

        LocalDate baseDate;
        try {
            baseDate = (date == null || date.isBlank()) ? LocalDate.now(zoneId) : LocalDate.parse(date.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date. Use YYYY-MM-DD.");
        }

        LocalDate startDate = baseDate.minusDays(365);
        Instant from = startDate.atStartOfDay(zoneId).toInstant();
        Instant toExclusive = baseDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<WorkoutLog> logs = workoutLogRepository.findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(
                userId, from, toExclusive
        );

        HashSet<LocalDate> workoutDays = new HashSet<>();
        for (WorkoutLog log : logs) {
            if (log.getPerformedAt() == null) continue;
            workoutDays.add(log.getPerformedAt().atZone(zoneId).toLocalDate());
        }

        int streak = 0;
        LocalDate cursor = baseDate;
        while (workoutDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        Instant last7From = baseDate.minusDays(6).atStartOfDay(zoneId).toInstant();
        Instant last7ToExclusive2 = baseDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        long last7Count = workoutLogRepository
                .findByUserProfileIdAndPerformedAtBetweenOrderByPerformedAtDesc(userId, last7From, last7ToExclusive2)
                .size();

        long total = workoutLogRepository.countByUserProfileId(userId);

        WorkoutStreakResponse r = new WorkoutStreakResponse();
        r.setUserId(userId);
        r.setTimezone(zoneId.getId());
        r.setBaseDate(baseDate.toString());
        r.setCurrentStreakDays(streak);
        r.setTotalWorkouts(total);
        r.setWorkoutsLast7Days(last7Count);
        return r;
    }

    private WorkoutLogResponse mapToResponse(WorkoutLog saved) { // Helper method to convert a WorkoutLog entity to a WorkoutLogResponse DTO, extracting the relevant fields from the entity and populating the response DTO accordingly before returning it to be sent back to the client when they access the relevant endpoint in the application
        WorkoutLogResponse r = new WorkoutLogResponse();
        r.setId(saved.getId());
        r.setUserId(saved.getUserProfile().getId());
        r.setWorkoutName(saved.getWorkoutName());
        r.setWorkoutType(saved.getWorkoutType());
        r.setDurationMinutes(saved.getDurationMinutes());
        r.setPerformedAt(saved.getPerformedAt());
        r.setNotes(saved.getNotes());
        r.setDetailsJson(saved.getDetailsJson());
        r.setCreatedAt(saved.getCreatedAt());
        r.setUpdatedAt(saved.getUpdatedAt());
        return r;
    }
}
