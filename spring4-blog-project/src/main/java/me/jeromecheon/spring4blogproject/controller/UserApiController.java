package me.jeromecheon.spring4blogproject.controller;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.dto.AddUserRequest;
import me.jeromecheon.spring4blogproject.service.UserService;
import org.springframework.stereotype.Controller;
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
}
