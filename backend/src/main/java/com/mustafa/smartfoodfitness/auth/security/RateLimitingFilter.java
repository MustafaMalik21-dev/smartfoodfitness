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
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servlet-level rate-limiting filter.
 *
 * Registered without an explicit order, so it runs *after* the Spring Security chain.
 * /api/auth/** is permitAll and therefore still reaches it, which is what matters —
 * credential brute-force is throttled. Requests to protected paths bearing a bad token are
 * rejected by the security chain first and never consume a bucket; the cost of those is one
 * HMAC verification. Giving this filter an explicit order ahead of Spring Security would
 * close that gap, at the cost of also metering CORS preflights.
 *
 * Limits per client IP:
 *   POST /api/auth/login    → 5 requests / minute
 *   POST /api/auth/register → 3 requests / minute
 *   /api/ai/**              → 20 requests / minute (each request costs money)
 *   Everything else         → 200 requests / minute
 *
 * Buckets are held in memory and swept periodically so that a scan of the public
 * endpoints cannot grow the maps without bound on the 512 MB instance.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final int LOGIN_CAPACITY    = 5;
    private static final int REGISTER_CAPACITY = 3;
    private static final int AI_CAPACITY       = 20;
    private static final int GENERAL_CAPACITY  = 200;

    /**
     * Must stay comfortably longer than REFILL_PERIOD: dropping an entry hands the caller a
     * full bucket, so an entry may only be discarded once it has been idle long enough that
     * the bandwidth would have refilled to capacity anyway.
     */
    private static final long ENTRY_TTL_NANOS = Duration.ofMinutes(15).toNanos();
    private static final long MIN_EVICTABLE_IDLE_NANOS = REFILL_PERIOD.toNanos();

    /** Backstop against a burst of distinct IPs arriving faster than the TTL retires them. */
    private static final int MAX_ENTRIES_PER_MAP = 20_000;

    private static final long SWEEP_INTERVAL_NANOS = Duration.ofMinutes(1).toNanos();

    private final ConcurrentHashMap<String, TrackedBucket> loginBuckets    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TrackedBucket> registerBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TrackedBucket> aiBuckets       = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TrackedBucket> generalBuckets  = new ConcurrentHashMap<>();

    private final AtomicLong nextSweepAt = new AtomicLong(System.nanoTime() + SWEEP_INTERVAL_NANOS);

    /** A bucket plus the last time it was handed out, so idle entries can be retired. */
    private static final class TrackedBucket {
        private final Bucket bucket;
        private volatile long lastAccessNanos;

        private TrackedBucket(Bucket bucket, long nowNanos) {
            this.bucket = bucket;
            this.lastAccessNanos = nowNanos;
        }
    }

    // ── bucket factories ─────────────────────────────────────────────────────

    private Bucket loginBucket(String ip) {
        return bucketFor(loginBuckets, ip, LOGIN_CAPACITY);
    }

    private Bucket registerBucket(String ip) {
        return bucketFor(registerBuckets, ip, REGISTER_CAPACITY);
    }

    private Bucket aiBucket(String ip) {
        return bucketFor(aiBuckets, ip, AI_CAPACITY);
    }

    private Bucket generalBucket(String ip) {
        return bucketFor(generalBuckets, ip, GENERAL_CAPACITY);
    }

    private static Bucket bucketFor(ConcurrentHashMap<String, TrackedBucket> buckets, String ip, int capacity) {
        long now = System.nanoTime();
        TrackedBucket tracked = buckets.computeIfAbsent(ip, k -> new TrackedBucket(Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, REFILL_PERIOD)
                        .build())
                .build(), now));
        tracked.lastAccessNanos = now;
        return tracked.bucket;
    }

    // ── filter logic ─────────────────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        maybeSweep();

        String ip   = resolveClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket = switch (path) {
            case "/api/auth/login"    -> loginBucket(ip);
            case "/api/auth/register" -> registerBucket(ip);
            default                   -> path.startsWith("/api/ai/") ? aiBucket(ip) : generalBucket(ip);
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

    /**
     * server.forward-headers-strategy=native puts Tomcat's RemoteIpValve in front of the filter
     * chain, so getRemoteAddr() already returns the proxy-reported client IP — and only when the
     * immediate peer is a trusted proxy. Reading X-Forwarded-For here instead would trust the
     * header on every request, letting a caller mint a fresh bucket per request.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return (ip != null && !ip.isBlank()) ? ip : "unknown";
    }

    // ── eviction ─────────────────────────────────────────────────────────────

    /** Sweeps at most once per SWEEP_INTERVAL_NANOS, on whichever request happens to be due. */
    private void maybeSweep() {
        long now = System.nanoTime();
        long due = nextSweepAt.get();
        if (now - due < 0) return;
        if (!nextSweepAt.compareAndSet(due, now + SWEEP_INTERVAL_NANOS)) return;

        sweep(loginBuckets, now);
        sweep(registerBuckets, now);
        sweep(aiBuckets, now);
        sweep(generalBuckets, now);
    }

    private static void sweep(ConcurrentHashMap<String, TrackedBucket> buckets, long now) {
        buckets.values().removeIf(t -> now - t.lastAccessNanos >= ENTRY_TTL_NANOS);

        if (buckets.size() <= MAX_ENTRIES_PER_MAP) return;

        long[] stamps = buckets.values().stream().mapToLong(t -> t.lastAccessNanos).toArray();
        if (stamps.length <= MAX_ENTRIES_PER_MAP) return;
        Arrays.sort(stamps);
        long cutoff = stamps[stamps.length - MAX_ENTRIES_PER_MAP];

        // Coldest first, and never an entry younger than the refill period — an entry that
        // recent still holds consumed tokens, so discarding it would reset a live limit.
        buckets.values().removeIf(t -> t.lastAccessNanos < cutoff
                && now - t.lastAccessNanos >= MIN_EVICTABLE_IDLE_NANOS);
    }
}
