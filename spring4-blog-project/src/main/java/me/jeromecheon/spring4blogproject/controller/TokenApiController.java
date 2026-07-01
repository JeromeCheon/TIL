package me.jeromecheon.spring4blogproject.controller;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.dto.CreateAccessTokenRequest;
import me.jeromecheon.spring4blogproject.dto.CreateAccessTokenResponse;
import me.jeromecheon.spring4blogproject.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
  private final TokenService tokenService;

  @PostMapping("/api/token")
  public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
          @RequestBody CreateAccessTokenRequest request) {
    String newAccessToken = this.tokenService.createNewAccessToken(request.getRefreshToken());
    return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
  }

  @DeleteMapping("/api/refresh-token")
  public ResponseEntity<Void> removeRefreshToken(Principal principal) {
    String email = principal.getName();
    this.tokenService.deleteByRefreshToken(email);
    return ResponseEntity.ok().build();
  }
}
