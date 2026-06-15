package com.mustafa.smartfoodfitness.auth.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet-level rate-limiting filter (runs before Spring Security).
 *
 * Limits per client IP:
 *   POST /api/auth/login    → 5 requests / minute
 *   POST /api/auth/register → 3 requests / minute
 *   Everything else         → 200 requests / minute
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> loginBuckets    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> generalBuckets  = new ConcurrentHashMap<>();

    // ── bucket factories ─────────────────────────────────────────────────────

    private Bucket loginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build());
    }

    private Bucket registerBucket(String ip) {
        return registerBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillIntervally(3, Duration.ofMinutes(1))
                        .build())
                .build());
    }

    private Bucket generalBucket(String ip) {
        return generalBuckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(200)
                        .refillIntervally(200, Duration.ofMinutes(1))
                        .build())
                .build());
    }

    // ── filter logic ─────────────────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip   = resolveClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket = switch (path) {
            case "/api/auth/login"    -> loginBucket(ip);
            case "/api/auth/register" -> registerBucket(ip);
            default                   -> generalBucket(ip);
        };

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Rate limit exceeded. Please wait before trying again.\"}");
        }
    }

    /** Honours X-Forwarded-For set by Railway / reverse proxies. */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
