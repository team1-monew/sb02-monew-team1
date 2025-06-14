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
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.notification.repository.NotificationRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.CommentLikeActivity;
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
class CommentLikeIntegrationTest {

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
    CommentLikeRepository commentLikeRepository;

    @Autowired
    NotificationRepository notificationRepository;

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
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        notificationRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
        mongoTemplate.dropCollection(CommentLikeActivity.class);

        User user = userRepository.save(User.builder()
            .email("user@test.com")
            .nickname("테스터")
            .password("encrypted")
            .build());

        Article article = articleRepository.save(Article.builder()
            .source("출처")
            .sourceUrl("https://test.com")
            .title("테스트 기사")
            .summary("요약")
            .publishDate(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build());

        userId = user.getId();
        articleId = article.getId();

        mongoTemplate.save(CommentLikeActivity.builder()
            .userId(userId)
            .commentLikes(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .build());
    }

    @Test
    void 댓글을_좋아요하면_MongoDB에_활동_기록이_추가된다() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "좋아요 대상 댓글");
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

        // when
        mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                .header("Monew-Request-User-ID", userId))
            .andExpect(status().isOk());

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentLikeActivity activity = mongoTemplate.findById(userId, CommentLikeActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getCommentLikes()).hasSize(1);
            assertThat(activity.getCommentLikes().get(0).commentId()).isEqualTo(commentId);
        });
    }

    @Test
    void 댓글_좋아요를_취소하면_MongoDB에서_활동기록이_삭제된다() throws Exception {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "좋아요 취소 대상 댓글");
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

        mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                .header("Monew-Request-User-ID", userId))
            .andExpect(status().isOk());

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentLikeActivity activity = mongoTemplate.findById(userId, CommentLikeActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getCommentLikes()).hasSize(1);
        });

        // when
        mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
                .header("Monew-Request-User-ID", userId))
            .andExpect(status().isOk());

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            CommentLikeActivity activity = mongoTemplate.findById(userId, CommentLikeActivity.class);
            assertThat(activity).isNotNull();
            assertThat(activity.getCommentLikes()).isEmpty();
        });
    }

}
