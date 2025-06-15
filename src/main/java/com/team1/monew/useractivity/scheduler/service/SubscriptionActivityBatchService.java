package com.team1.monew.useractivity.scheduler.service;

import static com.team1.monew.exception.ErrorCode.MAX_RETRY_EXCEEDED;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.exception.RestException;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.retry.support.RetryTemplate;
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
  private final RetryTemplate retryTemplate;

  @Transactional
  public void syncAll() {
    List<User> users = userRepository.findAll();
    List<WriteModel<Document>> operations = new ArrayList<>();
    List<Long> failedUserIds = new ArrayList<>();

    for (User user : users) {
      List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAt(
          user.getId());
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

    if (!operations.isEmpty()) {
      try {
        retryTemplate.execute(context -> {
          mongoTemplate.getCollection("subscription_activities")
              .bulkWrite(operations, new BulkWriteOptions().ordered(false));
          log.info("총 {}건의 SubscriptionActivity 문서가 bulkWrite 되었습니다.", operations.size());
          return null;
        });
      } catch (Exception e) {
        // 로그 분석을 위해 추적 가능한 실패한 유저 ID 추출
        if (e instanceof MongoBulkWriteException mongoEx) {
          List<Long> bulkFailedUserIds = mongoEx.getWriteErrors().stream()
              .map(error -> {
                int index = error.getIndex();
                Document failedDoc = ((ReplaceOneModel<Document>) operations.get(
                    index)).getReplacement();
                return failedDoc.getLong("_id");
              })
              .toList();
          failedUserIds.addAll(bulkFailedUserIds);
        }
        log.error("SubscriptionActivity bulkWrite 재시도 실패 - 최대 재시도 횟수 초과, userIds: {}", failedUserIds, e);
        throw new RestException(
            MAX_RETRY_EXCEEDED,
            Map.of(
                "detail", "구독 활동 내역 bulkWrite 최종 실패",
                "skippedUserIds", failedUserIds.toString()
            )
        );
      }
    }
    log.info("[배치 종료] 구독 활동 내역 MongoDB 동기화 완료");
    log.info("[요약] 전체 사용자 수: {}, 처리 성공 수: {}, 실패 사용자 수: {}",
        users.size(), operations.size(), failedUserIds.size());
  }
}