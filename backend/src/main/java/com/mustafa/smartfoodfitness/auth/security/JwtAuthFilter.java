package com.mustafa.smartfoodfitness.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mustafa.smartfoodfitness.auth.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final TokenVersionService tokenVersionService;

  public JwtAuthFilter(JwtService jwtService, TokenVersionService tokenVersionService) {
    this.jwtService = jwtService;
    this.tokenVersionService = tokenVersionService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth == null || !auth.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = auth.substring(7).trim();

    try {
      Claims claims = jwtService.parseClaims(token);
      String email = claims.getSubject();
      String role = (String) claims.get("role");
      Long userId = claims.get("userId", Long.class);
      int tokenVersion = jwtService.readTokenVersion(claims);

      // A token superseded by a logout or password change leaves the context
      // unauthenticated, so Spring Security answers 401 — the signal the mobile
      // client uses to drop stored auth and return to the login screen.
      boolean versionCurrent;
      try {
        versionCurrent = tokenVersionService.isCurrent(userId, tokenVersion);
      } catch (DataAccessException ex) {
        // The signature and expiry already passed; this lookup is the defence-in-depth
        // layer. A database blip must not be read as "revoked", which would sign every
        // active user out on the client's next 401.
        versionCurrent = true;
      }

      if (email != null
          && SecurityContextHolder.getContext().getAuthentication() == null
          && versionCurrent) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(email, userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
            );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception ex) {
    }

    filterChain.doFilter(request, response);
  }
}
