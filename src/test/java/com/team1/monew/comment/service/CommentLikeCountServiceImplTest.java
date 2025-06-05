package com.team1.monew.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentLikeCountServiceImplTest {

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentLikeCountServiceImpl commentLikeCountService;

    @Test
    void updateLikeCountByCommentId_정상동작() {
        // given
        Long commentId = 1L;
        long likeCount = 5L;

        Comment comment = mock(Comment.class);

        when(commentLikeRepository.countByCommentId(commentId)).thenReturn(likeCount);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        Long result = commentLikeCountService.updateLikeCountByCommentId(commentId);

        // then
        verify(comment).updateLikeCount(likeCount);
        assertEquals(likeCount, result);
    }

    @Test
    void updateLikeCountByDeletedUser_정상동작() {
        // given
        Long userId = 1L;
        Long commentId1 = 10L;
        Long commentId2 = 20L;

        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);
        when(comment1.getId()).thenReturn(commentId1);
        when(comment2.getId()).thenReturn(commentId2);

        CommentLike like1 = mock(CommentLike.class);
        CommentLike like2 = mock(CommentLike.class);
        when(like1.getComment()).thenReturn(comment1);
        when(like2.getComment()).thenReturn(comment2);

        when(commentLikeRepository.findAllByLikedById(userId)).thenReturn(List.of(like1, like2));
        when(commentRepository.findById(commentId1)).thenReturn(Optional.of(comment1));
        when(commentRepository.findById(commentId2)).thenReturn(Optional.of(comment2));
        when(commentLikeRepository.countByCommentId(commentId1)).thenReturn(3L);
        when(commentLikeRepository.countByCommentId(commentId2)).thenReturn(1L);

        // when
        commentLikeCountService.updateLikeCountByDeletedUser(userId);

        // then
        verify(commentLikeRepository).deleteAllInBatch(List.of(like1, like2));
        verify(comment1).updateLikeCount(3L);
        verify(comment2).updateLikeCount(1L);
    }
}
