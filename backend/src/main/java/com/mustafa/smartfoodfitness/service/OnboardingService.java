package com.mustafa.smartfoodfitness.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.dto.OnboardingRequest;
import com.mustafa.smartfoodfitness.dto.OnboardingResponse;
import com.mustafa.smartfoodfitness.entity.UserGoals;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserGoalsRepository;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

@Service
public class OnboardingService { // Service class responsible for handling the business logic of the onboarding process, providing a method to complete the onboarding process by accepting an OnboardingRequest DTO, validating the associated user profile, updating the user's profile information and goals based on the request data, creating an initial weight entry for the user, marking the onboarding process as complete for the user, and returning an OnboardingResponse DTO representing the result of the onboarding process to be sent back to the client when they access the relevant endpoint in the application

  private final UserProfileRepository userProfileRepository;
  private final UserGoalsRepository userGoalsRepository;
  private final WeightEntryService weightEntryService;

  public OnboardingService( // Constructor for the OnboardingService class, accepting dependencies for the UserProfileRepository, UserGoalsRepository, and WeightEntryService to facilitate database operations related to user profiles and goals, as well as creating weight entries during the onboarding process when users access the relevant endpoint in the application
      UserProfileRepository userProfileRepository,
      UserGoalsRepository userGoalsRepository,
      WeightEntryService weightEntryService
  ) {
    this.userProfileRepository = userProfileRepository;
    this.userGoalsRepository = userGoalsRepository;
    this.weightEntryService = weightEntryService;
  }

  @Transactional
  public OnboardingResponse complete(OnboardingRequest req) { // Method to complete the onboarding process, accepting an OnboardingRequest DTO containing the necessary information to complete onboarding, validating the input data and associated user profile, updating the user's profile and goals based on the request data, creating an initial weight entry for the user, marking the onboarding process as complete, and returning an OnboardingResponse DTO representing the result of the onboarding process to be sent back to the client when they access the relevant endpoint in the application
    UserProfile user = userProfileRepository.findById(req.getUserId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

    user.setAge(req.getAge());
    user.setGender(req.getGender());
    user.setHeightValue(req.getHeightValue());
    user.setHeightUnit(req.getHeightUnit() == null ? null : req.getHeightUnit().trim().toLowerCase());
    user.setActivityLevel(req.getActivityLevel());
    user.setExperienceLevel(req.getExperienceLevel());
    user.setUpdatedAt(Instant.now());

    UserGoals goals = userGoalsRepository.findByUserProfileId(user.getId()).orElse(null);
    if (goals == null) {
      goals = new UserGoals();
      goals.setUserProfile(user);
      goals.setCreatedAt(Instant.now());
    }
    goals.setCalorieGoal(req.getCalorieGoal());
    goals.setProteinGoal(req.getProteinGoal());
    goals.setCarbGoal(req.getCarbGoal());
    goals.setFatGoal(req.getFatGoal());
    goals.setUpdatedAt(Instant.now());
    userGoalsRepository.save(goals);

    weightEntryService.createWeightEntry(new com.mustafa.smartfoodfitness.dto.CreateWeightEntryRequest() {{
      setUserId(user.getId());
      setWeightValue(req.getWeightValue());
      setWeightUnit(req.getWeightUnit());
      setRecordedAt(Instant.now());
    }});

    user.setOnboardingComplete(true);
    userProfileRepository.save(user);

    OnboardingResponse res = new OnboardingResponse();
    res.setUserId(user.getId());
    res.setOnboardingComplete(true);
    return res;
  }
}
