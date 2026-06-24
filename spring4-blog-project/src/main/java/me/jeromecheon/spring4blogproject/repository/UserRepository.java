package me.jeromecheon.spring4blogproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.jeromecheon.spring4blogproject.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
