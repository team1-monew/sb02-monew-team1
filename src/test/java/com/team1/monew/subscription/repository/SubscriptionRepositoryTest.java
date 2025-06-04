package com.team1.monew.subscription.repository;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
public class SubscriptionRepositoryTest {
  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("관심사 ID와 유저 ID로 구독 조회 성공")
  void findByInterestIdAndUserId_success() {
    // given
    User user = new User("test@test.com","testUser", "1234");
    Interest interest = new Interest("테스트");
    entityManager.persist(user);
    entityManager.persist(interest);

    Subscription subscription = new Subscription(user, interest);
    entityManager.persist(subscription);
    entityManager.flush();
    entityManager.clear();

    // when
    Optional<Subscription> foundSubscription = subscriptionRepository.findByInterest_IdAndUser_Id(
        interest.getId(), user.getId());

    // then
    assertThat(foundSubscription).isPresent();
    assertThat(foundSubscription.get().getUser().getId()).isEqualTo(user.getId());
    assertThat(foundSubscription.get().getInterest().getId()).isEqualTo(interest.getId());
  }

  @Test
  @DisplayName("관심사 ID와 유저 ID로 구독 존재 여부 확인")
  void existsByInterestIdAndUserId_success() {
    // given
    User user = new User("test@test.com","testUser", "1234");
    Interest interest = new Interest("테스트");
    entityManager.persist(user);
    entityManager.persist(interest);

    Subscription subscription = new Subscription(user, interest);
    entityManager.persist(subscription);
    entityManager.flush();
    entityManager.clear();

    // when
    boolean exists = subscriptionRepository.existsByInterest_IdAndUser_Id(
        interest.getId(), user.getId());

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("구독 정보가 없을 경우, Optional 반환")
  void findByInterestIdAndUserId_notFound_return_optional() {
    // when
    Optional<Subscription> found = subscriptionRepository.findByInterest_IdAndUser_Id(1L, 1L);

    // then
    assertThat(found).isNotPresent();
  }
}
