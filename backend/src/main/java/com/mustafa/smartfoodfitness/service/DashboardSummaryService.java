package com.mustafa.smartfoodfitness.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.DashboardSummaryResponse;
import com.mustafa.smartfoodfitness.dto.NutritionSummaryVsGoalsResponse;
import com.mustafa.smartfoodfitness.dto.WorkoutStreakResponse;


@Service
public class DashboardSummaryService {

    private final NutritionSummaryService nutritionSummaryService;
    private final WorkoutLogService workoutLogService;

    public DashboardSummaryService(
            NutritionSummaryService nutritionSummaryService,
            WorkoutLogService workoutLogService
    ) {
        this.nutritionSummaryService = nutritionSummaryService;
        this.workoutLogService = workoutLogService;
    }


    public DashboardSummaryResponse getDashboardSummary(long userId, String date, String timezone) { // retrieve a summary of the user's nutrition and workout data for a specific date, validating the input and ensuring that the provided timezone is valid, then fetching the nutrition summary vs goals and workout streak for the user based on the provided date and timezone before aggregating this information into a DashboardSummaryResponse DTO and returning it
        ZoneId zoneId;
        try {
            zoneId = (timezone == null || timezone.isBlank())
                    ? ZoneId.systemDefault()
                    : ZoneId.of(timezone.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone.");
        }

        String dateValue = (date == null || date.isBlank()) // if the caller does not provide a date, use the current date in the specified timezone as the default value; otherwise, use the provided date string (after trimming whitespace) as is, allowing the nutrition summary service to apply its existing date parsing and validation logic which supports both absolute dates and relative date keywords like "daily", "weekly", etc.
                ? LocalDate.now(zoneId).toString()
                : date.trim();

        NutritionSummaryVsGoalsResponse vs = nutritionSummaryService.getNutritionSummaryVsGoals( //fetch the nutrition summary vs goals for the user based on the provided date and timezone by calling the getNutritionSummaryVsGoals method of the NutritionSummaryService, passing in the user ID, date value, and timezone ID as parameters, and storing the returned response in a variable for later use in constructing the DashboardSummaryResponse
                userId,
                "daily",
                dateValue,
                zoneId.getId()
        );

        DashboardSummaryResponse r = new DashboardSummaryResponse(); // create a new DashboardSummaryResponse DTO and populate it with data from the nutrition summary vs goals response as well as the workout streak response before returning the populated DTO
        r.setUserId(userId);
        r.setTimezone(zoneId.getId());
        r.setDate(dateValue);

        r.setFrom(vs.getFrom());
        r.setTo(vs.getTo());
        r.setEntriesCount(vs.getEntriesCount());

        r.setTotalCalories(vs.getTotalCalories());
        r.setCaloriesGoal(vs.getCaloriesGoal());
        r.setCaloriesRemaining(vs.getCaloriesRemaining());
        r.setCaloriesPercent(vs.getCaloriesPercent());

        r.setTotalProteins(vs.getTotalProteins());
        r.setProteinsGoal(vs.getProteinsGoal());
        r.setProteinsRemaining(vs.getProteinsRemaining());
        r.setProteinsPercent(vs.getProteinsPercent());

        r.setTotalCarbs(vs.getTotalCarbs());
        r.setCarbsGoal(vs.getCarbsGoal());
        r.setCarbsRemaining(vs.getCarbsRemaining());
        r.setCarbsPercent(vs.getCarbsPercent());

        r.setTotalFats(vs.getTotalFats());
        r.setFatsGoal(vs.getFatsGoal());
        r.setFatsRemaining(vs.getFatsRemaining());
        r.setFatsPercent(vs.getFatsPercent());

        WorkoutStreakResponse streak = workoutLogService.getWorkoutStreak(userId, dateValue, zoneId.getId()); // fetch the workout streak for the user based on the provided date and timezone by calling the getWorkoutStreak method of the WorkoutLogService, passing in the user ID, date value, and timezone ID as parameters, and storing the returned response in a variable for later use in populating the workout streak fields of the DashboardSummaryResponse
        r.setCurrentStreakDays(streak.getCurrentStreakDays());
        r.setTotalWorkouts(streak.getTotalWorkouts());
        r.setWorkoutsLast7Days(streak.getWorkoutsLast7Days());

        return r;
    }
}
