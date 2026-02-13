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
public class OnboardingService {

  private final UserProfileRepository userProfileRepository;
  private final UserGoalsRepository userGoalsRepository;
  private final WeightEntryService weightEntryService;

  public OnboardingService(
      UserProfileRepository userProfileRepository,
      UserGoalsRepository userGoalsRepository,
      WeightEntryService weightEntryService
  ) {
    this.userProfileRepository = userProfileRepository;
    this.userGoalsRepository = userGoalsRepository;
    this.weightEntryService = weightEntryService;
  }

  @Transactional
  public OnboardingResponse complete(OnboardingRequest req) {
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
