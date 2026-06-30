package me.jeromecheon.spring4blogproject.service;


import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.Article;
import me.jeromecheon.spring4blogproject.dto.AddArticleRequest;
import me.jeromecheon.spring4blogproject.dto.UpdateArticleRequest;
import me.jeromecheon.spring4blogproject.repository.BlogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
  private final BlogRepository blogRepository;

  public Article save(AddArticleRequest request, String userName) {
    return this.blogRepository.save(request.toEntity(userName));
  }

  public List<Article> findAll() {
    return this.blogRepository.findAll();
  }

  public Article findById(Long id) {
    return this.blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found: " + id));
  }

  public void delete(Long id) {
    this.blogRepository.deleteById(id);
  }

  @Transactional
  public Article update(long id, UpdateArticleRequest request) {
    Article article = this.blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    article.update(request.getTitle(), request.getContent());

    return article;
  }
}
