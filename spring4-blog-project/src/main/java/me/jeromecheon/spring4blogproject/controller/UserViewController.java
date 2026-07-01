package me.jeromecheon.spring4blogproject.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.config.jwt.TokenProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.WebUtils;

@RequiredArgsConstructor
@Controller
public class UserViewController {
  private final TokenProvider tokenProvider;

  @GetMapping("/login")
  public String login(HttpServletRequest request) {
    Cookie refreshTokenCookie = WebUtils.getCookie(request, "refresh_token");
    if (refreshTokenCookie != null && tokenProvider.validToken(refreshTokenCookie.getValue())) {
      return "redirect:/articles";
    }
    return "oauthLogin";
  }

  @GetMapping("/signup")
  public String signup() {
    return "signup";
  }
}
