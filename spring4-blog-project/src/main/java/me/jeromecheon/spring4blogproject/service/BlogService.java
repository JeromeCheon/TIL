package me.jeromecheon.spring4blogproject.service;


import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.Article;
import me.jeromecheon.spring4blogproject.dto.AddArticleRequest;
import me.jeromecheon.spring4blogproject.repository.BlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
  private final BlogRepository blogRepository;

  public Article save(AddArticleRequest request) {
    return this.blogRepository.save(request.toEntity());
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
}
