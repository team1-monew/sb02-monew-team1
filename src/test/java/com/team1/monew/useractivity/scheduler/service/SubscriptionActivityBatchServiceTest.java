package com.team1.monew.useractivity.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class SubscriptionActivityBatchServiceTest {

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    UserRepository userRepository;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @Spy
    SubscriptionMapper subscriptionMapper;

    @InjectMocks
    SubscriptionActivityBatchService subscriptionActivityBatchService;

    @Captor
    ArgumentCaptor<List<WriteModel<Document>>> captor;

    @Test
    @DisplayName("subscription RDBMS <-> MongoDB sync 작업 성공")
    void syncAll_success() {
        // given
        User user1 = User.builder()
            .email("test1@test.com")
            .nickname("test1")
            .password("Test1234!")
            .build();
        User user2 = User.builder()
            .email("test2@test.com")
            .nickname("test2")
            .password("Test1234!")
            .build();
        ReflectionTestUtils.setField(user1,"id", 1L);
        ReflectionTestUtils.setField(user2,"id", 2L);

        Interest interest = new Interest("여행");

        Subscription subscription1 = new Subscription(user1, interest);
        Subscription subscription2 = new Subscription(user2, interest);

        given(userRepository.findAll()).willReturn(List.of(user1, user2));
        given(subscriptionRepository.findByUserIdOrderByCreatedAt(user1.getId())).willReturn(List.of(subscription1));
        given(subscriptionRepository.findByUserIdOrderByCreatedAt(user2.getId())).willReturn(List.of(subscription2));
        given(mongoTemplate.getCollection("subscription_activities")).willReturn(mock(MongoCollection.class));

        // when
        subscriptionActivityBatchService.syncAll();

        // then
        then(mongoTemplate.getCollection("subscription_activities"))
            .should(times(1))
            .bulkWrite(captor.capture(), any(BulkWriteOptions.class));

        List<WriteModel<Document>> writeModels = captor.getValue();
        Document document1 = ((ReplaceOneModel<Document>) writeModels.get(0)).getReplacement();
        Document document2 = ((ReplaceOneModel<Document>) writeModels.get(1)).getReplacement();

        assertThat(writeModels).hasSize(2);
        assertThat(document1.get("_id")).isEqualTo(user1.getId());
        assertThat(document2.get("_id")).isEqualTo(user2.getId());
        assertThat(document1.get("subscriptions")).isNotNull();
        assertThat(document2.get("subscriptions")).isNotNull();
        assertThat(document1.get("createdAt")).isInstanceOf(LocalDateTime.class);
        assertThat(document2.get("createdAt")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("sync 작업 중 bulkWrite 예외 발생 - 예외 처리가 정상적으로 동작함")
    void syncAll_whenBulkWriteFails_thenExceptionHandled() {
        // given
        User user = User.builder()
            .email("fail@test.com")
            .nickname("failUser")
            .password("Fail1234!")
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Interest interest = new Interest("여행");
        Subscription subscription = new Subscription(user, interest);

        given(userRepository.findAll()).willReturn(List.of(user));
        given(subscriptionRepository.findByUserIdOrderByCreatedAt(user.getId()))
            .willReturn(List.of(subscription));

        MongoCollection<Document> mockCollection = mock(MongoCollection.class);
        given(mongoTemplate.getCollection("subscription_activities")).willReturn(mockCollection);

        MongoBulkWriteException mockException = mock(MongoBulkWriteException.class);
        given(mockCollection.bulkWrite(any(), any(BulkWriteOptions.class))).willThrow(mockException);

        // when + then
        assertDoesNotThrow(() -> subscriptionActivityBatchService.syncAll());
        then(mockException).should().getWriteErrors();
    }
}
