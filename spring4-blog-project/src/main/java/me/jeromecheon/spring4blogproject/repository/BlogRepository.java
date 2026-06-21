package me.jeromecheon.spring4blogproject.repository;

import me.jeromecheon.spring4blogproject.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article, Long> {

}