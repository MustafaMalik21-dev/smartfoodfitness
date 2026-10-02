package com.mustafa.smartfoodfitness.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mustafa.smartfoodfitness.auth.service.JwtService;

import io.jsonwebtoken.Claims;

/**
 * The userId claim round-trip underpins every ownership check: JwtAuthFilter
 * reads it as a Long, and AuthGuard compares it to the userId in the request.
 * JSON numbers deserialize as Integer when small, so this guards against the
 * conversion silently yielding null and locking every user out with a 403.
 */
class JwtUserIdClaimTest {

  private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long-for-hmac-sha";

  @Test
  void userIdClaimSurvivesRoundTripAsLong() {
    JwtService jwtService = new JwtService(SECRET, 3600);

    String token = jwtService.createToken("user@example.com", 7L, "USER");
    Claims claims = jwtService.parseClaims(token);

    assertEquals(7L, claims.get("userId", Long.class));
    assertEquals("user@example.com", claims.getSubject());
  }

  @Test
  void largeUserIdSurvivesRoundTrip() {
    JwtService jwtService = new JwtService(SECRET, 3600);

    long bigId = 9_000_000_000L; // beyond Integer range
    Claims claims = jwtService.parseClaims(jwtService.createToken("a@b.com", bigId, "USER"));

    assertEquals(bigId, claims.get("userId", Long.class));
  }
}
