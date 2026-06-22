package me.jeromecheon.spring4blogproject.dto;

import lombok.Getter;
import me.jeromecheon.spring4blogproject.domain.Article;

@Getter
public class ArticleListViewResponse {
  private final Long id;
  private final String title;
  private final String content;

  public ArticleListViewResponse(Article article) {
    this.id = article.getId();
    this.title = article.getTitle();
    this.content = article.getContent();
  }
}
