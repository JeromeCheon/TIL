package me.jeromecheon.spring4blogproject.controller;

import me.jeromecheon.spring4blogproject.domain.Article;
import me.jeromecheon.spring4blogproject.dto.AddArticleRequest;
import me.jeromecheon.spring4blogproject.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private BlogRepository blogRepository;

  @BeforeEach
  public void mockMvcSetup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    this.blogRepository.deleteAll();
  }

  @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
  @Test
  void addArticle() throws Exception {
    // given
    final String url = "/api/articles";
    final String title = "title";
    final String content = "content";
    final AddArticleRequest request = new AddArticleRequest(title, content);

    // when
    final ResultActions result = this.mockMvc.perform(
            post(url)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(this.objectMapper.writeValueAsString(request))
    );
    // then
    result
            .andExpect(status().isCreated());

    List<Article> articles = this.blogRepository.findAll();
    assertThat(articles.size()).isEqualTo(1);
    assertThat(articles.getFirst().getTitle()).isEqualTo(title);
    assertThat(articles.getFirst().getContent()).isEqualTo(content);
  }

  @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
  @Test
  void findAllArticles() throws Exception {
    // given
    final String url = "/api/articles";
    final String title = "title 1";
    final String content = "content 2";

    this.blogRepository.save(Article.builder().title(title).content(content).build());

    // when
    final ResultActions result = this.mockMvc.perform(
            get(url)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
    );
    // then
    result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value(title))
            .andExpect(jsonPath("$[0].content").value(content));

  }
}