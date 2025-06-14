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
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
class CommentLikeActivityBatchServiceTest {

    @Test
    void 좋아요한댓글_bulkWrite가_정상_호출() {
        // given
        UserRepository userRepository = mock(UserRepository.class);
        CommentLikeRepository commentLikeRepository = mock(CommentLikeRepository.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoCollection<Document> collection = mock(MongoCollection.class);

        CommentLikeActivityBatchService service =
            new CommentLikeActivityBatchService(userRepository, commentLikeRepository, mongoTemplate);

        User user = User.builder().email("like@test.com").nickname("좋아요").password("pw").build();
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1L);
        when(article.getTitle()).thenReturn("아티클");

        Comment comment = new Comment(article, user, "좋아요한 댓글");
        comment.updateLikeCount(5L);

        CommentLike commentLike = mock(CommentLike.class);
        when(commentLike.getId()).thenReturn(100L);
        when(commentLike.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(commentLike.getComment()).thenReturn(comment);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(commentLikeRepository.findWithCommentByLikedById(user.getId())).thenReturn(List.of(commentLike));
        when(mongoTemplate.getCollection("comment_like_activities")).thenReturn(collection);

        // when
        service.syncAll();

        // then
        ArgumentCaptor<List<WriteModel<Document>>> captor = ArgumentCaptor.forClass(List.class);
        verify(collection, times(1)).bulkWrite(captor.capture(), any());

        List<WriteModel<Document>> ops = captor.getValue();
        assertThat(ops).hasSize(1);
    }
}
