package com.team1.monew.comment.service;

import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.dto.CommentLikeDto;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.event.CommentLikeActivityCreatedEvent;
import com.team1.monew.comment.event.CommentLikeActivityDeletedEvent;
import com.team1.monew.comment.mapper.CommentLikeMapper;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentLikeCountService commentLikeCountService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeMapper commentLikeMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public CommentLikeDto like(Long commentId, Long userId) {
        log.info("댓글 좋아요 요청 - commentId: {}, userId: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> {
                log.warn("댓글 조회 실패 - commentId: {}", commentId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "Comment not found"
                ));
            });

        if (comment.isDeleted()) {
            log.warn("논리 삭제된 댓글에 좋아요 시도 - commentId: {}", commentId);
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "commentId", commentId,
                "detail", "Cannot like a soft deleted comment"
            ));
        }

        User likedBy = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("사용자 조회 실패 - userId: {}", userId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "userId", userId,
                    "detail", "User not found"
                ));
            });

        if (commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, userId)) {
            log.warn("이미 좋아요한 댓글 - commentId: {}, userId: {}", commentId, userId);
            throw new RestException(ErrorCode.CONFLICT, Map.of(
                "commentId", commentId,
                "userId", userId,
                "detail", "Comment already liked"
            ));
        }

        CommentLike commentLike = new CommentLike(comment, likedBy);
        CommentLike savedCommentLike = commentLikeRepository.save(commentLike);
        log.info("댓글 좋아요 완료 - commentId: {}, userId: {}", commentId, userId);

        // likeCount 업데이트
        Long likeCount = commentLikeCountService.updateLikeCountByCommentId(commentId);

        CommentLikeDto dto = commentLikeMapper.toDto(savedCommentLike, likeCount);
        log.debug("댓글 좋아요 DTO 반환 - {}", dto);

        // 이벤트 발행
        CommentLikeActivityDto eventDto = CommentLikeActivityDto.builder()
            .id(savedCommentLike.getId())
            .createdAt(savedCommentLike.getCreatedAt())
            .commentId(comment.getId())
            .articleId(comment.getArticle().getId())
            .articleTitle(comment.getArticle().getTitle())
            .commentUserId(comment.getUser().getId())
            .commentUserNickname(comment.getUser().getNickname())
            .commentContent(comment.getContent())
            .commentLikeCount(likeCount)
            .commentCreatedAt(comment.getCreatedAt())
            .build();

        eventPublisher.publishEvent(new CommentLikeActivityCreatedEvent(eventDto, userId));

        return dto;
    }

    @Transactional
    @Override
    public void unlike(Long commentId, Long userId) {
        log.info("댓글 좋아요 취소 요청 - commentId: {}, userId: {}", commentId, userId);

        CommentLike commentLike = commentLikeRepository.findByComment_IdAndLikedBy_Id(commentId, userId)
            .orElseThrow(() -> {
                log.warn("좋아요 기록 없음 - commentId: {}, userId: {}", commentId, userId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "userId", userId,
                    "detail", "CommentLike not found"
                ));
            });

        Long commentLikeId = commentLike.getId();

        commentLikeRepository.delete(commentLike);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()->{
                log.warn("댓글 조회 실패 - commentId: {}", commentId);
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "Comment not found"
                ));
            });

        if (comment.isDeleted()) {
            log.warn("논리 삭제된 댓글에 좋아요 취소 시도 - commentId: {}", commentId);
            throw new RestException(ErrorCode.ACCESS_DENIED, Map.of(
                "commentId", commentId,
                "detail", "Cannot unlike a soft deleted comment"
            ));
        }

        // likeCount 업데이트
        commentLikeCountService.updateLikeCountByCommentId(commentId);

        // 이벤트 발행
        eventPublisher.publishEvent(new CommentLikeActivityDeletedEvent(userId, commentLikeId));

        log.info("댓글 좋아요 취소 완료 - commentId: {}, userId: {}", commentId, userId);
    }
}
