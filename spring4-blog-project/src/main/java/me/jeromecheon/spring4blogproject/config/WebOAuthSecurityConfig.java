package me.jeromecheon.spring4blogproject.config;


import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.config.jwt.TokenProvider;
import me.jeromecheon.spring4blogproject.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import me.jeromecheon.spring4blogproject.config.oauth.OAuth2UserCustomService;
import me.jeromecheon.spring4blogproject.repository.RefreshTokenRepository;
import me.jeromecheon.spring4blogproject.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {
  private final OAuth2UserCustomService oAuth2UserCustomService;
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserService userService;

  @Bean
  public WebSecurityCustomizer configure() {
    return (web) -> web.ignoring()
            .requestMatchers(toH2Console())
            .requestMatchers(
                    "/img/**",
                    "/css/**",
                    "/js/**"
            );
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(management -> management.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .addFilterBefore(tokenAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/token").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll())
            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .authorizationEndpoint(authorizationEndpoint ->
                            authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                    .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService))
                    .successHandler(oAuth2SuccessHandler())
            )
            .exceptionHandling(ex -> ex
                    .defaultAuthenticationEntryPointFor(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                            request -> request.getRequestURI().startsWith("/api/")
                    ))
            .build();
  }

  @Bean
  public OAuth2SuccessHandler oAuth2SuccessHandler() {
    return new OAuth2SuccessHandler(
            this.tokenProvider,
            this.refreshTokenRepository,
            this.oAuth2AuthorizationRequestBasedOnCookieRepository(),
            this.userService
    );
  }

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() throws Exception {
    return new TokenAuthenticationFilter(this.tokenProvider);
  }

  @Bean
  public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
    return new OAuth2AuthorizationRequestBasedOnCookieRepository();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
