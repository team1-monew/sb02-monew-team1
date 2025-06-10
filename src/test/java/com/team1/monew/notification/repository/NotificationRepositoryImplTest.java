package com.team1.monew.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.user.entity.User;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DataJpaTest
class NotificationRepositoryImplTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private TestEntityManager em;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void getAllByCursorRequest() {
    // given
    User user = User.builder()
        .email("user1@email.com")
        .nickname("user1")
        .password("user1@@@")
        .build();
    em.persist(user);

    Notification n1 = Notification.builder()
        .user(user)
        .content("알림1")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(11L)
        .build();
    em.persist(n1);

    Notification n2 = Notification.builder()
        .user(user)
        .content("알림2")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(12L)
        .build();
    em.persist(n2);

    em.flush();
    em.clear();

    jdbcTemplate.update(
        "UPDATE notifications SET created_at = ? WHERE id = ?",
        Timestamp.valueOf(LocalDateTime.now().plusMinutes(1)),
        n2.getId()
    );

    LocalDateTime now = LocalDateTime.now();

    NotificationCursorRequest request = NotificationCursorRequest.builder()
        .direction("ASC")
        .cursor(now.toString())
        .after(now)
        .limit(50)
        .userId(user.getId())
        .build();

    // when
    Slice<NotificationDto> slice = notificationRepository.getAllByCursorRequest(request);

    // then
    assertThat(slice.getContent()).hasSize(1);
    assertThat(slice.hasNext()).isFalse(); // 더 있는지 여부
  }
}