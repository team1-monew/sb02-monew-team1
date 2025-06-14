package com.team1.monew.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.CommentActivity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CommentIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setMongoUri(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    Long userId;
    Long articleId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        userRepository.deleteAll();
        articleRepository.deleteAll();
        mongoTemplate.dropCollection(CommentActivity.class);

        User user = userRepository.save(
            User.builder()
                .email("user@test.com")
                .nickname("테스터")
                .password("encrypted")
                .build()
        );

        Article article = articleRepository.save(
            Article.builder()
                .source("테스트출처")
                .sourceUrl("https://test.com")
                .title("테스트 기사")
                .publishDate(LocalDateTime.now())
                .summary("요약")
                .createdAt(LocalDateTime.now())
                .build()
        );

        userId = user.getId();
        articleId = article.getId();

        mongoTemplate.save(CommentActivity.builder()
            .userId(userId)
            .comments(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .build()
        );
    }

    @Test
    void 댓글을_등록하면_MongoDB에_활동_기록이_추가된다() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "댓글 내용");

        // when
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Monew-Request-User-ID", userId)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentActivity activity = mongoTemplate.findById(userId, CommentActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getComments()).hasSize(1);
            assertThat(activity.getComments().get(0).content()).isEqualTo("댓글 내용");
        });
    }

    @Test
    void 댓글을_삭제하면_MongoDB_활동기록에서_해당_댓글이_제거된다() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "삭제 대상 댓글");
        String content = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Monew-Request-User-ID", userId)
                .content(content))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long commentId = objectMapper.readTree(response).get("id").asLong();

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentActivity activity = mongoTemplate.findById(userId, CommentActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getComments()).hasSize(1);
            assertThat(activity.getComments().get(0).id()).isEqualTo(commentId);
        });

        // when
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .header("Monew-Request-User-ID", userId))
            .andExpect(status().isNoContent());

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentActivity activity = mongoTemplate.findById(userId, CommentActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getComments())
                .noneMatch(c -> c.id().equals(commentId));
        });
    }

}
