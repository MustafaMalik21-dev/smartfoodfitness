package com.mustafa.smartfoodfitness.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  public static final String TOKEN_VERSION_CLAIM = "tokenVersion";

  /** Tokens issued before token versioning existed carry no claim and count as this. */
  public static final int LEGACY_TOKEN_VERSION = 0;

  private final SecretKey key;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expirationSeconds}") long expirationSeconds
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  public String createToken(String email, Long userId, String role) {
    return createToken(email, userId, role, LEGACY_TOKEN_VERSION);
  }

  public String createToken(String email, Long userId, String role, Integer tokenVersion) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expirationSeconds);

    return Jwts.builder()
        .subject(email)
        .claim("userId", userId)
        .claim("role", role)
        // Written as an int so it always deserializes as Integer, never Long.
        .claim(TOKEN_VERSION_CLAIM, tokenVersion == null ? LEGACY_TOKEN_VERSION : tokenVersion.intValue())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  /** Version carried by a token; 0 for every token issued before this claim existed. */
  public int readTokenVersion(Claims claims) {
    Integer version = claims.get(TOKEN_VERSION_CLAIM, Integer.class);
    return version == null ? LEGACY_TOKEN_VERSION : version;
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
