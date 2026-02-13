package com.mustafa.smartfoodfitness.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.CreateFoodEntryLogsRequest;
import com.mustafa.smartfoodfitness.dto.FoodEntryLogsResponse;
import com.mustafa.smartfoodfitness.dto.UpdateFoodEntryLogsRequest;
import com.mustafa.smartfoodfitness.service.FoodEntryLogsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/food-entry-logs") // define a REST controller for handling HTTP requests related to food entry logs, with a base URL of "/api/food-entry-logs" to group all food entry log-related endpoints together and allow for organized routing within the application
public class FoodEntryLogsController {

    private final FoodEntryLogsService foodEntryLogsService;

    public FoodEntryLogsController(FoodEntryLogsService foodEntryLogsService) {
        this.foodEntryLogsService = foodEntryLogsService;
    }

    @PostMapping // handle HTTP POST requests to create a new food entry log, accepting a request body containing the details of the food entry log to be created, validating the input data, and returning a response DTO representing the created food entry log to the client when they access the relevant endpoint in the application
    public FoodEntryLogsResponse createFoodEntry(@Valid @RequestBody CreateFoodEntryLogsRequest request) {
        return foodEntryLogsService.createFoodEntry(request);
    }

    @PutMapping("/{id}") // handle HTTP PUT requests to update an existing food entry log identified by its ID, accepting a request body containing the updated details of the food entry log, validating the input data, and returning a response DTO representing the updated food entry log to the client when they access the relevant endpoint in the application
    public FoodEntryLogsResponse updateFoodEntry(
            @PathVariable long id,
            @Valid @RequestBody UpdateFoodEntryLogsRequest request
    ) {
        return foodEntryLogsService.updateFoodEntry(id, request);
    }

    @GetMapping("/{id}") // handle HTTP GET requests to retrieve a specific food entry log by its ID, validating that the entry exists and returning a response DTO representing the food entry log to the client when they access the relevant endpoint in the application
    public FoodEntryLogsResponse getFoodEntryById(@PathVariable long id) {
        return foodEntryLogsService.getFoodEntryById(id);
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve food entry logs for a specific user, optionally filtered by a date range specified by the "from" and "to" query parameters, allowing clients to fetch food entry logs for a user and narrow down the results based on a specific time period when they access the relevant endpoint in the application
    public List<FoodEntryLogsResponse> getFoodEntriesForUser(
            @PathVariable long userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return foodEntryLogsService.getFoodEntriesForUser(userId, from, to);
    }
}
