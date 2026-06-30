package me.jeromecheon.spring4blogproject.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.jeromecheon.spring4blogproject.domain.Article;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddArticleRequest {
  private String title;
  private String content;

  public Article toEntity(String author) {
    return Article.builder()
            .title(this.title)
            .content(this.content)
            .author(author)
            .build();
  }
}
