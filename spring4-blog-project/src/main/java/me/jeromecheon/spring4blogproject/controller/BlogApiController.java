package me.jeromecheon.spring4blogproject.controller;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.Article;
import me.jeromecheon.spring4blogproject.dto.AddArticleRequest;
import me.jeromecheon.spring4blogproject.dto.ArticleResponse;
import me.jeromecheon.spring4blogproject.dto.UpdateArticleRequest;
import me.jeromecheon.spring4blogproject.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class BlogApiController {
  private final BlogService blogService;

  @PostMapping("/api/articles")
  public ResponseEntity<Article> addArticle(@RequestBody @Validated AddArticleRequest request, Principal principal) {
    Article savedArticle = this.blogService.save(request, principal.getName());

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedArticle);
  }

  @GetMapping("/api/articles")
  public ResponseEntity<List<ArticleResponse>> findAllArticles() {
    List<ArticleResponse> articles = this.blogService.findAll()
            .stream()
            .map(ArticleResponse::new)
            .toList();
    return ResponseEntity.ok().body(articles);
  }

  @GetMapping("/api/articles/{id}")
  public ResponseEntity<ArticleResponse> findArticleById(@PathVariable Long id) {
    Article article = this.blogService.findById(id);

    return ResponseEntity.ok()
            .body(new ArticleResponse(article));
  }

  @DeleteMapping("/api/articles/{id}")
  public ResponseEntity<Void> deleteArticleById(@PathVariable Long id) {
    this.blogService.delete(id);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/api/articles/{id}")
  public ResponseEntity<Article> updateArticleById(
          @PathVariable long id,
          @RequestBody UpdateArticleRequest request) {
    Article updatedArticle = this.blogService.update(id, request);

    return ResponseEntity.ok().body(updatedArticle);
  }
}
