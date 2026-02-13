package com.mustafa.smartfoodfitness.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.dto.OnboardingRequest;
import com.mustafa.smartfoodfitness.dto.OnboardingResponse;
import com.mustafa.smartfoodfitness.service.OnboardingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

  private final OnboardingService onboardingService;

  public OnboardingController(OnboardingService onboardingService) {
    this.onboardingService = onboardingService;
  }

  @PostMapping("/complete")
  public OnboardingResponse complete(@Valid @RequestBody OnboardingRequest req) {
    return onboardingService.complete(req);
  }
}
