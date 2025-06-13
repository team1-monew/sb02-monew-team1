package com.team1.monew.article.event.listener;

import com.mongodb.client.model.Filters;
import com.team1.monew.article.event.ArticleViewCreateEvent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleViewEventListener {

  private final MongoTemplate mongoTemplate;

  @EventListener
  @Async
  public void handleArticleViewCreateEvent(ArticleViewCreateEvent articleViewCreateEvent) {
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
  }
}
