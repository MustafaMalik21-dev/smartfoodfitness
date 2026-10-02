package com.mustafa.smartfoodfitness.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // ── CSRF ── disabled (stateless JWT API, not browser-session based)
        .csrf(csrf -> csrf.disable())

        // ── CORS ── delegated to CorsConfig bean
        .cors(Customizer.withDefaults())

        // ── Session ── stateless; no HttpSession ever created
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // ── Security headers ──────────────────────────────────────────────
        .headers(headers -> headers
            // Prevent MIME-type sniffing
            .contentTypeOptions(Customizer.withDefaults())
            // Prevent clickjacking
            .frameOptions(frame -> frame.deny())
            // HSTS — tell browsers to always use HTTPS (1 year)
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31_536_000))
            // Don't send the full Referer URL to external sites
            .referrerPolicy(ref -> ref
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        )

        // ── Route authorisation ───────────────────────────────────────────
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated()
        )

        // ── Unauthenticated requests ──────────────────────────────────────
        // Spring's default entry point answers 403, but the mobile client only drops
        // its stored auth and returns to the login screen on 401. A missing, malformed,
        // expired or superseded token must therefore come back as 401. Requests that
        // are authenticated but not permitted still fail with 403 via AuthGuard.
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        )

        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
