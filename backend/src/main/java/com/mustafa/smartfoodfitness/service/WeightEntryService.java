package com.mustafa.smartfoodfitness.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.CreateWeightEntryRequest;
import com.mustafa.smartfoodfitness.dto.UpdateWeightEntryRequest;
import com.mustafa.smartfoodfitness.dto.WeightEntryResponse;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.entity.WeightEntry;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;
import com.mustafa.smartfoodfitness.repository.WeightEntryRepository;

@Service
public class WeightEntryService {

    private final WeightEntryRepository weightEntryRepository;
    private final UserProfileRepository userProfileRepository;

    public WeightEntryService(WeightEntryRepository weightEntryRepository, UserProfileRepository userProfileRepository) { // constructor injection of repositories
        this.weightEntryRepository = weightEntryRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public WeightEntryResponse createWeightEntry(CreateWeightEntryRequest request) { // create a new weight entry for a user, validating the input and ensuring that the associated user profile exists, then saving the new entry to the database and returning a response DTO representing the saved entry
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required.");
        }
        boolean skipWeight = "scan".equalsIgnoreCase(request.getSource())
                          || "measurement".equalsIgnoreCase(request.getSource());
        if (!skipWeight && request.getWeightValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weightValue is required.");
        }
        if (request.getWeightUnit() == null || request.getWeightUnit().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weightUnit is required.");
        }

        UserProfile user = userProfileRepository.findById(request.getUserId()) // validate that the user profile associated with the weight entry exists by fetching it from the database using the provided user ID, throwing a 404 Not Found error if it does not exist
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));
 
        WeightEntry e = new WeightEntry(); // create a new WeightEntry entity and populate it with data from the request, including setting the user profile, weight value, weight unit, and recorded timestamp (defaulting to the current time if not provided)
        e.setUserProfile(user);
        e.setWeightValue(request.getWeightValue() != null ? request.getWeightValue() : 0.0);
        e.setWeightUnit(request.getWeightUnit().trim().toLowerCase());
        e.setRecordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : Instant.now());
        e.setBodyFatPercent(request.getBodyFatPercent());
        e.setProteinPercent(request.getProteinPercent());
        e.setMuscleMassKg(request.getMuscleMassKg());
        e.setVisceralFatLevel(request.getVisceralFatLevel());
        e.setBmi(request.getBmi());
        e.setBoneMassKg(request.getBoneMassKg());
        e.setWaterPercent(request.getWaterPercent());
        e.setBmr(request.getBmr());
        e.setWaistCm(request.getWaistCm());
        e.setHipCm(request.getHipCm());
        e.setChestCm(request.getChestCm());
        e.setNeckCm(request.getNeckCm());
        e.setShoulderCm(request.getShoulderCm());
        e.setLeftBicepCm(request.getLeftBicepCm());
        e.setRightBicepCm(request.getRightBicepCm());
        e.setLeftForearmCm(request.getLeftForearmCm());
        e.setRightForearmCm(request.getRightForearmCm());
        e.setAbdomenCm(request.getAbdomenCm());
        e.setLeftThighCm(request.getLeftThighCm());
        e.setRightThighCm(request.getRightThighCm());
        e.setLeftCalfCm(request.getLeftCalfCm());
        e.setRightCalfCm(request.getRightCalfCm());
        e.setSource(request.getSource() != null ? request.getSource() : "manual");

        Instant now = Instant.now(); // set the createdAt and updatedAt timestamps to the current time
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        WeightEntry saved = weightEntryRepository.save(e);

        // Only sync profile weight for true weight entries (not scan/measurement placeholders)
        String src = saved.getSource() != null ? saved.getSource().toLowerCase() : "";
        if (!src.equals("scan") && !src.equals("measurement")) {
            syncUserProfileWeight(user, saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public WeightEntryResponse updateWeightEntry(long id, UpdateWeightEntryRequest request) { // update an existing weight entry by its ID, validating the input and ensuring that the entry exists, then saving the updated entry to the database and returning a response DTO representing the updated entry
        WeightEntry e = weightEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weight entry not found."));

        if (request.getWeightValue() != null) e.setWeightValue(request.getWeightValue()); // update the weight value if provided in the request
        if (request.getWeightUnit() != null && !request.getWeightUnit().isBlank()) {
            e.setWeightUnit(request.getWeightUnit().trim().toLowerCase());
        }
        if (request.getRecordedAt() != null) e.setRecordedAt(request.getRecordedAt()); // update the recorded timestamp if provided in the request

        e.setUpdatedAt(Instant.now());

        WeightEntry saved = weightEntryRepository.save(e); // save the updated weight entry to the database

        WeightEntry latest = weightEntryRepository // check if the updated entry is now the latest for the user, and if so, update the user's profile weight accordingly by fetching the latest weight entry for the user and comparing it to the updated entry
                .findTopByUserProfileIdOrderByRecordedAtDesc(saved.getUserProfile().getId())
                .orElse(saved);

        syncUserProfileWeight(saved.getUserProfile(), latest); 

        return toResponse(saved);
    }

    @Transactional(readOnly = true) // retrieve a weight entry by its ID, throwing a 404 Not Found error if it does not exist, and returning a response DTO representing the entry
    public WeightEntryResponse getWeightEntryById(long id) {
        WeightEntry e = weightEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weight entry not found."));
        return toResponse(e);
    }

    @Transactional(readOnly = true) // retrieve weight entries for a user, optionally filtered by a date range, by fetching the relevant entries from the database based on the provided user ID and date range parameters, and returning a list of response DTOs representing the retrieved entries
    public List<WeightEntryResponse> getWeightEntriesForUser(long userId, Instant from, Instant to) {
        List<WeightEntry> list;

        if (from != null && to != null) {
            list = weightEntryRepository.findByUserProfileIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, from, to);
        } else {
            list = weightEntryRepository.findByUserProfileIdOrderByRecordedAtAsc(userId);
        }

        return list.stream().map(this::toResponse).toList(); // convert the list of WeightEntry entities to a list of WeightEntryResponse DTOs using a stream and the toResponse helper method
    }

    @Transactional(readOnly = true) // retrieve the latest weight entry for a user by fetching the most recent entry from the database based on the provided user ID, throwing a 404 Not Found error if no entries exist for the user, and returning a response DTO representing the latest entry
    public WeightEntryResponse getLatestForUser(long userId) {
        WeightEntry e = weightEntryRepository.findTopByUserProfileIdOrderByRecordedAtDesc(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No weight entries for user."));
        return toResponse(e);
    }

    private void syncUserProfileWeight(UserProfile user, WeightEntry latest) { // helper method to synchronize the user's profile weight with the latest weight entry, updating the user's profile weight value and unit if the provided latest entry is more recent than the current profile weight, and saving the updated user profile to the database
        user.setWeightValue(latest.getWeightValue()); // update the user's profile weight value and unit to match the latest weight entry, and save the updated user profile to the database
        user.setWeightUnit(latest.getWeightUnit());
        user.setUpdatedAt(Instant.now());
        userProfileRepository.save(user);
    }

    private WeightEntryResponse toResponse(WeightEntry e) { // helper method to convert a WeightEntry entity to a WeightEntryResponse DTO by copying the relevant fields from the entity to the DTO and returning the DTO
        WeightEntryResponse r = new WeightEntryResponse();
        r.setId(e.getId());
        r.setUserId(e.getUserProfile().getId());
        r.setWeightValue(e.getWeightValue());
        r.setWeightUnit(e.getWeightUnit());
        r.setRecordedAt(e.getRecordedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setBodyFatPercent(e.getBodyFatPercent());
        r.setProteinPercent(e.getProteinPercent());
        r.setMuscleMassKg(e.getMuscleMassKg());
        r.setVisceralFatLevel(e.getVisceralFatLevel());
        r.setBmi(e.getBmi());
        r.setBoneMassKg(e.getBoneMassKg());
        r.setWaterPercent(e.getWaterPercent());
        r.setBmr(e.getBmr());
        r.setWaistCm(e.getWaistCm());
        r.setHipCm(e.getHipCm());
        r.setChestCm(e.getChestCm());
        r.setNeckCm(e.getNeckCm());
        r.setShoulderCm(e.getShoulderCm());
        r.setLeftBicepCm(e.getLeftBicepCm());
        r.setRightBicepCm(e.getRightBicepCm());
        r.setLeftForearmCm(e.getLeftForearmCm());
        r.setRightForearmCm(e.getRightForearmCm());
        r.setAbdomenCm(e.getAbdomenCm());
        r.setLeftThighCm(e.getLeftThighCm());
        r.setRightThighCm(e.getRightThighCm());
        r.setLeftCalfCm(e.getLeftCalfCm());
        r.setRightCalfCm(e.getRightCalfCm());
        r.setSource(e.getSource());
        return r;
    }
}
