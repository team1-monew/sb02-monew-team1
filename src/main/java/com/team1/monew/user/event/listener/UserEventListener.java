package com.team1.monew.user.event.listener;

import com.team1.monew.user.event.UserCreateEvent;
import com.team1.monew.user.event.UserDeleteEvent;
import com.team1.monew.useractivity.document.ArticleViewActivity;
import com.team1.monew.useractivity.document.CommentActivity;
import com.team1.monew.useractivity.document.CommentLikeActivity;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

  private final MongoTemplate mongoTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void userCreateEventHandler(UserCreateEvent userCreateEvent) {
    Long userId = userCreateEvent.userId();

    SubscriptionActivity subscriptionActivity = SubscriptionActivity.builder()
        .userId(userId)
        .subscriptions(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    ArticleViewActivity articleViewActivity = ArticleViewActivity.builder()
        .userId(userId)
        .articleViews(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    CommentActivity commentActivity = CommentActivity.builder()
        .userId(userId)
        .comments(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    CommentLikeActivity commentLikeActivity = CommentLikeActivity.builder()
        .userId(userId)
        .commentLikes(new ArrayList<>())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    int retryCount = 0;
    int maxRetries = 3;
    long delayMillis = 1000;

    while (retryCount < maxRetries) {
      try {
        mongoTemplate.save(subscriptionActivity);
        mongoTemplate.save(articleViewActivity);
        mongoTemplate.save(commentActivity);
        mongoTemplate.save(commentLikeActivity);
        return;
      } catch (Exception e) {
        retryCount++;
        if (retryCount >= maxRetries) {
          log.error("유저 생성 시 활동 내역 초기 document 생성 최종 실패", e);
        } else {
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException ignored) {}
        }
      }
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void userDeleteEventHandler(UserDeleteEvent userDeleteEvent) {
    Long userId = userDeleteEvent.userId();


    int retryCount = 0;
    int maxRetries = 3;
    long delayMillis = 1000;

    while (retryCount < maxRetries) {
      try {
        Query query = Query.query(Criteria.where("_id").is(userId));

        mongoTemplate.remove(query, SubscriptionActivity.class);
        mongoTemplate.remove(query, ArticleViewActivity.class);
        mongoTemplate.remove(query, CommentActivity.class);
        mongoTemplate.remove(query, CommentLikeActivity.class);

        return;
      } catch (Exception e) {
        retryCount++;
        if (retryCount >= maxRetries) {
          log.error("유저 삭제 시 활동 내역 document 삭제 최종 실패 - userId: {}", userId, e);
        } else {
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException ignored) {}
        }
      }
    }
  }
}
