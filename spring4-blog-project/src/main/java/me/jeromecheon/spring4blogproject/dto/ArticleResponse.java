package me.jeromecheon.spring4blogproject.dto;

import lombok.Getter;
import me.jeromecheon.spring4blogproject.domain.Article;

@Getter
public class ArticleResponse {
  private final String title;
  private final String content;

  public ArticleResponse(Article article) {
    this.title = article.getTitle();
    this.content = article.getContent();
  }
}
