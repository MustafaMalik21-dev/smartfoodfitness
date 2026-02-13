package com.mustafa.smartfoodfitness.auth.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mustafa.smartfoodfitness.entity.UserProfile;
import com.mustafa.smartfoodfitness.repository.UserProfileRepository;

@Service
public class EmailUserDetailsService implements UserDetailsService {

  private final UserProfileRepository repo;

  public EmailUserDetailsService(UserProfileRepository repo) {
    this.repo = repo;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserProfile user = repo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPasswordHash())
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
        .build();
  }
}
