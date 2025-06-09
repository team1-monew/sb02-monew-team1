package com.team1.monew.notification.event;

import com.team1.monew.comment.event.CommentLikedEvent;
import com.team1.monew.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async
  @EventListener
  public void handleCommentLikedEvent(CommentLikedEvent event) {
    log.info("댓글 좋아요 이벤트 처리 중 - 댓글 ID: {}, 좋아요 누른 사용자 ID: {}",
        event.getComment().getId(),
        event.getLikedBy().getId());

    try {
      notificationService.notifyCommentLiked(event.getComment(), event.getLikedBy());
      log.info("댓글 좋아요 알림 전송 완료");
    } catch (Exception e) {
      log.error("댓글 좋아요 알림 전송 실패", e);
    }
  }
}