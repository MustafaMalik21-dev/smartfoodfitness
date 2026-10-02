package com.mustafa.smartfoodfitness.auth.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

/**
 * Current token version per user.
 *
 * JwtAuthFilter consults this on every authenticated request, so the version is
 * cached in memory: the database is read only on a cache miss, and a bump writes
 * through so invalidation takes effect on the very next request instead of after
 * a TTL. The database stays authoritative — dropping cache entries only costs a
 * reload, which is why the bounded cache can simply discard everything when full.
 */
@Service
public class TokenVersionService {

  /** Rows created before the token_version column existed read back as null. */
  private static final int DEFAULT_VERSION = 0;

  /**
   * Version reported for a user who no longer exists. No issued token can satisfy it, so
   * deleting an account revokes its tokens immediately rather than leaving them usable
   * until they expire.
   */
  private static final int REVOKED_VERSION = Integer.MAX_VALUE;

  private static final int MAX_CACHED_USERS = 5_000;

  private final UserProfileRepository repo;
  private final ConcurrentHashMap<Long, Integer> cache = new ConcurrentHashMap<>();

  public TokenVersionService(UserProfileRepository repo) {
    this.repo = repo;
  }

  /** True when a token's version is still valid for that user. */
  public boolean isCurrent(Long userId, int tokenVersion) {
    if (userId == null) return true;

    int current = currentVersion(userId);
    if (current == REVOKED_VERSION) return false;
    return tokenVersion >= current;
  }

  /** The user's current version, loaded from the database on a cache miss. */
  public int currentVersion(Long userId) {
    if (userId == null) return DEFAULT_VERSION;

    Integer cached = cache.get(userId);
    if (cached != null) return cached;

    int stored = repo.findById(userId)
        .map(u -> normalise(u.getTokenVersion()))
        .orElse(REVOKED_VERSION);
    remember(userId, stored);
    return stored;
  }

  /**
   * Permanently rejects every token belonging to the user. Call when the account itself is
   * gone — unlike {@link #bump}, there is no row left to raise a version on.
   */
  public void revokeAll(Long userId) {
    if (userId == null) return;
    remember(userId, REVOKED_VERSION);
  }

  /**
   * Invalidates every token issued to the user so far.
   * Returns the new version.
   */
  @Transactional
  public int bump(Long userId) {
    if (userId == null) return DEFAULT_VERSION;

    UserProfile u = repo.findById(userId).orElse(null);
    if (u == null) return DEFAULT_VERSION;

    int next = normalise(u.getTokenVersion()) + 1;
    u.setTokenVersion(next);
    repo.save(u);
    remember(userId, next);
    return next;
  }

  // ─────────────────────────────────────────────────────────────────────────

  private void remember(Long userId, int version) {
    if (cache.size() >= MAX_CACHED_USERS && !cache.containsKey(userId)) {
      cache.clear();
    }
    // Versions only ever increase, so a slow database read can never overwrite a
    // newer value written through by a concurrent bump.
    cache.merge(userId, version, Math::max);
  }

  private int normalise(Integer version) {
    return version == null ? DEFAULT_VERSION : version;
  }
}
