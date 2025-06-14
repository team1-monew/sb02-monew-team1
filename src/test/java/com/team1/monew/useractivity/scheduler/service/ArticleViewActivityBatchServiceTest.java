package com.team1.monew.useractivity.scheduler.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.article.entity.Article;
import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.article.repository.ArticleViewRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleViewActivityBatchServiceTest {

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    UserRepository userRepository;

    @Mock
    ArticleViewRepository articleViewRepository;

    @InjectMocks
    ArticleViewActivityBatchService articleViewActivityBatchService;

    @Captor
    ArgumentCaptor<List<WriteModel<Document>>> captor;

    @Test
    @DisplayName("article_view RDBMS -> MongoDB sync 작업 성공")
    void syncAll_success() {
        // given
        User user1 = User.builder().email("user1@test.com").nickname("user1").password("pw1").build();
        User user2 = User.builder().email("user2@test.com").nickname("user2").password("pw2").build();
        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 2L);

        ArticleView view1 = new ArticleView(mock(Article.class), user1);
        ArticleView view2 = new ArticleView(mock(Article.class), user2);

        given(userRepository.findAll()).willReturn(List.of(user1, user2));
        given(articleViewRepository.findValidArticleViewsByUserIdOrderByCreatedAt(user1.getId())).willReturn(List.of(view1));
        given(articleViewRepository.findValidArticleViewsByUserIdOrderByCreatedAt(user2.getId())).willReturn(List.of(view2));
        given(mongoTemplate.getCollection("article_view_activities")).willReturn(mock(MongoCollection.class));

        // when
        articleViewActivityBatchService.syncAll();

        // then
        then(mongoTemplate.getCollection("article_view_activities"))
            .should(times(1))
            .bulkWrite(captor.capture(), any(BulkWriteOptions.class));

        List<WriteModel<Document>> writeModels = captor.getValue();
        Document doc1 = ((ReplaceOneModel<Document>) writeModels.get(0)).getReplacement();
        Document doc2 = ((ReplaceOneModel<Document>) writeModels.get(1)).getReplacement();

        assertThat(writeModels).hasSize(2);
        assertThat(doc1.get("_id")).isEqualTo(user1.getId());
        assertThat(doc2.get("_id")).isEqualTo(user2.getId());
        assertThat(doc1.get("articleViews")).isNotNull();
        assertThat(doc2.get("articleViews")).isNotNull();
        assertThat(doc1.get("createdAt")).isInstanceOf(LocalDateTime.class);
        assertThat(doc2.get("createdAt")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("sync 작업 중 bulkWrite 예외 발생 - 예외 처리가 정상적으로 동작함")
    void syncAll_whenBulkWriteFails_thenHandledException() {
        // given
        User user = User.builder()
            .email("fail@test.com")
            .nickname("failUser")
            .password("Fail1234!")
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        ArticleView articleView = new ArticleView(mock(Article.class), user); // null 대신 실제 Article entity가 있으면 사용

        given(userRepository.findAll()).willReturn(List.of(user));
        given(articleViewRepository.findValidArticleViewsByUserIdOrderByCreatedAt(user.getId()))
            .willReturn(List.of(articleView));

        MongoCollection<Document> mockCollection = mock(MongoCollection.class);
        given(mongoTemplate.getCollection("article_view_activities")).willReturn(mockCollection);

        MongoBulkWriteException mockException = mock(MongoBulkWriteException.class);
        given(mockCollection.bulkWrite(any(), any(BulkWriteOptions.class))).willThrow(mockException);
        given(mockException.getWriteErrors()).willReturn(List.of()); // catch 블록 실행 검증용

        // when + then
        assertDoesNotThrow(() -> articleViewActivityBatchService.syncAll());
        then(mockException).should().getWriteErrors();
    }
}