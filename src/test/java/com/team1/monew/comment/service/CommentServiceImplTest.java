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
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.mapper.CommentMapper;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.Instant;
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
            .createdAt(Instant.now())
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
}
