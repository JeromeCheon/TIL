package me.jeromecheon.spring4blogproject.service;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    return this.userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException(email));
  }
}
