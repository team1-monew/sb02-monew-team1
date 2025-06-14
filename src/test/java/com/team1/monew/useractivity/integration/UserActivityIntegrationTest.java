package com.team1.monew.useractivity.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.ArticleViewActivity;
import com.team1.monew.useractivity.document.CommentActivity;
import com.team1.monew.useractivity.document.CommentLikeActivity;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import com.team1.monew.useractivity.repository.ArticleViewActivityRepository;
import com.team1.monew.useractivity.repository.CommentActivityRepository;
import com.team1.monew.useractivity.repository.CommentLikeActivityRepository;
import com.team1.monew.useractivity.repository.SubscriptionActivityRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
class UserActivityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentActivityRepository commentActivityRepository;

    @Autowired
    CommentLikeActivityRepository commentLikeActivityRepository;

    @Autowired
    SubscriptionActivityRepository subscriptionActivityRepository;

    @Autowired
    ArticleViewActivityRepository articleViewActivityRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setMongoUri(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    @DisplayName("GET /api/user-activities/{userId} - 성공")
    void findUserActivity_success() throws Exception {
        // given
        Long userId = 1L;

        User user = User.builder()
            .email("test@example.com")
            .nickname("tester")
            .password("pw")
            .build();
        userRepository.save(user);

        commentActivityRepository.save(CommentActivity.builder()
            .userId(userId)
            .comments(List.of(CommentActivityDto.builder()
                .id(1L)
                .userId(userId)
                .content("댓글")
                .articleId(10L)
                .articleTitle("기사 제목")
                .likeCount(5L)
                .createdAt(LocalDateTime.now())
                .build()))
            .build());

        commentLikeActivityRepository.save(CommentLikeActivity.builder()
            .userId(userId)
            .commentLikes(List.of(CommentLikeActivityDto.builder()
                .id(2L)
                .commentId(1L)
                .articleId(10L)
                .articleTitle("기사 제목")
                .commentUserId(99L)
                .commentUserNickname("작성자")
                .commentContent("댓글")
                .commentLikeCount(5L)
                .commentCreatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build()))
            .build());

        subscriptionActivityRepository.save(SubscriptionActivity.builder()
            .userId(userId)
            .subscriptions(List.of(SubscriptionDto.builder()
                .id(1L)
                .interestId(123L)
                .interestName("관심 주제")
                .interestKeywords(List.of("뉴스", "정치"))
                .interestSubscriberCount(100L)
                .createdAt(LocalDateTime.now())
                .build()))
            .build());

        articleViewActivityRepository.save(ArticleViewActivity.builder()
            .userId(userId)
            .articleViews(List.of(ArticleViewDto.builder()
                .articleId(300L)
                .articleTitle("본 기사")
                .createdAt(LocalDateTime.now())
                .build()))
            .build());

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", userId)
                .header("Monew-Request-User-ID", userId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("tester"))
            .andExpect(jsonPath("$.comments[0].content").value("댓글"))
            .andExpect(jsonPath("$.commentLikes[0].commentContent").value("댓글"))
            .andExpect(jsonPath("$.subscriptions[0].interestName").value("관심 주제"))
            .andExpect(jsonPath("$.articleViews[0].articleTitle").value("본 기사"));
    }
}
