package com.mustafa.smartfoodfitness.auth.security;

import java.security.Principal;

/**
 * Authenticated identity extracted from a verified JWT.
 * Carries the userId claim so controllers can enforce resource ownership
 * without an extra database lookup per request.
 */
public record JwtPrincipal(String email, Long userId) implements Principal {

  @Override
  public String getName() {
    return email;
  }
}
