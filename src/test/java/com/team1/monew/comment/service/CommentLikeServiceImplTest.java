package com.team1.monew.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.dto.CommentLikeDto;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.mapper.CommentLikeMapper;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class CommentLikeServiceImplTest {

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentLikeCountService commentLikeCountService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentLikeServiceImpl commentLikeService;

    @Test
    void 댓글좋아요_성공() {
        // given
        Long commentId = 1L;
        Long userId = 2L;
        Long likeCount = 10L;

        User commentUser = mock(User.class);
        when(commentUser.getId()).thenReturn(3L);
        when(commentUser.getNickname()).thenReturn("작성자");

        Article article = mock(Article.class);
        when(article.getId()).thenReturn(5L);
        when(article.getTitle()).thenReturn("기사 제목");

        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(commentId);
        when(comment.getContent()).thenReturn("댓글 내용");
        when(comment.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 6, 12, 4, 0));
        when(comment.isDeleted()).thenReturn(false);
        when(comment.getUser()).thenReturn(commentUser);
        when(comment.getArticle()).thenReturn(article);

        User likedBy = mock(User.class);
        CommentLike savedLike = mock(CommentLike.class);
        when(savedLike.getId()).thenReturn(101L);
        when(savedLike.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 6, 12, 4, 5));

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(likedBy));
        when(commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, userId)).thenReturn(false);
        when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(savedLike);
        when(commentLikeCountService.updateLikeCountByCommentId(commentId)).thenReturn(likeCount);

        CommentLikeDto dto = CommentLikeDto.builder()
            .id(101L)
            .likedBy(userId)
            .createdAt(LocalDateTime.of(2025, 6, 12, 4, 5))
            .commentId(commentId)
            .articleId(5L)
            .commentUserId(3L)
            .commentUserNickname("작성자")
            .commentContent("댓글 내용")
            .commentLikeCount(likeCount)
            .commentCreatedAt(LocalDateTime.of(2025, 6, 12, 4, 0))
            .build();

        when(commentLikeMapper.toDto(savedLike, likeCount)).thenReturn(dto);

        // when
        CommentLikeDto result = commentLikeService.like(commentId, userId);

        // then
        assertEquals(dto.id(), result.id());
        assertEquals(dto.likedBy(), result.likedBy());
        assertEquals(dto.commentLikeCount(), result.commentLikeCount());

        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    void 댓글좋아요_예외_댓글없음() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RestException.class, () -> commentLikeService.like(1L, 2L));
    }

    @Test
    void 댓글좋아요_예외_논리삭제() {
        Comment comment = mock(Comment.class);
        when(comment.isDeleted()).thenReturn(true);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        assertThrows(RestException.class, () -> commentLikeService.like(1L, 2L));
    }

    @Test
    void 댓글좋아요_예외_사용자없음() {
        Comment comment = mock(Comment.class);
        when(comment.isDeleted()).thenReturn(false);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RestException.class, () -> commentLikeService.like(1L, 2L));
    }

    @Test
    void 댓글좋아요_예외_중복좋아요() {
        Comment comment = mock(Comment.class);
        when(comment.isDeleted()).thenReturn(false);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        User user = mock(User.class);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(commentLikeRepository.existsByComment_IdAndLikedBy_Id(1L, 2L)).thenReturn(true);
        assertThrows(RestException.class, () -> commentLikeService.like(1L, 2L));
    }

    @Test
    void 댓글좋아요취소_성공() {
        // given
        Long commentId = 1L;
        Long userId = 2L;
        CommentLike like = mock(CommentLike.class);
        Comment comment = mock(Comment.class);

        when(commentLikeRepository.findByComment_IdAndLikedBy_Id(commentId, userId)).thenReturn(Optional.of(like));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.isDeleted()).thenReturn(false);

        // when
        commentLikeService.unlike(commentId, userId);

        // then
        verify(commentLikeRepository).delete(like);
        verify(commentLikeCountService).updateLikeCountByCommentId(commentId);
    }

    @Test
    void 댓글좋아요취소_예외_좋아요기록없음() {
        when(commentLikeRepository.findByComment_IdAndLikedBy_Id(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RestException.class, () -> commentLikeService.unlike(1L, 2L));
    }

    @Test
    void 댓글좋아요취소_예외_댓글없음() {
        CommentLike like = mock(CommentLike.class);
        when(commentLikeRepository.findByComment_IdAndLikedBy_Id(1L, 2L)).thenReturn(Optional.of(like));
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RestException.class, () -> commentLikeService.unlike(1L, 2L));
    }

    @Test
    void 댓글좋아요취소_예외_댓글논리삭제() {
        CommentLike like = mock(CommentLike.class);
        Comment comment = mock(Comment.class);
        when(comment.isDeleted()).thenReturn(true);
        when(commentLikeRepository.findByComment_IdAndLikedBy_Id(1L, 2L)).thenReturn(Optional.of(like));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        assertThrows(RestException.class, () -> commentLikeService.unlike(1L, 2L));
    }
}
