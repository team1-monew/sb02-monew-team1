package com.team1.monew.notification.event;

import com.team1.monew.article.event.NewArticlesCollectedEvent;
import com.team1.monew.comment.event.CommentLikedEvent;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.notification.service.NotificationService;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import java.util.List;
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

  private final SubscriptionRepository subscriptionRepository;

  @Async
  @EventListener
  public void handleNewArticlesEvent(NewArticlesCollectedEvent event) {
    log.info("새로운 기사 이벤트 처리 중 - 관심사 이름: {}, 기사 개수: {}",
        event.getInterest().getName(),
        event.getArticles().size());

    Interest interest = event.getInterest();
    List<User> subscribers = subscriptionRepository.findAllByInterestWithUser(interest)
        .stream()
        .map(Subscription::getUser)
        .toList();

    try {
      for (User user : subscribers) {
        notificationService.notifyNewArticles(user, interest, event.getArticles().size());
      }
      log.info("새 기사 수집 알림 전송 완료 - 관심사 ID: {}, 알림 대상 사용자 수: {}",
          interest.getId(), subscribers.size());
    } catch (Exception e) {
      log.error("새 기사 수집 알림 전송 실패 - 관심사 ID: {}", interest.getId(), e);
    }
  }

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