package com.team1.monew.subscription.event.listener;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.subscription.event.SubscriptionCreateEvent;
import com.team1.monew.subscription.event.SubscriptionDeleteEvent;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionEventListener {

  private final MongoTemplate mongoTemplate;
  private final RetryTemplate retryTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleSubscriptionCreateEvent(SubscriptionCreateEvent subscriptionCreateEvent) {
    try {
      retryTemplate.execute(context -> {
        Query query = Query.query(Criteria.where("_id").is(subscriptionCreateEvent.userId()));
        Update update = new Update()
            .push("subscriptions", subscriptionCreateEvent.subscriptionDto())
            .set("updatedAt", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, SubscriptionActivity.class);
        return null;
      });
    } catch (Exception e) {
      log.error("Subscription 생성 시 SubscriptionActivity 저장 이벤트 처리 최종 실패 - userId={}",
          subscriptionCreateEvent.userId(), e);
      throw new RestException(ErrorCode.MAX_RETRY_EXCEEDED,
          Map.of("userId", subscriptionCreateEvent.userId(), "details",
              "Subscription 생성 시 SubscriptionActivity 저장 이벤트 처리 최종 실패"));
    }
  }


  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleSubscriptionDeleteEvent(SubscriptionDeleteEvent subscriptionDeleteEvent) {
    try {
      retryTemplate.execute(context -> {
        Query query = Query.query(Criteria.where("_id").is(subscriptionDeleteEvent.userId()));
        Update update = new Update()
            .pull("subscriptions",
                Query.query(Criteria.where("id").is(subscriptionDeleteEvent.subscriptionId()))
                    .getQueryObject())
            .set("updatedAt", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, SubscriptionActivity.class);
        return null;
      });
    } catch (Exception e) {
      log.error("Subscription 삭제 시 SubscriptionActivity 삭제 이벤트 처리 최종 실패 - userId={}",
          subscriptionDeleteEvent.userId(), e);
      throw new RestException(ErrorCode.MAX_RETRY_EXCEEDED,
          Map.of("userId", subscriptionDeleteEvent.userId(), "details",
              "Subscription 삭제 시 SubscriptionActivity 삭제 이벤트 처리 최종 실패"));
    }
  }
}

