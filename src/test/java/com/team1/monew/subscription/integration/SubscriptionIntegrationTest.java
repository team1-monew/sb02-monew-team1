package com.team1.monew.subscription.integration;

import com.team1.monew.common.support.IntegrationTestSupport;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Transactional
public class SubscriptionIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SubscriptionRepository subscriptionRepository;

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
