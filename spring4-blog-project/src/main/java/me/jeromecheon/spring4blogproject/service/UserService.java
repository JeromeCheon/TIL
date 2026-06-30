package me.jeromecheon.spring4blogproject.service;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.User;
import me.jeromecheon.spring4blogproject.dto.AddUserRequest;
import me.jeromecheon.spring4blogproject.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository userRepository;

  public Long save(AddUserRequest dto) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return userRepository.save(User.builder()
            .email(dto.getEmail())
            .password(encoder.encode(dto.getPassword()))
            .build()).getId();
  }

  public User findById(Long userId) {
    return this.userRepository.findById(userId)
            .orElseThrow(()-> new IllegalArgumentException("Unexpected user"));
  }

  public User findByEmail(String email) {
    return this.userRepository.findByEmail(email)
            .orElseThrow(()-> new IllegalArgumentException("Unexpected user"));
  }
}
