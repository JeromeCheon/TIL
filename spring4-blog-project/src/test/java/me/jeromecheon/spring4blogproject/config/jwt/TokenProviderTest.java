package me.jeromecheon.spring4blogproject.config.jwt;

import io.jsonwebtoken.Jwts;
import me.jeromecheon.spring4blogproject.domain.User;
import me.jeromecheon.spring4blogproject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class TokenProviderTest {
  @Autowired
  private TokenProvider tokenProvider;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JwtProperties jwtProperties;

  @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
  @Test
  void generateToken() {
    // given
    User testUser = this.userRepository.save(User.builder()
            .email("user@gmail.com")
            .password("test")
            .build());
    // when
    String token = this.tokenProvider.generateToken(testUser, Duration.ofDays(14));
    // then
    Long userId = Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey())
            .parseClaimsJws(token)
            .getBody()
            .get("id", Long.class);
    assertThat(userId).isEqualTo(testUser.getId());
  }

  @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
  @Test
  void validToken_invalidToken() {
    // given
    String token = JwtFactory.withDefaultValues().createToken(jwtProperties);
    // when
    boolean result = tokenProvider.validToken(token);
    // then
    assertThat(result).isTrue();
  }

  @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
  @Test
  void getAuthentication() {
    // given
    String userEmail = "user@email.com";
    String token = JwtFactory.builder()
            .subject(userEmail)
            .build()
            .createToken(jwtProperties);

    // when
    Authentication authentication = this.tokenProvider.getAuthentication(token);
    // then
    assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
  }

  @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
  @Test
  void getUserId() {
    // given
    Long userId = 1L;
    String token = JwtFactory.builder()
            .claims(Map.of("id", userId))
            .build()
            .createToken(jwtProperties);
    // when
    Long userIdByToken = this.tokenProvider.getUserId(token);
    // then
    assertThat(userIdByToken).isEqualTo(userId);
  }
}
