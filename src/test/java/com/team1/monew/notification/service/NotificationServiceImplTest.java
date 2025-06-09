package com.team1.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.notification.repository.NotificationRepository;
import com.team1.monew.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
  @Mock
  NotificationRepository notificationRepository;

  @InjectMocks
  NotificationServiceImpl notificationService;

  @Test
  void notifyNewArticles() {
    // given
    User subscriber = User.builder().build();
    Interest interest = new Interest("관심사1");
    ReflectionTestUtils.setField(interest, "id", 1L);
    int articleCount = 1;

    // when
    notificationService.notifyNewArticles(subscriber, interest, articleCount);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getUser()).isEqualTo(subscriber);
    assertThat(saved.getResourceType()).isEqualTo(ResourceType.INTEREST.getName());
    assertThat(saved.getResourceId()).isEqualTo(interest.getId());
  }

  @Test
  void notifyCommentLiked() {
    // given
    User commentAuthor = User.builder().build();
    Comment comment = new Comment(null, commentAuthor, null);
    ReflectionTestUtils.setField(comment, "id", 1L);
    User likedBy = User.builder()
        .email("commentLiker@email.com")
        .nickname("commentLiker")
        .password("commentLiker123@@@")
        .build();

    // when
    notificationService.notifyCommentLiked(comment, likedBy);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getUser()).isEqualTo(commentAuthor);
    assertThat(saved.getResourceType()).isEqualTo(ResourceType.COMMENT.getName());
    assertThat(saved.getResourceId()).isEqualTo(comment.getId());
  }
}