package me.jeromecheon.spring4blogproject.controller;

import lombok.RequiredArgsConstructor;
import me.jeromecheon.spring4blogproject.dto.ArticleListViewResponse;
import me.jeromecheon.spring4blogproject.service.BlogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {

  private final BlogService blogService;

  @GetMapping("/articles")
  public String getArticles(Model model) {
    List<ArticleListViewResponse> articles = this.blogService.findAll().stream()
            .map(ArticleListViewResponse::new)
            .toList();
    model.addAttribute("articles", articles);
    return "articleList";
  }
}
