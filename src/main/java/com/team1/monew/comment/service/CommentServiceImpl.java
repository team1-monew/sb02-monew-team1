package com.team1.monew.comment.service;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.comment.dto.CommentUpdateRequest;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.mapper.CommentMapper;
import com.team1.monew.comment.mapper.CommentPageResponseMapper;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final CommentPageResponseMapper pageResponseMapper;


    @Transactional
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

        if (article.isDeleted()) {
            log.warn("논리 삭제된 기사에 댓글 등록 요청 - articleId: {}", request.articleId());
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "articleId", request.articleId(),
                "detail", "Cannot comment on a deleted article"
            ));
        }

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

    @Transactional
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
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "commentId", commentId,
                "userId", userId,
                "detail", "You do not have permission to update this comment"
            ));
        }

        if (comment.isDeleted()){
            log.warn("논리 삭제 된 댓글 수정 요청 - commentId: {}", commentId);
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "commentId", commentId,
                "detail", "soft deleted comment cannot be updated"
            ));
        }

        comment.update(request.content());
        log.info("댓글 내용 수정 - newContent: {}", request.content());

        boolean likedByMe = commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, userId);

        CommentDto dto = commentMapper.toDto(comment, likedByMe);
        log.debug("댓글 DTO 반환 - {}", dto);

        return dto;
    }

    @Transactional
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
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "commentId", commentId,
                "userId", userId,
                "detail", "You do not have permission to soft delete this comment"
            ));
        }

        comment.delete();

        log.info("댓글 소프트 삭제 완료 - commentId: {}", commentId);
    }

    @Transactional
    @Override
    public void hardDelete(Long commentId) {
        log.info("댓글 하드 삭제 요청 - commentId: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> {
                log.warn("댓글 조회 실패 - commentId: {}", commentId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "Comment not found"
                ));
            });

        commentRepository.delete(comment);

        log.info("댓글 하드 삭제 완료 - commentId: {}", commentId);
    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponse<CommentDto> findCommentsByArticleId(CommentSearchCondition condition) {
        log.debug("댓글 조회 요청 - orderBy: {}, direction: {}, limit: {}, userId: {}",
            condition.orderBy(), condition.direction(), condition.limit(), condition.userId());
        Slice<Comment> comments = commentRepository.searchByCondition(condition);
        log.debug("댓글 조회 완료 - numberOfElements: {}, hasNext: {}", comments.getNumberOfElements(), comments.hasNext());

        List<CommentDto> commentDtos = comments.stream()
            .map(
                comment -> {
                    boolean likedByMe = commentLikeRepository.existsByComment_IdAndLikedBy_Id(
                        comment.getId(),
                        condition.userId()
                    );
                    return commentMapper.toDto(comment, likedByMe);
                }
            )
            .toList();

        log.debug("댓글 DTO 리스트 생성 - size: {}", commentDtos.size());

        return pageResponseMapper.toPageResponse(commentDtos, condition, comments.hasNext());
    }
}
