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
@RequestMapping("/api/onboarding") //Controller class responsible for handling HTTP requests related to the onboarding process, providing an endpoint for completing the onboarding process, accepting a request body containing the necessary information to complete onboarding, validating the input data, and returning a response DTO representing the result of the onboarding process to the client when they access the relevant endpoint in the application
public class OnboardingController {

  private final OnboardingService onboardingService;

  public OnboardingController(OnboardingService onboardingService) {
    this.onboardingService = onboardingService;
  }

  @PostMapping("/complete") // handle HTTP POST requests to complete the onboarding process, accepting a request body containing the necessary information to complete onboarding, validating the input data, and returning a response DTO representing the result of the onboarding process to the client when they access the relevant endpoint in the application
  public OnboardingResponse complete(@Valid @RequestBody OnboardingRequest req) {
    return onboardingService.complete(req);
  }
}
