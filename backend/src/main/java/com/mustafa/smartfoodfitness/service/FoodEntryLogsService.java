package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateFoodEntryLogsRequest;
import com.mustafa.smartfoodfitness.dto.FoodEntryLogsResponse;
import com.mustafa.smartfoodfitness.dto.UpdateFoodEntryLogsRequest;
import com.mustafa.smartfoodfitness.entity.FoodEntryLogs;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.FoodEntryLogsRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;


@Service
public class FoodEntryLogsService {

    private final FoodEntryLogsRepository foodEntryLogsRepository;
    private final UserProfileRepository userProfileRepository;

    public FoodEntryLogsService(
            FoodEntryLogsRepository foodEntryLogsRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.foodEntryLogsRepository = foodEntryLogsRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public FoodEntryLogsResponse createFoodEntry(CreateFoodEntryLogsRequest request) { // create a new food entry log for a user, validating the input and ensuring that the associated user profile exists, then saving the new entry to the database and returning a response DTO representing the saved entry
        Long userId = request.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }

        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        FoodEntryLogs entity = new FoodEntryLogs(); // create a new FoodEntryLogs entity and populate it with data from the request, including setting the user profile, food name, weight value, weight unit, calories, macronutrients, meal type, and logged timestamp before saving it to the database and returning a response DTO representing the saved entry
        entity.setUserProfile(userProfile);
        entity.setFoodName(request.getFoodName());
        entity.setWeightValue(request.getWeightValue());
        entity.setWeightUnit(request.getWeightUnit());
        entity.setCalories(request.getCalories());
        entity.setProteins(request.getProteins());
        entity.setCarbs(request.getCarbs());
        entity.setFats(request.getFats());
        entity.setMealType(request.getMealType());
        entity.setLoggedAt(request.getLoggedAt());

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        FoodEntryLogs saved = foodEntryLogsRepository.save(entity); // save the new food entry log to the database and return a response DTO representing the saved entry
        return mapToResponse(saved);
    }

    public FoodEntryLogsResponse updateFoodEntry(long id, UpdateFoodEntryLogsRequest request) { // update an existing food entry log by its ID, validating the input and ensuring that the entry exists, then saving the updated entry to the database and returning a response DTO representing the updated entry
        FoodEntryLogs entity = foodEntryLogsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food entry not found."));

        entity.setFoodName(request.getFoodName()); // update the fields of the existing FoodEntryLogs entity with data from the request, including the food name, weight value, weight unit, calories, macronutrients, meal type, and logged timestamp before saving the updated entry to the database and returning a response DTO representing the updated entry
        entity.setWeightValue(request.getWeightValue());
        entity.setWeightUnit(request.getWeightUnit());
        entity.setCalories(request.getCalories());
        entity.setProteins(request.getProteins());
        entity.setCarbs(request.getCarbs());
        entity.setFats(request.getFats());
        entity.setMealType(request.getMealType());
        entity.setLoggedAt(request.getLoggedAt());

        entity.setUpdatedAt(Instant.now());

        FoodEntryLogs saved = foodEntryLogsRepository.save(entity); // save the updated food entry log to the database and return a response DTO representing the updated entry
        return mapToResponse(saved);
    }

    public FoodEntryLogsResponse getFoodEntryById(long id) { // retrieve a food entry log by its ID, validating that the entry exists and returning a response DTO representing the retrieved entry
        FoodEntryLogs entity = foodEntryLogsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food entry not found."));
        return mapToResponse(entity);
    }

    public List<FoodEntryLogsResponse> getFoodEntriesForUser(long userId, Instant from, Instant to) {
        List<FoodEntryLogs> logs;

        if (from != null && to != null) { // retrieve food entry logs for a user, optionally filtered by a date range, by querying the database for entries associated with the user's profile and ordered by the logged timestamp in descending order, then mapping the retrieved entries to response DTOs before returning the list
            logs = foodEntryLogsRepository
                    .findByUserProfileIdAndLoggedAtBetweenOrderByLoggedAtDesc(userId, from, to);
        } else {
            logs = foodEntryLogsRepository
                    .findByUserProfileIdOrderByLoggedAtDesc(userId);
        }

        return logs.stream().map(this::mapToResponse).toList();
    }

    private FoodEntryLogsResponse mapToResponse(FoodEntryLogs saved) { // helper method to convert a FoodEntryLogs entity to a FoodEntryLogsResponse DTO by creating a new FoodEntryLogsResponse instance and populating it with data from the FoodEntryLogs entity, including the ID, user ID, food name, weight value, weight unit, calories, macronutrients, meal type, logged timestamp, createdAt timestamp, and updatedAt timestamp before returning the populated DTO
        FoodEntryLogsResponse r = new FoodEntryLogsResponse();
        r.setId(saved.getId());
        r.setUserId(saved.getUserProfile().getId());
        r.setFoodName(saved.getFoodName());
        r.setWeightValue(saved.getWeightValue());
        r.setWeightUnit(saved.getWeightUnit());
        r.setCalories(saved.getCalories());
        r.setProteins(saved.getProteins());
        r.setCarbs(saved.getCarbs());
        r.setFats(saved.getFats());
        r.setMealType(saved.getMealType());
        r.setLoggedAt(saved.getLoggedAt());
        r.setCreatedAt(saved.getCreatedAt());
        r.setUpdatedAt(saved.getUpdatedAt());
        return r;
    }
}
