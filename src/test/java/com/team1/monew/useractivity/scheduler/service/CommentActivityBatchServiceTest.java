package com.team1.monew.useractivity.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.retry.support.RetryTemplate;

@ExtendWith(MockitoExtension.class)
class CommentActivityBatchServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    MongoTemplate mongoTemplate;

    @Spy
    RetryTemplate retryTemplate;

    @Mock
    MongoCollection<Document> collection;

    @InjectMocks
    CommentActivityBatchService commentActivityBatchService;

    @Test
    void 작성한댓글_bulkWrite_1회호출_및_WriteModel_1건포함됨() {
        // 사용자 생성
        User user = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("pw")
            .build();

        // 게시글 + 댓글
        Article article = mock(Article.class);

        Comment comment = new Comment(article, user, "댓글 내용");
        comment.updateLikeCount(10L);

        // mock 리턴값 설정
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(commentRepository.findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId()))
            .thenReturn(List.of(comment));
        when(mongoTemplate.getCollection("comment_activities")).thenReturn(collection);

        // when
        commentActivityBatchService.syncAll();

        // then
        ArgumentCaptor<List<WriteModel<Document>>> captor = ArgumentCaptor.forClass(List.class);
        verify(collection, times(1)).bulkWrite(captor.capture(), any());

        List<WriteModel<Document>> ops = captor.getValue();
        assertThat(ops).hasSize(1);
    }

}
