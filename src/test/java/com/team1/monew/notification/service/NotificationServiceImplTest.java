package com.team1.monew.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.notification.mapper.NotificationPageResponseMapper;
import com.team1.monew.notification.repository.NotificationRepository;
import com.team1.monew.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
  @Mock
  NotificationRepository notificationRepository;

  @Mock
  private NotificationPageResponseMapper notificationPageResponseMapper;

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

  @Test
  void getAllNotifications() {
    // given
    NotificationCursorRequest request = NotificationCursorRequest.builder()
        .userId(1L)
        .direction("DESC")
        .cursor(null)
        .after(null)
        .limit(10)
        .build();

    List<NotificationDto> dtoList = List.of(
        NotificationDto.builder()
            .id(11L)
            .createdAt(null)
            .updatedAt(null)
            .confirmed(false)
            .userId(1L)
            .content("content")
            .resourceType(ResourceType.COMMENT.getName())
            .resourceId(null)
            .build()
        );

    Slice<NotificationDto> slice = new SliceImpl<>(dtoList, PageRequest.of(0, request.limit()), false);

    CursorPageResponse<NotificationDto> expectedResponse =
        new CursorPageResponse<>(dtoList, null, null, 1L, null, false);

    // stub
    given(notificationRepository.getAllByCursorRequest(any())).willReturn(slice);
    given(notificationPageResponseMapper.toPageResponse(anyList(), any(), anyBoolean()))
        .willReturn(expectedResponse);

    // when
    CursorPageResponse<NotificationDto> result = notificationService.getAllNotifications(request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
    verify(notificationRepository).getAllByCursorRequest(request);
    verify(notificationPageResponseMapper)
        .toPageResponse(dtoList, request, false);
  }
}