package me.jeromecheon.spring4blogproject.service;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.config.jwt.TokenProvider;
import me.jeromecheon.spring4blogproject.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;

  public String createNewAccessToken(String refreshToken) {
    // 토큰 유효성 검사에 실패하면 예외 발생
    if(!this.tokenProvider.validToken(refreshToken)) {
      throw new IllegalArgumentException("Unexpected token");
    }

    Long userId = this.refreshTokenService.findByRefreshToken(refreshToken).getUserId();
    User user = this.userService.findById(userId);

    return this.tokenProvider.generateToken(user, Duration.ofHours(2));
  }

  public void deleteByRefreshToken(String email) {
    User user = this.userService.findByEmail(email);
    this.refreshTokenService.deleteByUserId(user.getId());
  }
}
