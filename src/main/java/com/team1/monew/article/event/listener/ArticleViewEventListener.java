package com.team1.monew.article.event.listener;

import com.mongodb.client.model.Filters;
import com.team1.monew.article.event.ArticleViewCreateEvent;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleViewEventListener {

  private final MongoTemplate mongoTemplate;
  private final RetryTemplate retryTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleArticleViewCreateEvent(ArticleViewCreateEvent articleViewCreateEvent) {
    try {
      retryTemplate.execute(context -> {
        Document push = new Document()
            .append("$each", List.of(articleViewCreateEvent.articleViewDto()))
            .append("$position", 0); // 배열 맨 앞에 추가

        Document update = new Document("$push", new Document("articleViews", push))
            .append("$set", new Document("updatedAt", LocalDateTime.now()));

        mongoTemplate.getDb()
            .getCollection("article_view_activities")
            .updateOne(
                Filters.eq("_id", articleViewCreateEvent.userId()),
                update
            );
        return null;
      });
    } catch (Exception e) {
      log.error("ArticleView 생성 시 ArticleViewActivity 저장 이벤트 처리 최종 실패 - userId={}",
          articleViewCreateEvent.userId(), e);
      throw new RestException(ErrorCode.MAX_RETRY_EXCEEDED,
          Map.of("userId", articleViewCreateEvent.userId(), "details",
              "ArticleView 생성 시 ArticleViewActivity 저장 이벤트 처리 최종 실패"));
    }
  }
}
