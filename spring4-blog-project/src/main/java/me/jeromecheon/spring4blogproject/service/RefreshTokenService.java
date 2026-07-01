package me.jeromecheon.spring4blogproject.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.RefreshToken;
import me.jeromecheon.spring4blogproject.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken findByRefreshToken(String refreshToken) {
    return refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(()-> new IllegalArgumentException("Unexpected refresh token"));
  }

  @Transactional
  public void deleteByUserId(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }
}
