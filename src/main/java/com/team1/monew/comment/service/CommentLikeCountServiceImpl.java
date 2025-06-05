package com.team1.monew.comment.service;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CommentLikeCountServiceImpl implements CommentLikeCountService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public void updateLikeCountByDeletedUser(Long userId) {
        log.debug("사용자 삭제로 인한 좋아요 수 갱신 시작 - userId: {}", userId);

        List<CommentLike> likes = commentLikeRepository.findAllByLikedById(userId);

        Set<Long> affectedCommentIds = likes.stream()
            .map(like -> like.getComment().getId())
            .collect(Collectors.toSet());

        commentLikeRepository.deleteAllInBatch(likes);

        for (Long commentId : affectedCommentIds) {
            long newLikeCount = commentLikeRepository.countByCommentId(commentId);

            Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "commentId", commentId,
                    "detail", "좋아요 수 갱신 중 댓글을 찾을 수 없습니다."
                )));

            comment.updateLikeCount(newLikeCount);
        }

        log.debug("사용자 삭제로 인한 좋아요 수 갱신 완료 - userId: {}", userId);
    }

    @Override
    public Long updateLikeCountByCommentId(Long commentId) {
        log.debug("댓글 좋아요 수 갱신 시작 - commentId: {}", commentId);

        long likeCount = commentLikeRepository.countByCommentId(commentId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND, Map.of(
                "commentId", commentId,
                "detail", "댓글을 찾을 수 없습니다."
            )));

        comment.updateLikeCount(likeCount);

        log.debug("댓글 좋아요 수 갱신 완료 - commentId: {}, newLikeCount: {}", commentId, likeCount);

        return likeCount;
    }
}
