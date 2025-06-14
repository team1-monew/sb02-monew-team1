package com.team1.monew.article.integration;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.common.support.IntegrationTestSupport;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.ArticleViewActivity;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Testcontainers
@Transactional
public class ArticleViewIntegrationTest extends IntegrationTestSupport{

  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

  @DynamicPropertySource
  static void setMongoUri(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired private
  MongoTemplate mongoTemplate;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = userRepository.save(User.builder()
        .email("test@test.com")
        .password("Test1234!")
        .nickname("테스터")
        .build());

    article = articleRepository.save(Article.builder()
        .title("테스트 기사")
        .summary("요약")
        .source("NAVER")
        .sourceUrl("https://naver.com/test-article")
        .publishDate(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .viewCount(0L)
        .build());

    mongoTemplate.save(
        ArticleViewActivity.builder()
            .userId(user.getId())
            .articleViews(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build()
    );
  }

  @AfterEach
  void cleanUpMongoCollection() {
    mongoTemplate.dropCollection(SubscriptionActivity.class);
  }

  @Test
  @DisplayName("기사 조회 API 요청 성공 - 비동기 이벤트 발행으로 MongoDB에 document 업데이트")
  void recordView_shouldStoreDocumentInMongo() throws Exception {
    // given

    // when
    mockMvc.perform(post("/api/articles/{articleId}/article-views", article.getId())
            .header("Monew-Request-User-ID", user.getId()))
        .andExpect(status().isOk());

    // then
    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      ArticleViewActivity activity = mongoTemplate.findById(user.getId(), ArticleViewActivity.class);
      assertThat(activity).isNotNull();
      assertThat(activity.getArticleViews()).hasSize(1);
      assertThat(activity.getArticleViews().get(0).articleId()).isEqualTo(article.getId());
      assertThat(activity.getArticleViews().get(0).articleTitle()).isEqualTo(article.getTitle());
    });
  }
}

