package com.mustafa.smartfoodfitness.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mustafa.smartfoodfitness.auth.dto.AuthResponse;
import com.mustafa.smartfoodfitness.auth.dto.LoginRequest;
import com.mustafa.smartfoodfitness.auth.dto.RegisterRequest;
import com.mustafa.smartfoodfitness.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
    return authService.register(req);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    return authService.login(req);
  }
}
