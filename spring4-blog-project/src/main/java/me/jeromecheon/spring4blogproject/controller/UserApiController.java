package me.jeromecheon.spring4blogproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.dto.AddUserRequest;
import me.jeromecheon.spring4blogproject.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserApiController {
  private final UserService userService;

  @PostMapping("/user")
  public String signup(AddUserRequest request) {
    this.userService.save(request);
    return "redirect:/login";
  }

  @GetMapping()
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    new SecurityContextLogoutHandler().logout(request, response,
            SecurityContextHolder.getContext().getAuthentication());
    return "redirect:/login";
  }
}
