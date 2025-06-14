package com.team1.monew.subscription.event.listener;

import com.team1.monew.subscription.event.SubscriptionCreateEvent;
import com.team1.monew.subscription.event.SubscriptionDeleteEvent;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionEventListener {

  private final MongoTemplate mongoTemplate;

  @EventListener
  @Async
  public void handelSubscriptionCreateEvent(SubscriptionCreateEvent subscriptionCreateEvent) {
    try {
      Query query = Query.query(Criteria.where("_id").is(subscriptionCreateEvent.userId()));
      Update update = new Update()
          .push("subscriptions", subscriptionCreateEvent.subscriptionDto())
          .set("updatedAt", LocalDateTime.now());
      mongoTemplate.updateFirst(query, update, SubscriptionActivity.class);
    } catch (Exception e) {
      log.error("Subscription 이벤트 처리 실패 - SubscriptionActivity 저장 중 예외 발생: userId={}, err={}", subscriptionCreateEvent.userId(), e.getMessage(), e);
    }
  }

  @EventListener
  @Async
  public void handelSubscriptionDeleteEvent(SubscriptionDeleteEvent subscriptionDeleteEvent) {
    try {
      Query query = Query.query(Criteria.where("_id").is(subscriptionDeleteEvent.userId()));
      Update update = new Update()
          .pull("subscriptions",
              Query.query(Criteria.where("id").is(subscriptionDeleteEvent.subscriptionId()))
                  .getQueryObject())
          .set("updatedAt", LocalDateTime.now());
      mongoTemplate.updateFirst(query, update, SubscriptionActivity.class);
    } catch (Exception e) {
      log.error("Subscription 이벤트 처리 실패 - SubscriptionActivity 삭제 중 예외 발생: userId={}, err={}", subscriptionDeleteEvent.userId(), e.getMessage(), e);
    }
  }
}
