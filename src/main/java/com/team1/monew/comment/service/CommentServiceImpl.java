package com.team1.monew.comment.service;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.dto.CommentUpdateRequest;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.mapper.CommentMapper;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto register(CommentRegisterRequest request) {
        log.info("댓글 등록 요청 - articleId: {}, userId: {}", request.articleId(), request.userId());

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> {
                log.warn("사용자 조회 실패 - userId: {}", request.userId());
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "userId", request.userId(),
                    "detail", "User not found"
                ));
            });

        Article article = articleRepository.findById(request.articleId())
            .orElseThrow(() -> {
                log.warn("기사 조회 실패 - articleId: {}", request.articleId());
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "articleId", request.articleId(),
                    "detail", "Article not found"
                ));
            });

        Comment newComment = new Comment(article, user, request.content());

        Comment savedComment = commentRepository.save(newComment);
        log.info("댓글 저장 성공 - commentId: {}", savedComment.getId());

        boolean likedByMe = commentLikeRepository.existsByComment_IdAndLikedBy_Id(
            savedComment.getId(),
            request.userId()
        );

        CommentDto dto = commentMapper.toDto(savedComment, likedByMe);
        log.debug("댓글 DTO 반환 - {}", dto);

        return dto;
    }

    @Override
    public CommentDto update(CommentUpdateRequest request, Long commentId, Long userId) {
        log.info("댓글 수정 요청 - commentId: {}, userId: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> {
                log.warn("댓글 조회 실패 - commentId: {}", commentId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "Comment not found"
                ));
            });

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            log.warn("댓글 수정 권한 없음 - commentId: {}, userId: {}", commentId, userId);
            throw new RestException(ErrorCode.FORBIDDEN, Map.of(
                "commentId", commentId,
                "userId", userId,
                "detail", "You do not have permission to update this comment"
            ));
        }

        comment.update(request.content());
        log.info("댓글 내용 수정 - newContent: {}", request.content());

        boolean likedByMe = commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, userId);

        CommentDto dto = commentMapper.toDto(comment, likedByMe);
        log.debug("댓글 DTO 반환 - {}", dto);

        return dto;
    }

    @Override
    public void softDelete(Long commentId, Long userId) {
        log.info("댓글 소프트 삭제 요청 - commentId: {}, userId: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> {
                log.warn("댓글 조회 실패 - commentId: {}", commentId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "Comment not found"
                ));
            });

        if (!Objects.equals(comment.getUser().getId(), userId)) {
            log.warn("댓글 삭제 권한 없음 - commentId: {}, userId: {}", commentId, userId);
            throw new RestException(ErrorCode.FORBIDDEN, Map.of(
                "commentId", commentId,
                "userId", userId,
                "detail", "You do not have permission to soft delete this comment"
            ));
        }

        comment.delete();

        log.info("댓글 소프트 삭제 완료 - commentId: {}", commentId);
    }
}
