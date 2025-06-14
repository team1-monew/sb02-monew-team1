package com.team1.monew.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class) // 필요 시 QueryDSL 설정 포함
class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("markAllAsConfirmedByUserId() 성공")
  void markAllAsConfirmedByUserId_shouldUpdateConfirmedFieldToTrue() {
    // given
    User user = User.builder()
        .email("test@example.com")
        .nickname("testuser")
        .password("testpassword")
        .build();
    entityManager.persist(user);


    Notification notification1 = Notification.builder()
        .user(user)
        .content("알림1")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(11L)
        .build();
    entityManager.persist(notification1);

    Notification notification2 = Notification.builder()
        .user(user)
        .content("알림2")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(12L)
        .build();
    entityManager.persist(notification2);

    entityManager.flush();
    entityManager.clear(); // 1차 캐시 제거

    // when
    notificationRepository.markAllAsConfirmedByUserId(user.getId());
    entityManager.flush();
    entityManager.clear(); // 다시 쿼리 실행하게끔 캐시 비움

    // then
    List<Notification> all = notificationRepository.findAll();
    assertThat(all).hasSize(2);
    assertThat(all).allMatch(Notification::isConfirmed);
  }

  @Test
  @DisplayName("markAsConfirmedByNotificationId() 성공")
  void markAsConfirmedByNotificationId_shouldUpdateConfirmedFieldToTrue() {
    // given
    User user = User.builder()
        .email("test@example.com")
        .nickname("testuser")
        .password("testpassword")
        .build();
    entityManager.persist(user);


    Notification notification1 = Notification.builder()
        .user(user)
        .content("알림1")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(11L)
        .build();
    entityManager.persist(notification1);

    Notification notification2 = Notification.builder()
        .user(user)
        .content("알림2")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(12L)
        .build();
    entityManager.persist(notification2);

    entityManager.flush();
    entityManager.clear(); // 1차 캐시 제거

    // when
    notificationRepository.markAsConfirmedByNotificationId(notification1.getId());
    entityManager.flush();
    entityManager.clear(); // 다시 쿼리 실행하게끔 캐시 비움

    // then
    Notification updated = notificationRepository.findById(notification1.getId())
        .orElseThrow();
    Notification untouched = notificationRepository.findById(notification2.getId())
        .orElseThrow();

    assertThat(updated.isConfirmed()).isTrue();
    assertThat(untouched.isConfirmed()).isFalse();
  }

  @Test
  @DisplayName("confirmed=true이고 updatedAt이 cutoff 이전인 알림만 삭제되어야 한다")
  void deleteConfirmedBefore_shouldDeleteOnlyMatching() {
    // given: 다양한 상태의 알림을 저장
    User user = User.builder()
        .email("test@example.com")
        .nickname("testuser")
        .password("testpassword")
        .build();
    entityManager.persist(user);

    Notification keep1 = Notification.builder()
        .user(user)
        .content("알림1")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(11L)
        .build();
    entityManager.persist(keep1);
    Notification keep2 = Notification.builder()
        .user(user)
        .content("알림2")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(12L)
        .build();
    entityManager.persist(keep2);
    Notification del1 = Notification.builder()
        .user(user)
        .content("알림3")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(13L)
        .build();
    entityManager.persist(del1);

    entityManager.flush();
    entityManager.clear(); // 1차 캐시 제거

    notificationRepository.markAsConfirmedByNotificationId(del1.getId());

    // when: cutoff를 당일로 설정하고 삭제 호출
    int deletedCount = notificationRepository.deleteConfirmedBefore(LocalDateTime.now());

    // then: 딱 한 건만 삭제되고, 나머지는 그대로 남아 있어야 함
    assertThat(deletedCount).isEqualTo(1);
    List<Notification> remaining = notificationRepository.findAll();
    assertThat(remaining.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("countByUserIdAndConfirmedFalse()는 confirmed=false인 알림만 정확히 센다")
  void countByUserIdAndConfirmedFalse_shouldCountOnlyUnconfirmed() {
    // given
    User user = User.builder()
        .email("test@example.com")
        .nickname("testuser")
        .password("testpassword")
        .build();
    entityManager.persist(user);

    // confirmed = false (기본값)
    Notification unconfirmed1 = Notification.builder()
        .user(user)
        .content("unconfirmed 1")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(1L)
        .build();
    entityManager.persist(unconfirmed1);

    Notification unconfirmed2 = Notification.builder()
        .user(user)
        .content("unconfirmed 2")
        .resourceType(ResourceType.INTEREST.getName())
        .resourceId(2L)
        .build();
    entityManager.persist(unconfirmed2);

    // confirmed = true
    Notification confirmed = Notification.builder()
        .user(user)
        .content("confirmed")
        .resourceType(ResourceType.INTEREST.getName())
        .resourceId(3L)
        .build();
    entityManager.persist(confirmed);

    entityManager.flush();
    entityManager.clear();

    notificationRepository.markAsConfirmedByNotificationId(confirmed.getId());
    entityManager.flush();
    entityManager.clear();

    // when
    long count = notificationRepository.countByUserIdAndConfirmedFalse(user.getId());

    // then
    assertThat(count).isEqualTo(2);
  }

}