package com.mustafa.smartfoodfitness.auth.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Object-level authorization checks. Every endpoint that takes a userId in the
 * path, query string, or request body must verify it against the JWT identity —
 * otherwise any authenticated user can read or modify any other user's data.
 */
public final class AuthGuard {

  private AuthGuard() {}

  /** The userId claim of the authenticated user, or null when unauthenticated. */
  public static Long currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof JwtPrincipal p) {
      return p.userId();
    }
    return null;
  }

  /** The email (JWT subject) of the authenticated user, or null when unauthenticated. */
  public static String currentEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof JwtPrincipal p) {
      return p.email();
    }
    return null;
  }

  /** Throws 403 unless the given userId belongs to the authenticated user. */
  public static void requireSelf(Long userId) {
    Long current = currentUserId();
    if (userId == null || current == null || !current.equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own data.");
    }
  }

  /** Throws 403 unless the given email belongs to the authenticated user. */
  public static void requireSelfEmail(String email) {
    String current = currentEmail();
    if (email == null || current == null || !current.equalsIgnoreCase(email.trim())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own data.");
    }
  }
}
