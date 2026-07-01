package me.jeromecheon.spring4blogproject.controller;

import me.jeromecheon.spring4blogproject.domain.Article;
import me.jeromecheon.spring4blogproject.domain.User;
import me.jeromecheon.spring4blogproject.dto.AddArticleRequest;
import me.jeromecheon.spring4blogproject.dto.UpdateArticleRequest;
import me.jeromecheon.spring4blogproject.repository.BlogRepository;
import me.jeromecheon.spring4blogproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

  @Autowired
  UserRepository userRepository;

  User user;

  @BeforeEach
  public void mockMvcSetup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    this.blogRepository.deleteAll();
  }


  @BeforeEach
  void setSecurityContext() {
    userRepository.deleteAll();
    user = userRepository.save(User.builder()
            .email("user@gmail.com")
            .password("test")
            .build());
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
            new UsernamePasswordAuthenticationToken(
                    user,
                    user.getPassword(),
                    user.getAuthorities()
            )
    );
  }

  @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
  @Test
  void addArticle() throws Exception {
    // given
    final String url = "/api/articles";
    final String title = "title";
    final String content = "content";
    final AddArticleRequest request = new AddArticleRequest(title, content);

    Principal principal = Mockito.mock(Principal.class);
    Mockito.when(principal.getName()).thenReturn("username");
    // when
    final ResultActions result = this.mockMvc.perform(
            post(url)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .principal(principal)
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
    Article savedArticle = createDefaultArticle();

    // when
    final ResultActions result = this.mockMvc.perform(
            get(url)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
    );
    // then
    result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()))
            .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()));

  }

  @DisplayName("findArticleById: 블로그 글 하나 조회에 성공한다.")
  @Test
  public void findArticleById() throws Exception {
    // given
    final String url = "/api/articles/{id}";
    Article savedArticle = createDefaultArticle();

    // when
    final ResultActions result = this.mockMvc.perform(
            get(url, savedArticle.getId())
    );
    // then
    result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(savedArticle.getTitle()))
            .andExpect(jsonPath("$.content").value(savedArticle.getContent()));
  }

  @DisplayName("deleteArticleById: 블로그 글 삭제에 성공한다.")
  @Test
  public void deleteArticleById() throws Exception {
    // given
    final String url = "/api/articles/{id}";
    Article savedArticle = createDefaultArticle();

    // when
    mockMvc.perform(
            delete(url, savedArticle.getId())
    ).andExpect(status().isOk());
    // then
    List<Article> articles = this.blogRepository.findAll();
    assertThat(articles).isEmpty();
  }

  @DisplayName("updateArticleById: 블로그 글 수정에 성공한다.")
  @Test
  void updateArticleById() throws Exception {
    // given
    final String url = "/api/articles/{id}";
    Article savedArticle = createDefaultArticle();

    final String newTitle = "new title";
    final String newContent = "new content";

    UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

    // when
    ResultActions result = this.mockMvc.perform(
            put(url, savedArticle.getId())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(this.objectMapper.writeValueAsString(request))
    );
    // then
    result.andExpect(status().isOk());

    Article updatedArticle = this.blogRepository.findById(savedArticle.getId()).get();

    assertThat(updatedArticle.getTitle()).isEqualTo(newTitle);
    assertThat(updatedArticle.getContent()).isEqualTo(newContent);
  }

  private Article createDefaultArticle() {
    return this.blogRepository.save(
            Article
                    .builder()
                    .title("title")
                    .author(user.getUsername())
                    .content("content")
                    .build());
  }
}