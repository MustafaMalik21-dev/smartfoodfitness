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

import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
import com.mustafa.smartfoodfitness.dto.CreateWeightEntryRequest;
import com.mustafa.smartfoodfitness.dto.UpdateWeightEntryRequest;
import com.mustafa.smartfoodfitness.dto.WeightEntryResponse;
import com.mustafa.smartfoodfitness.service.WeightEntryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/weight-entries") // define a REST controller for handling HTTP requests related to weight entries, with a base URL of "/api/weight-entries" to group all weight entry-related endpoints together and allow for organized routing within the application
public class WeightEntryController {

    private final WeightEntryService weightEntryService;

    public WeightEntryController(WeightEntryService weightEntryService) {
        this.weightEntryService = weightEntryService;
    }

    @PostMapping
    public WeightEntryResponse createWeightEntry(@Valid @RequestBody CreateWeightEntryRequest request) {
        AuthGuard.requireSelf(request.getUserId());
        return weightEntryService.createWeightEntry(request);
    }

    @PutMapping("/{id}") // handle HTTP PUT requests to update an existing weight entry identified by its ID, accepting a request body containing the updated details of the weight entry, validating the input data, and returning a response DTO representing the updated weight entry to the client
    public WeightEntryResponse updateWeightEntry(
            @PathVariable long id,
            @Valid @RequestBody UpdateWeightEntryRequest request
    ) {
        AuthGuard.requireSelf(weightEntryService.getWeightEntryById(id).getUserId());
        return weightEntryService.updateWeightEntry(id, request);
    }

    @GetMapping("/{id}") // handle HTTP GET requests to retrieve a specific weight entry by its ID, validating that the entry exists and returning a response DTO representing the weight entry to the client when they access the relevant endpoint in the application
    public WeightEntryResponse getWeightEntryById(@PathVariable long id) {
        WeightEntryResponse entry = weightEntryService.getWeightEntryById(id);
        AuthGuard.requireSelf(entry.getUserId());
        return entry;
    }

    @GetMapping("/user/{userId}") // handle HTTP GET requests to retrieve weight entries for a specific user, optionally filtered by a date range specified by the "from" and "to" query parameters, allowing clients to fetch weight entries for a user and narrow down the results based on a specific time period when they access the relevant endpoint in the application
    public List<WeightEntryResponse> getWeightEntriesForUser(
            @PathVariable long userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        AuthGuard.requireSelf(userId);
        return weightEntryService.getWeightEntriesForUser(userId, from, to);
    }
 
    @GetMapping("/user/{userId}/latest") // handle HTTP GET requests to retrieve the most recent weight entry for a specific user, validating that the entry exists and returning a response DTO representing the latest weight entry to the client when they access the relevant endpoint in the application
    public WeightEntryResponse getLatestForUser(@PathVariable long userId) {
        AuthGuard.requireSelf(userId);
        return weightEntryService.getLatestForUser(userId);
    }
}
