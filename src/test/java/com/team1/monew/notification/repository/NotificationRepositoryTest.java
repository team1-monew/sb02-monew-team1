package com.team1.monew.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

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
}