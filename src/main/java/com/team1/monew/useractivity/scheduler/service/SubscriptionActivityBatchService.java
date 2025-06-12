package com.team1.monew.useractivity.scheduler.service;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionActivityBatchService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;
  private final SubscriptionMapper subscriptionMapper;
  private final MongoTemplate mongoTemplate;

  @Transactional
  public void syncAll() {
    List<User> users = userRepository.findAll();
    List<WriteModel<Document>> operations = new ArrayList<>();

    for (User user : users) {
      List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAt(user.getId());
      List<SubscriptionDto> subscriptionDtos = subscriptions.stream()
          .map(subscriptionMapper::toDto)
          .toList();

      // bulkWrite 연산 - Document를 받음
      Document document = new Document();
      document.put("_id", user.getId());
      document.put("subscriptions", subscriptionDtos);
      document.put("createdAt", LocalDateTime.now());
      document.put("updatedAt", LocalDateTime.now());

      Query query = Query.query(Criteria.where("_id").is(user.getId()));
      ReplaceOneModel<Document> replaceModel = new ReplaceOneModel<>(
          query.getQueryObject(),
          document,
          new ReplaceOptions().upsert(true)
      );

      operations.add(replaceModel);
    }

    try {
      if (!operations.isEmpty()) {
        mongoTemplate.getCollection("subscription_activities")
            .bulkWrite(operations, new BulkWriteOptions().ordered(false));
        log.info("총 {}건의 SubscriptionActivity 문서가 bulkWrite 되었습니다.", operations.size());
      }
    } catch(MongoBulkWriteException e) {
      log.error("SubscriptionActivity bulkWrite 실패 - 일부 문서만 저장되었습니다.", e);
      e.getWriteErrors().forEach(err ->
          log.warn("실패 문서 index: {}, 코드: {}, 메시지: {}",
              err.getIndex(), err.getCode(), err.getMessage()));
    }
  }
}
