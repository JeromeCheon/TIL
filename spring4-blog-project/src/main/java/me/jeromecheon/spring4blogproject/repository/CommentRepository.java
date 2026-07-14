package me.jeromecheon.spring4blogproject.repository;

import me.jeromecheon.spring4blogproject.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
