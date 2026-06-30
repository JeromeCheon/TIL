package me.jeromecheon.spring4blogproject.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.config.jwt.TokenProvider;
import me.jeromecheon.spring4blogproject.domain.RefreshToken;
import me.jeromecheon.spring4blogproject.domain.User;
import me.jeromecheon.spring4blogproject.repository.RefreshTokenRepository;
import me.jeromecheon.spring4blogproject.service.UserService;
import me.jeromecheon.spring4blogproject.util.CookieUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.sql.Ref;
import java.time.Duration;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
  public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
  public static final String REDIRECT_PATH = "/articles";

  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
  private final UserService userService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    User user = this.userService.findByEmail((String) oAuth2User.getAttributes().get("email"));

    String refreshToken = this.tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
    saveRefreshToken(user.getId(), refreshToken);
    addRefreshTokenToCookie(request, response, refreshToken);

    String accessToken = this.tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
    String targetUrl = this.getTargetUrl(accessToken);

    clearAuthenticationAttributes(request, response);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private void saveRefreshToken(Long userId, String newRefreshToken) {
    RefreshToken refreshToken = this.refreshTokenRepository.findByUserId(userId)
            .map(entity -> entity.update(newRefreshToken))
            .orElse(new RefreshToken(userId, newRefreshToken));
    this.refreshTokenRepository.save(refreshToken);
  }

  private void addRefreshTokenToCookie(
          HttpServletRequest request, HttpServletResponse response, String refreshToken) {
    int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
    CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
    CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
  }

  private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    this.authorizationRequestRepository.removeAuthorizationRequestCookie(request, response);
  }

  private String getTargetUrl(String token) {
    return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
            .queryParam("token", token)
            .build()
            .toUriString();
  }

}
