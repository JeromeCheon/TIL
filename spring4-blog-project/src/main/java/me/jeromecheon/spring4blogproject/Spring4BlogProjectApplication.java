package me.jeromecheon.spring4blogproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Spring4BlogProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(Spring4BlogProjectApplication.class, args);
  }

}
