package me.jeromecheon.spring4blogproject.controller;

import me.jeromecheon.spring4blogproject.config.jwt.JwtFactory;
import me.jeromecheon.spring4blogproject.config.jwt.JwtProperties;
import me.jeromecheon.spring4blogproject.domain.RefreshToken;
import me.jeromecheon.spring4blogproject.domain.User;
import me.jeromecheon.spring4blogproject.dto.CreateAccessTokenRequest;
import me.jeromecheon.spring4blogproject.repository.RefreshTokenRepository;
import me.jeromecheon.spring4blogproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenApiControllerTest {
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected WebApplicationContext context;

  @Autowired
  JwtProperties jwtProperties;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  public void mockMvcSetup(){
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    this.userRepository.deleteAll();
  }

  @DisplayName("createNewAccessToken(): 새로운 엑세스 토큰을 발급한다.")
  @Test
  public void createNewAccessToken() throws Exception {
    // given
    final String url = "/api/token";

    User user = this.userRepository.save(User.builder()
            .email("test@test.com")
            .password("test")
            .build());

    String refreshToken = JwtFactory.builder()
            .claims(Map.of("id", user.getId()))
            .build()
            .createToken(jwtProperties);
    this.refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

    CreateAccessTokenRequest request = new CreateAccessTokenRequest();
    request.setRefreshToken(refreshToken);
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    final ResultActions result = this.mockMvc.perform(
            post(url).contentType(MediaType.APPLICATION_JSON).content(requestBody)
    );
    // then
    result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty());
  }
}
