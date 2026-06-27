package me.jeromecheon.spring4blogproject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccessTokenRequest {
  private String refreshToken;
}
