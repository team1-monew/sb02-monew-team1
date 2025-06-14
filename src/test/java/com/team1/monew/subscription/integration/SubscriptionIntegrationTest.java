package com.team1.monew.subscription.integration;

import com.team1.monew.common.support.IntegrationTestSupport;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers;
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
public class SubscriptionIntegrationTest extends IntegrationTestSupport {

  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

  @DynamicPropertySource
  static void setMongoUri(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  private Long userId;
  private Interest savedInterest;
  private User savedUser;

  @BeforeEach
  void setUp() {
    savedUser = userRepository.save(User.builder()
        .email("test@test.com")
        .nickname("test")
        .password("Test1234!")
        .build());
    userId = savedUser.getId();

    savedInterest = interestRepository.save(new Interest("여행"));
    savedInterest.addKeyword(new Keyword("해외"));
    savedInterest.addKeyword(new Keyword("국내"));

    mongoTemplate.save(SubscriptionActivity.builder()
        .userId(userId)
        .subscriptions(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .build()
    );
  }

  @AfterEach
  void cleanUpMongoCollection() {
    mongoTemplate.dropCollection(SubscriptionActivity.class);
  }

  @Test
  @DisplayName("관심사 구독 API 요청 성공")
  void createSubscription_success() throws Exception {
    // given

    // when + then
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.interestId").value(savedInterest.getId()))
        .andExpect(jsonPath("$.interestName").value(savedInterest.getName()))
        .andExpect(jsonPath("$.interestKeywords").value(Matchers.contains("해외", "국내")))
        .andExpect(jsonPath("$.interestSubscriberCount").value(1))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("관심사 구독 API 요청 성공 - 비동기 이벤트 발행으로 MongoDB에 document 업데이트")
  void createSubscription_eventPublish_async_mongoDB_save_document() throws Exception {
    // given

    // when
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isCreated());

    // then
    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      SubscriptionActivity activity = mongoTemplate.findById(userId, SubscriptionActivity.class);
      assertThat(activity).isNotNull();
      assertThat(activity.getSubscriptions()).hasSize(1);
      assertThat(activity.getSubscriptions().get(0).interestName()).isEqualTo("여행");
    });
    ;
  }

  @Test
  @DisplayName("관심사 구독 API 요청 - 중복 구독 실패")
  void createSubscription_duplicateSubscription_failed() throws Exception {
    // given
    subscriptionRepository.save(new Subscription(savedUser, savedInterest));

    // when + then
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("이미 존재하는 리소스입니다."))
        .andExpect(jsonPath("$.code").value("CONFLICT"));
  }

  @Test
  @DisplayName("관심사 구독 취소 API 요청 성공")
  void deleteSubscription_success() throws Exception {
    // given
    subscriptionRepository.save(new Subscription(savedUser, savedInterest));

    // when + then
    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("관심사 구독 취소 API 요청 성공 - 비동기 이벤트 발행으로 MongoDB에 document 업데이트")
  void deleteSubscription_eventPublish_async_mongoDB_save_document() throws Exception {
    // given
    Subscription savedSubscription = subscriptionRepository.save(
        new Subscription(savedUser, savedInterest));

    SubscriptionActivity subscriptionActivity = mongoTemplate.findById(userId,
        SubscriptionActivity.class);
    subscriptionActivity.getSubscriptions().add(SubscriptionDto.builder()
        .id(savedSubscription.getId())
        .interestId(savedInterest.getId())
        .interestName(savedInterest.getName())
        .build());
    mongoTemplate.save(subscriptionActivity);

    // when
    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNoContent());

    // then
    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      SubscriptionActivity activity = mongoTemplate.findById(userId, SubscriptionActivity.class);
      assertThat(activity).isNotNull();
      assertThat(activity.getSubscriptions()).hasSize(0);
    });
  }

  @Test
  @DisplayName("관심사 구독 취소 API 요청 - 구독내역이 없을 때 실패")
  void deleteSubscription_notFoundSubscription_failed() throws Exception {
    // given

    // when + then
    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", savedInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }
}
