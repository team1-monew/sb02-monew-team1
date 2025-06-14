package com.team1.monew.subscription.repository;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

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
    User user = new User("test@test.com", "testUser", "1234");
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
    User user = new User("test@test.com", "testUser", "1234");
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

  @Test
  @DisplayName("유저가 구독한 관심사의 ID 리스트 조회")
  void findSubscribedInterestId_byUserId_success() {
    // given
    User user = new User("test@test.com", "testUser", "1234");
    Interest interest1 = new Interest("테스트1");
    Interest interest2 = new Interest("테스트2");
    entityManager.persist(user);
    entityManager.persist(interest1);
    entityManager.persist(interest2);

    Subscription subscription1 = new Subscription(user, interest1);
    Subscription subscription2 = new Subscription(user, interest2);
    entityManager.persist(subscription1);
    entityManager.persist(subscription2);
    entityManager.flush();
    entityManager.clear();

    // when
    List<Long> interestIdList = subscriptionRepository.findSubscribedInterestIdByUserId(
        user.getId());

    // then
    assertThat(interestIdList).hasSize(2);
    assertThat(interestIdList).containsExactlyInAnyOrderElementsOf(
        List.of(interest1.getId(), interest2.getId()));
  }

  @Test
  @DisplayName("유저가 구독한 관심사 전체 리스트 fetch join 조회")
  void findSubscriptions_byUserId_fetchJoin_success() {
    // given
    PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    User user = new User("test@test.com", "testUser", "1234");
    Interest interest1 = new Interest("테스트1");
    Interest interest2 = new Interest("테스트2");
    entityManager.persist(user);
    entityManager.persist(interest1);
    entityManager.persist(interest2);

    Subscription subscription1 = new Subscription(user, interest1);
    Subscription subscription2 = new Subscription(user, interest2);
    entityManager.persist(subscription1);
    entityManager.persist(subscription2);
    entityManager.flush();
    entityManager.clear();

    // when
    List<Subscription> subscriptionList = subscriptionRepository.findByUserIdFetch(user.getId());

    // then
    subscriptionList.forEach(
        subscription -> assertThat(util.isLoaded(subscription, "interest")).isTrue());

    List<String> subscribedInterestNameList = subscriptionList.stream()
        .map(subscription -> subscription.getInterest().getName()).toList();
    assertThat(subscriptionList).hasSize(2);
    assertThat(subscribedInterestNameList).containsExactlyInAnyOrderElementsOf(
        List.of(interest1.getName(), interest2.getName()));
  }

  @Test
  @DisplayName("특정 interest를 구독한 subscription 리스트 fetch join 조회")
  void findSubscriptions_byInterest_fetchJoin_success() {
    // given
    PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    User user = new User("test@test.com", "testUser", "1234");
    Interest interest1 = new Interest("테스트1");
    Interest interest2 = new Interest("테스트2");
    entityManager.persist(user);
    entityManager.persist(interest1);
    entityManager.persist(interest2);

    Subscription subscription1 = new Subscription(user, interest1);
    Subscription subscription2 = new Subscription(user, interest2);
    entityManager.persist(subscription1);
    entityManager.persist(subscription2);
    entityManager.flush();
    entityManager.clear();

    // when
    List<Subscription> subscriptionList = subscriptionRepository.findAllByInterestWithUser(
        interest1);

    Subscription subscription = subscriptionList.get(0);
    // then
    assertThat(subscriptionList).hasSize(1);
    assertThat(util.isLoaded(subscription, "user")).isTrue();
  }

  @Test
  @DisplayName("특정 유저가 구독한 subscription 리스트를 createdAt 내림차순으로 fetch join 조회")
  void findSubscriptions_byUserId_fetchJoin_OrderBy_createdAt_DESC_success() {
    // given
    PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    User user = new User("test@test.com", "testUser", "1234");
    Interest interest1 = new Interest("테스트1");
    Interest interest2 = new Interest("테스트2");
    Interest interest3 = new Interest("테스트3");
    entityManager.persist(user);
    entityManager.persist(interest1);
    entityManager.persist(interest2);
    entityManager.persist(interest3);

    Subscription subscription1 = new Subscription(user, interest1);
    Subscription subscription2 = new Subscription(user, interest2);
    Subscription subscription3 = new Subscription(user, interest3);
    ReflectionTestUtils.setField(subscription1, "createdAt", LocalDateTime.of(2025, 6, 11, 15, 30, 0));
    ReflectionTestUtils.setField(subscription2, "createdAt", LocalDateTime.of(2025, 5, 11, 15, 30, 0));
    ReflectionTestUtils.setField(subscription3, "createdAt", LocalDateTime.of(2025, 4, 11, 15, 30, 0));
    entityManager.persist(subscription1);
    entityManager.persist(subscription2);
    entityManager.persist(subscription3);
    entityManager.flush();
    entityManager.clear();

    // when
    List<Subscription> subscriptionList = subscriptionRepository.findByUserIdOrderByCreatedAt(user.getId());

    // then
    subscriptionList.forEach(
        subscription -> assertThat(util.isLoaded(subscription, "interest")).isTrue());

    assertThat(subscriptionList.get(0).getCreatedAt()).isEqualTo(subscription1.getCreatedAt());
    assertThat(subscriptionList.get(1).getCreatedAt()).isEqualTo(subscription2.getCreatedAt());
    assertThat(subscriptionList.get(2).getCreatedAt()).isEqualTo(subscription3.getCreatedAt());
  }


}
