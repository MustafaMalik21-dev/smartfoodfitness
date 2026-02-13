package com.mustafa.smartfoodfitness.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.dto.AuthResponse;
import com.mustafa.smartfoodfitness.auth.dto.LoginRequest;
import com.mustafa.smartfoodfitness.auth.dto.RegisterRequest;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

@Service
public class AuthService {

  private final UserProfileRepository repo;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;

  public AuthService(UserProfileRepository repo, PasswordEncoder encoder, JwtService jwtService) {
    this.repo = repo;
    this.encoder = encoder;
    this.jwtService = jwtService;
  }

  public AuthResponse register(RegisterRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    if (repo.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use.");
    }

    UserProfile u = new UserProfile();
    u.setEmail(email);
    u.setDisplayName(req.getDisplayName().trim());
    u.setPasswordHash(encoder.encode(req.getPassword()));
    u.setRole("USER");

    UserProfile saved = repo.save(u);

    String token = jwtService.createToken(saved.getEmail(), saved.getId(), saved.getRole());

    AuthResponse r = new AuthResponse();
    r.setUserId(saved.getId());
    r.setEmail(saved.getEmail());
    r.setDisplayName(saved.getDisplayName());
    r.setToken(token);
    r.setOnboardingComplete(saved.getOnboardingComplete());

    return r;
  }

  public AuthResponse login(LoginRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    UserProfile u = repo.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

    if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
    }

    String token = jwtService.createToken(u.getEmail(), u.getId(), u.getRole());

    AuthResponse r = new AuthResponse();
    r.setUserId(u.getId());
    r.setEmail(u.getEmail());
    r.setDisplayName(u.getDisplayName());
    r.setToken(token);
    r.setOnboardingComplete(u.getOnboardingComplete());
    return r;
  }
}
