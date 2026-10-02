package com.mustafa.smartfoodfitness.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.dto.AuthResponse;
import com.mustafa.smartfoodfitness.auth.dto.ChangePasswordRequest;
import com.mustafa.smartfoodfitness.auth.dto.LoginRequest;
import com.mustafa.smartfoodfitness.auth.dto.RegisterRequest;
import com.mustafa.smartfoodfitness.auth.security.AuthGuard;
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

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    authService.logout(requireCurrentUserId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/change-password")
  public ResponseEntity<Map<String, Object>> changePassword(
      @Valid @RequestBody ChangePasswordRequest req
  ) {
    authService.changePassword(requireCurrentUserId(), req);
    return ResponseEntity.ok(Map.of());
  }

  /**
   * /api/auth/** is permitAll, so these two endpoints are reached even without a
   * valid token — the identity has to come from the JWT and be checked by hand.
   */
  private static Long requireCurrentUserId() {
    Long userId = AuthGuard.currentUserId();
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
    }
    return userId;
  }
}
