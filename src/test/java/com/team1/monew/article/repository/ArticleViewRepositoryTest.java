package com.team1.monew.article.repository;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class ArticleViewRepositoryTest {

  @Autowired
  private ArticleViewRepository articleViewRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("유저가 조회한 삭제되지 않은 기사 뷰 리스트를 createdAt 내림차순 + fetch join으로 조회")
  void findValidArticleViewsByUserIdOrderByCreatedAt_success() {
    // given
    PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    User user = new User("test@test.com", "testUser", "1234");
    entityManager.persist(user);

    Article article1 = Article.builder()
        .title("기사1").source("source").sourceUrl("url1")
        .summary("요약1").publishDate(LocalDateTime.now()).createdAt(LocalDateTime.of(2025, 6, 11, 17, 30, 0))
        .isDeleted(false).build();

    Article article2 = Article.builder()
        .title("기사2").source("source").sourceUrl("url2")
        .summary("요약2").publishDate(LocalDateTime.now()).createdAt(LocalDateTime.of(2025, 6, 11, 15, 30, 0))
        .isDeleted(true).build(); // 삭제된 기사

    Article article3 = Article.builder()
        .title("기사3").source("source").sourceUrl("url3")
        .summary("요약3").publishDate(LocalDateTime.now()).createdAt(LocalDateTime.of(2025, 6, 11, 13, 30, 0))
        .isDeleted(false).build();

    entityManager.persist(article1);
    entityManager.persist(article2);
    entityManager.persist(article3);

    ArticleView view1 = new ArticleView(article1, user);
    ArticleView view2 = new ArticleView(article2, user); // 삭제된 기사
    ArticleView view3 = new ArticleView(article3, user);;

    entityManager.persist(view1);
    entityManager.persist(view2);
    entityManager.persist(view3);

    entityManager.flush();
    entityManager.clear();

    // when
    List<ArticleView> result = articleViewRepository.findValidArticleViewsByUserIdOrderByCreatedAt(user.getId());

    // then
    result.forEach(view ->
        assertThat(util.isLoaded(view, "article")).isTrue()
    );

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getArticle().getTitle()).isEqualTo("기사1");
    assertThat(result.get(1).getArticle().getTitle()).isEqualTo("기사3");
  }
}
