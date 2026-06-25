package me.jeromecheon.spring4blogproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
  private String email;
  private String password;
}
