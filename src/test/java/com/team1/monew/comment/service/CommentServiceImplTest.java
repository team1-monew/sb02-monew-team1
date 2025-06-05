package com.team1.monew.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.dto.CommentUpdateRequest;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.mapper.CommentMapper;
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

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void 댓글등록_성공() {
        // given
        Long userId = 1L;
        Long articleId = 10L;
        String content = "좋은 기사 감사합니다.";

        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, content);

        User mockUser = mock(User.class);
        Article mockArticle = mock(Article.class);
        Comment mockComment = mock(Comment.class);
        when(mockComment.getId()).thenReturn(100L);

        CommentDto expectedDto = CommentDto.builder()
            .id(100L)
            .articleId(articleId)
            .userId(userId)
            .userNickname("MockUser")
            .content(content)
            .createdAt(LocalDateTime.now())
            .likeCount(0L)
            .likedByMe(false)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        when(commentMapper.toDto(mockComment, false)).thenReturn(expectedDto);

        // when
        CommentDto result = commentService.register(request);

        // then
        assertEquals(expectedDto.id(), result.id());
        assertEquals(expectedDto.content(), result.content());
        assertEquals(expectedDto.userId(), result.userId());

        verify(userRepository).findById(userId);
        verify(articleRepository).findById(articleId);
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).toDto(mockComment, false);
    }

    @Test
    void 댓글등록_예외발생_사용자없음() {
        // given
        CommentRegisterRequest request = new CommentRegisterRequest(10L, 999L, "test comment");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RestException.class, () -> commentService.register(request));
        verify(userRepository).findById(999L);
    }

    @Test
    void 댓글등록_예외발생_기사없음() {
        // given
        Long userId = 1L;
        Long articleId = 10L;

        User user = mock(User.class);

        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "댓글입니다");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RestException.class, () -> commentService.register(request));
        verify(articleRepository).findById(articleId);
    }

    @Test
    void 댓글등록_예외발생_기사논리삭제() {
        // given
        Long userId = 1L;
        Long articleId = 10L;

        User user = mock(User.class);
        Article article = mock(Article.class);
        when(article.isDeleted()).thenReturn(true);

        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "댓글입니다");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        // when & then
        assertThrows(RestException.class, () -> commentService.register(request));
        verify(articleRepository).findById(articleId);
    }

    @Test
    void 댓글수정_성공() {
        // given
        Long userId = 1L;
        Long commentId = 100L;
        String updatedContent = "수정된 댓글입니다.";

        CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Article mockArticle = mock(Article.class);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);

        CommentDto expectedDto = CommentDto.builder()
            .id(commentId)
            .articleId(mockArticle.getId())
            .userId(userId)
            .userNickname("MockUser")
            .content(updatedContent)
            .createdAt(LocalDateTime.now())
            .likeCount(0L)
            .likedByMe(true)
            .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, userId)).thenReturn(true);
        when(commentMapper.toDto(mockComment, true)).thenReturn(expectedDto);

        // when
        CommentDto result = commentService.update(request, commentId, userId);

        // then
        assertEquals(expectedDto.id(), result.id());
        assertEquals(expectedDto.content(), result.content());
        assertEquals(expectedDto.userId(), result.userId());

        verify(commentRepository).findById(commentId);
        verify(mockComment).update(updatedContent);
        verify(commentMapper).toDto(mockComment, true);
    }

    @Test
    void 댓글수정_예외발생_댓글없음() {
        // given
        Long commentId = 999L;
        Long userId = 1L;
        CommentUpdateRequest request = new CommentUpdateRequest("수정");

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RestException.class, () -> commentService.update(request, commentId, userId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글수정_예외발생_댓글논리삭제() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        Long commentId = 999L;
        Comment comment = mock(Comment.class);
        when(comment.getUser()).thenReturn(user);
        when(comment.isDeleted()).thenReturn(true);
        CommentUpdateRequest request = new CommentUpdateRequest("수정");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when & then
        assertThrows(RestException.class, () -> commentService.update(request, commentId, userId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글수정_예외발생_권한없음() {
        // given
        Long commentId = 100L;
        Long writerId = 1L;
        Long otherUserId = 2L;

        CommentUpdateRequest request = new CommentUpdateRequest("수정");

        User writer = mock(User.class);
        when(writer.getId()).thenReturn(writerId);

        Comment comment = mock(Comment.class);
        when(comment.getUser()).thenReturn(writer);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when & then
        assertThrows(RestException.class, () -> commentService.update(request, commentId, otherUserId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글논리삭제_성공() {
        // given
        Long commentId = 100L;
        Long userId = 1L;

        User writer = mock(User.class);
        when(writer.getId()).thenReturn(userId);

        Comment comment = mock(Comment.class);
        when(comment.getUser()).thenReturn(writer);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.softDelete(commentId, userId);

        // then
        verify(commentRepository).findById(commentId);
        verify(comment).delete();
    }

    @Test
    void 댓글논리삭제_예외발생_댓글없음() {
        // given
        Long commentId = 999L;
        Long userId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RestException.class, () -> commentService.softDelete(commentId, userId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글논리삭제_예외발생_권한없음() {
        // given
        Long commentId = 100L;
        Long writerId = 1L;
        Long otherUserId = 2L;

        User writer = mock(User.class);
        when(writer.getId()).thenReturn(writerId);

        Comment comment = mock(Comment.class);
        when(comment.getUser()).thenReturn(writer);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when & then
        assertThrows(RestException.class, () -> commentService.softDelete(commentId, otherUserId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글물리삭제_성공() {
        // given
        Long commentId = 100L;

        Comment mockComment = mock(Comment.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        // when
        commentService.hardDelete(commentId);

        // then
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(mockComment);
    }

    @Test
    void 댓글물리삭제_예외발생_댓글없음() {
        // given
        Long commentId = 999L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RestException.class, () -> commentService.hardDelete(commentId));
        verify(commentRepository).findById(commentId);
    }


}
