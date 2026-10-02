package com.mustafa.smartfoodfitness.auth.service;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mustafa.smartfoodfitness.auth.dto.AuthResponse;
import com.mustafa.smartfoodfitness.auth.dto.ChangePasswordRequest;
import com.mustafa.smartfoodfitness.auth.dto.LoginRequest;
import com.mustafa.smartfoodfitness.auth.dto.RegisterRequest;
import com.mustafa.smartfoodfitness.auth.security.LoginAttemptService;
import com.mustafa.smartfoodfitness.auth.security.TokenVersionService;
import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

@Service
public class AuthService {

  // Character classes mirror PW_RULES in the mobile RegisterScreen exactly — they are
  // deliberately ASCII, so a password the sign-up form accepts is never rejected here.
  private static final Pattern PW_UPPER   = Pattern.compile("[A-Z]");
  private static final Pattern PW_LOWER   = Pattern.compile("[a-z]");
  private static final Pattern PW_DIGIT   = Pattern.compile("[0-9]");
  private static final Pattern PW_SPECIAL = Pattern.compile("[^A-Za-z0-9]");

  private static final String PASSWORD_POLICY_MESSAGE =
      "Password must be at least 8 characters and include an uppercase letter, " +
      "a lowercase letter, a number and a special character.";

  private final UserProfileRepository repo;
  private final PasswordEncoder        encoder;
  private final JwtService             jwtService;
  private final LoginAttemptService    loginAttemptService;
  private final TokenVersionService    tokenVersionService;

  public AuthService(UserProfileRepository repo,
                     PasswordEncoder encoder,
                     JwtService jwtService,
                     LoginAttemptService loginAttemptService,
                     TokenVersionService tokenVersionService) {
    this.repo               = repo;
    this.encoder            = encoder;
    this.jwtService         = jwtService;
    this.loginAttemptService = loginAttemptService;
    this.tokenVersionService = tokenVersionService;
  }

  /**
   * Checks whether the email's domain exists in DNS.
   * Returns false only when the domain is definitively NXDOMAIN.
   * Any network/timeout error is treated as permissive so legitimate users
   * are never blocked by a transient DNS failure.
   */
  private static boolean emailDomainExists(String email) {
    try {
      String domain = email.substring(email.lastIndexOf('@') + 1);
      Hashtable<String, String> env = new Hashtable<>();
      env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
      env.put("com.sun.jndi.dns.timeout.initial", "3000");
      env.put("com.sun.jndi.dns.timeout.retries", "1");
      new InitialDirContext(env).getAttributes(domain, new String[]{"MX"});
      return true;
    } catch (javax.naming.NameNotFoundException e) {
      return false; // NXDOMAIN — domain doesn't exist
    } catch (NamingException e) {
      return true;  // timeout or resolver error — be permissive
    }
  }

  public AuthResponse register(RegisterRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    if (!emailDomainExists(email)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "The email domain doesn't exist. Please use a real email address.");
    }

    if (repo.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use.");
    }

    UserProfile u = new UserProfile();
    u.setEmail(email);
    u.setDisplayName(req.getDisplayName().trim());
    u.setPasswordHash(encoder.encode(req.getPassword()));
    u.setRole("USER");

    UserProfile saved = repo.save(u);

    String token = jwtService.createToken(
        saved.getEmail(), saved.getId(), saved.getRole(), saved.getTokenVersion());

    AuthResponse r = new AuthResponse();
    r.setUserId(saved.getId());
    r.setEmail(saved.getEmail());
    r.setDisplayName(saved.getDisplayName());
    r.setToken(token);
    r.setOnboardingComplete(saved.getOnboardingComplete());

    return r;
  }

  public AuthResponse login(LoginRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    // ── Account lockout check ────────────────────────────────────────────────
    if (loginAttemptService.isLocked(email)) {
      long secs = loginAttemptService.secondsUntilUnlock(email);
      long mins  = (long) Math.ceil(secs / 60.0);
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
          "Account temporarily locked due to too many failed attempts. " +
          "Try again in " + mins + " minute" + (mins == 1 ? "" : "s") + ".");
    }

    // ── Credential check ────────────────────────────────────────────────────
    UserProfile u = repo.findByEmail(email).orElse(null);

    if (u == null || !encoder.matches(req.getPassword(), u.getPasswordHash())) {
      // Record failure regardless of whether the email exists —
      // prevents user-enumeration via timing difference.
      loginAttemptService.recordFailure(email);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
    }

    // ── Success ─────────────────────────────────────────────────────────────
    loginAttemptService.recordSuccess(email);

    String token = jwtService.createToken(
        u.getEmail(), u.getId(), u.getRole(), tokenVersionService.currentVersion(u.getId()));

    AuthResponse r = new AuthResponse();
    r.setUserId(u.getId());
    r.setEmail(u.getEmail());
    r.setDisplayName(u.getDisplayName());
    r.setToken(token);
    r.setOnboardingComplete(u.getOnboardingComplete());
    return r;
  }

  /** Invalidates every token currently held by the user, including the caller's. */
  public void logout(Long userId) {
    tokenVersionService.bump(userId);
  }

  // One transaction: the new hash and the version bump that retires the old sessions have to
  // land together, or a failure between them leaves the password changed but old tokens live.
  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest req) {
    UserProfile u = repo.findById(userId).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in."));

    if (!encoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
    }

    if (!meetsPasswordPolicy(req.getNewPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_POLICY_MESSAGE);
    }

    u.setPasswordHash(encoder.encode(req.getNewPassword()));
    repo.save(u);

    // Any session still holding the old password's token is now stale.
    tokenVersionService.bump(userId);
  }

  private static boolean meetsPasswordPolicy(String password) {
    if (password == null || password.length() < 8) return false;
    return PW_UPPER.matcher(password).find()
        && PW_LOWER.matcher(password).find()
        && PW_DIGIT.matcher(password).find()
        && PW_SPECIAL.matcher(password).find();
  }
}
