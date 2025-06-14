package com.team1.monew.article.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.entity.Article;
import com.team1.monew.article.mapper.ArticleMapper;
import com.team1.monew.article.repository.ArticleInterestRepository;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.article.repository.ArticleViewRepository;
import com.team1.monew.article.service.ArticleService;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.common.S3Util;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private ArticleInterestRepository articleInterestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private S3Util s3Util;

    @Autowired
    private ObjectMapper objectMapper;

    private Interest interest;
    private Keyword keyword;
    private User testUser;

    private List<Article> savedArticles;

    @BeforeAll
    void setup() {
        commentRepository.deleteAll();
        articleViewRepository.deleteAll();
        articleInterestRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
        interestRepository.deleteAll();

        interest = new Interest("IT");
        keyword = new Keyword("AI");
        interest.addKeyword(keyword);
        interest = interestRepository.save(interest);

        testUser = userRepository.save(
                User.builder()
                        .nickname("test")
                        .email("test@test.com")
                        .password("password123$")
                        .build()
        );
    }

    @Test
    @DisplayName("기사 수집 및 저장 통합 테스트")
    void testCollectAndSaveArticles() {
        articleService.collectAndSaveNaverArticles(interest, keyword);
        articleService.collectAndSaveChosunArticles(interest, keyword);

        savedArticles = articleRepository.findAll();
        assertFalse(savedArticles.isEmpty());
    }

    @Test
    @DisplayName("기사 백업 통합 테스트")
    void testBackupUpload() throws Exception {
        if (savedArticles == null || savedArticles.isEmpty()) {
            testCollectAndSaveArticles();
        }

        List<ArticleDto> articleDtos = savedArticles.stream()
                .map(article -> ArticleMapper.toDto(article, 0L, false))
                .toList();

        String json = objectMapper.writeValueAsString(Map.of("items", articleDtos));
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        String backupKey = "backup/articles/backup-articles-" + LocalDate.now() + ".json";

        try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
            s3Util.upload(backupKey, inputStream, jsonBytes.length, "application/json");
        }
    }

    @Test
    @DisplayName("기사 조회 통합 테스트")
    void testRecordView() {
        if (savedArticles == null || savedArticles.isEmpty()) {
            testCollectAndSaveArticles();
        }
        Article firstArticle = savedArticles.get(0);

        ArticleViewDto viewDto = articleService.recordView(firstArticle.getId(), testUser.getId());
        assertEquals(firstArticle.getId(), viewDto.articleId());
    }

    @Test
    @DisplayName("기사 논리 삭제 통합 테스트")
    void testLogicalDelete() {
        if (savedArticles == null || savedArticles.isEmpty()) {
            testCollectAndSaveArticles();
        }
        Article firstArticle = savedArticles.get(0);

        articleService.deleteArticle(firstArticle.getId());
        Article deletedArticle = articleRepository.findById(firstArticle.getId()).orElseThrow();
        assertTrue(deletedArticle.isDeleted());
    }

    @Test
    @DisplayName("기사 물리 삭제 통합 테스트")
    void testHardDelete() {
        if (savedArticles == null || savedArticles.isEmpty()) {
            testCollectAndSaveArticles();
        }
        Article firstArticle = savedArticles.get(0);

        articleService.hardDeleteArticle(firstArticle.getId());
        boolean exists = articleRepository.existsById(firstArticle.getId());
        assertFalse(exists);
    }

    @Test
    @DisplayName("기사 복구 통합 테스트")
    void testRestoreFromBackup() throws Exception {
        String backupKey = "backup/articles/backup-articles-" + LocalDate.now() + ".json";
        byte[] backupData = s3Util.download(backupKey);
        assertTrue(backupData.length > 0);

        String jsonString = new String(backupData, StandardCharsets.UTF_8);
        Map<String, Object> jsonData = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        List<ArticleDto> restoredArticles = objectMapper.convertValue(jsonData.get("items"), new TypeReference<List<ArticleDto>>() {
        });

        restoredArticles.forEach(dto -> {
            Article restored = ArticleMapper.toRestoredEntity(dto);
            articleRepository.save(restored);
        });
    }
}



