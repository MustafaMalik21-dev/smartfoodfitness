package com.mustafa.smartfoodfitness.auth.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts per email address.
 * After MAX_ATTEMPTS consecutive failures the account is locked for LOCK_MINUTES.
 * State is in-memory only — clears on restart (acceptable for a beta / FYP context).
 */
@Service
public class LoginAttemptService {

    private static final int  MAX_ATTEMPTS        = 5;
    private static final long LOCK_DURATION_SECS  = 15 * 60L; // 15 minutes

    private record Attempt(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, Attempt> store = new ConcurrentHashMap<>();

    // ── public API ───────────────────────────────────────────────────────────

    /** Call after a successful login — clears the failure counter. */
    public void recordSuccess(String email) {
        store.remove(normalise(email));
    }

    /** Call after a failed login attempt. */
    public void recordFailure(String email) {
        String key = normalise(email);
        store.merge(key,
                new Attempt(1, null),
                (existing, ignored) -> {
                    int next = existing.count() + 1;
                    Instant lock = (next >= MAX_ATTEMPTS)
                            ? Instant.now().plusSeconds(LOCK_DURATION_SECS)
                            : null;
                    return new Attempt(next, lock);
                });
    }

    /**
     * Returns true if the account is currently locked.
     * Automatically clears an expired lock.
     */
    public boolean isLocked(String email) {
        Attempt a = store.get(normalise(email));
        if (a == null || a.lockedUntil() == null) return false;
        if (Instant.now().isBefore(a.lockedUntil())) return true;
        store.remove(normalise(email)); // lock has expired
        return false;
    }

    /** Remaining seconds until the lock expires (0 if not locked). */
    public long secondsUntilUnlock(String email) {
        Attempt a = store.get(normalise(email));
        if (a == null || a.lockedUntil() == null) return 0;
        long remaining = Instant.now().until(a.lockedUntil(), java.time.temporal.ChronoUnit.SECONDS);
        return Math.max(0, remaining);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
