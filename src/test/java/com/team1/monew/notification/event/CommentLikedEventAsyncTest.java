package com.team1.monew.notification.event;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.event.CommentLikedEvent;
import com.team1.monew.notification.service.NotificationService;
import com.team1.monew.user.entity.User;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@EnableAsync  // 꼭 필요!
class CommentLikedEventAsyncTest {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  void commentLikedEvent_shouldCallNotificationService_async() {
    // given
    User user = User.builder().nickname("작성자").build();
    User likedBy = User.builder().nickname("좋아요한사람").build();
    Comment comment = new Comment(null, user, "comment");
    CommentLikedEvent event = new CommentLikedEvent(comment, likedBy);

    // when
    eventPublisher.publishEvent(event); // 비동기 처리 시작

    // then (Awaitility로 비동기 호출을 기다림)
    await()
        .atMost(1, TimeUnit.SECONDS)
        .untilAsserted(() ->
            verify(notificationService).notifyCommentLiked(comment, likedBy)
        );
  }
}
