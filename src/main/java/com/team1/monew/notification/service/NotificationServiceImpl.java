package com.team1.monew.notification.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationPageResponseMapper notificationPageResponseMapper;

  @Override
  public void notifyNewArticles(User user, Interest interest, int articleCount) {
    Notification notification = Notification.builder()
        .user(user)
        .content(String.format("[%s]와 관련된 기사가 %d건 등록되었습니다.", interest.getName(), articleCount))
        .resourceType(ResourceType.INTEREST.getName())
        .resourceId(interest.getId())
        .build();

    notificationRepository.save(notification);

    log.info("신규 기사 알림 생성 - 사용자 ID: {}, 관심사 ID: {}, 기사 수: {}",
        user.getId(), interest.getId(), articleCount);
  }

  @Override
  public void notifyCommentLiked(Comment comment, User likedBy) {
    Notification notification = Notification.builder()
        .user(comment.getUser())
        .content(String.format("[%s]님이 나의 댓글을 좋아합니다.", likedBy.getNickname()))
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(comment.getId())
        .build();

    notificationRepository.save(notification);

    log.info("댓글 좋아요 알림 생성 - 댓글 ID: {}, 좋아요 누른 사용자 ID: {}, 수신자 사용자 ID: {}",
        comment.getId(), likedBy.getId(), comment.getUser().getId());
  }

  @Transactional(readOnly = true)
  @Override
  public CursorPageResponse<NotificationDto> getAllNotifications(
      NotificationCursorRequest request) {

    Slice<NotificationDto> result = notificationRepository.getAllByCursorRequest(request);

    log.info("알림 목록 불러오기 성공 - userId: {}, cursor: {}, after: {}, direction: {}, limit: {}, resultSize: {}, hasNext: {}",
        request.userId(),
        request.cursor(),
        request.after(),
        request.direction(),
        request.limit(),
        result.getContent().size(),
        result.hasNext()
    );

    return notificationPageResponseMapper.toPageResponse(result.getContent(), request, result.hasNext());
  }

  @Override
  public void confirmAll(Long userId) {
    notificationRepository.markAllAsConfirmedByUserId(userId);

    log.info("전체 알림 확인 완료 - userId : {}", userId);
  }

  @Override
  public void confirm(Long notificationId) {
    notificationRepository.markAsConfirmedByNotificationId(notificationId);

    log.info("알림 확인 완료 - notificationId : {}", notificationId);
  }
}