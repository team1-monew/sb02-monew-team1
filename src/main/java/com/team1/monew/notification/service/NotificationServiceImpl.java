package com.team1.monew.notification.service;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.notification.repository.NotificationRepository;
import com.team1.monew.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final NotificationRepository notificationRepository;

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
}
