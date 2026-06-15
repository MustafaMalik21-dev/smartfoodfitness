package com.mustafa.smartfoodfitness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.CalorieTrendResponse;
import com.mustafa.smartfoodfitness.dto.MacroTrendResponse;
import com.mustafa.smartfoodfitness.dto.MicroTrendResponse;
import com.mustafa.smartfoodfitness.dto.NutritionSummaryResponse;
import com.mustafa.smartfoodfitness.dto.NutritionSummaryVsGoalsResponse;
import com.mustafa.smartfoodfitness.dto.NutritionTrendResponse;
import com.mustafa.smartfoodfitness.service.NutritionSummaryService;

@RestController
@RequestMapping("/api/nutrition-summary") // define a REST controller for handling HTTP requests related to nutrition summaries, with a base URL of "/api/nutrition-summary" to group all nutrition summary-related endpoints together and allow for organized routing within the application
public class NutritionSummaryController {

    private final NutritionSummaryService nutritionSummaryService;

    public NutritionSummaryController(NutritionSummaryService nutritionSummaryService) {
        this.nutritionSummaryService = nutritionSummaryService;
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve a nutrition summary for a specific user identified by their user ID, optionally filtered by a specified period, date, and timezone, allowing clients to view the user's nutrition summary information when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public NutritionSummaryResponse getNutritionSummary(
            @PathVariable long userId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getNutritionSummary(userId, period, date, timezone);
    }

    @GetMapping("/user/{userId}/vs-goals") // handle HTTP GET requests to retrieve a nutrition summary compared to the user's goals for a specific user identified by their user ID, optionally filtered by a specified period, date, and timezone, allowing clients to view how the user's nutrition summary compares to their goals when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public NutritionSummaryVsGoalsResponse getNutritionSummaryVsGoals(
            @PathVariable long userId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getNutritionSummaryVsGoals(userId, period, date, timezone);
    }

    @GetMapping("/user/{userId}/trend") // handle HTTP GET requests to retrieve nutrition trend data for a specific user identified by their user ID, optionally filtered by a specified range, date, and timezone, allowing clients to view the user's nutrition trends over time when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public NutritionTrendResponse getNutritionTrend(
            @PathVariable long userId,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getNutritionTrend(userId, range, date, timezone);
    }

    @GetMapping("/user/{userId}/macro-trend") // handle HTTP GET requests to retrieve macronutrient trend data for a specific user identified by their user ID, optionally filtered by a specified range, date, and timezone, allowing clients to view the user's macronutrient trends over time when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public MacroTrendResponse getMacroTrend(
            @PathVariable long userId,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getMacroTrend(userId, range, date, timezone);
    }

    @GetMapping("/user/{userId}/micro-trend")
    public MicroTrendResponse getMicroTrend(
            @PathVariable long userId,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getMicroTrend(userId, range, date, timezone);
    }

    @GetMapping("/user/{userId}/calorie-trend") // handle HTTP GET requests to retrieve calorie trend data for a specific user identified by their user ID, optionally filtered by a specified range, date, and timezone, allowing clients to view the user's calorie trends over time when they access the relevant endpoint in the application, with the ability to specify filters for more accurate and relevant results
    public CalorieTrendResponse getCalorieTrend(
            @PathVariable long userId,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timezone
    ) {
        return nutritionSummaryService.getCalorieTrend(userId, range, date, timezone);
    }
}
