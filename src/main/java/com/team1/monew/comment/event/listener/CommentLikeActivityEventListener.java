package com.team1.monew.comment.event.listener;

import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.event.CommentLikeActivityCreatedEvent;
import com.team1.monew.comment.event.CommentLikeActivityDeletedEvent;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.useractivity.document.CommentLikeActivity;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeActivityEventListener {

    private final MongoTemplate mongoTemplate;
    private final RetryTemplate retryTemplate;

    @Async
    @EventListener
    public void handleCommentLikeActivityCreatedEvent(CommentLikeActivityCreatedEvent event) {
        Long userId = event.likedById();
        Long commentId = event.commentLike().commentId();
        CommentLikeActivityDto commentLike = event.commentLike();
        log.debug("CommentLikeActivityCreatedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        retryTemplate.execute(context -> {
            CommentLikeActivity activity = mongoTemplate.findById(userId, CommentLikeActivity.class);
            if (activity == null) {
                log.warn("CommentLikeActivity 문서 없음 - userId: {}", userId);
                return null;
            }

            Long currentVersion = activity.getVersion();

            Query query = Query.query(Criteria.where("_id").is(userId).and("version").is(currentVersion));
            Update update = new Update()
                .push("commentLikes").atPosition(0).value(commentLike)
                .inc("version", 1)
                .set("updatedAt", LocalDateTime.now());

            var result = mongoTemplate.updateFirst(query, update, CommentLikeActivity.class);

            if (result.getModifiedCount() == 0) {
                log.warn("낙관적 락 충돌 - retry 필요 (재시도 횟수: {})", context.getRetryCount());
                throw new IllegalStateException("낙관적 락 충돌 - 추가 실패");
            }

            log.debug("CommentLikeActivity commentLikes add 완료 - commentId: {}", commentId);
            return null;
        }, context -> {
            log.error("CommentLikeActivity 낙관적 락 충돌 - 최대 재시도 실패, userId: {}", userId);
            throw new RestException(ErrorCode.OPTIMISTIC_LOCK_MAX_RETRY_EXCEEDED, Map.of(
                "userId", userId,
                "commentId", commentId,
                "message", "댓글 좋아요 추가 시 낙관적 락 충돌로 인해 활동 기록을 업데이트할 수 없습니다."
            ));
        });
    }

    @Async
    @EventListener
    public void handleCommentLikeActivityDeletedEvent(CommentLikeActivityDeletedEvent event) {
        Long userId = event.userId();
        Long commentLikeId = event.commentLikeId();
        log.debug("CommentLikeActivityDeletedEvent 수신 - userId: {}, commentLikeId: {}", userId, commentLikeId);

        retryTemplate.execute(context -> {
            CommentLikeActivity activity = mongoTemplate.findById(userId, CommentLikeActivity.class);
            if (activity == null) {
                log.warn("CommentLikeActivity 문서 없음 - userId: {}", userId);
                return null;
            }

            Long currentVersion = activity.getVersion();

            Query query = Query.query(Criteria.where("_id").is(userId).and("version").is(currentVersion));
            Update update = new Update()
                .pull("commentLikes", Query.query(Criteria.where("id").is(commentLikeId)).getQueryObject())
                .inc("version", 1)
                .set("updatedAt", LocalDateTime.now());

            var result = mongoTemplate.updateFirst(query, update, CommentLikeActivity.class);

            if (result.getModifiedCount() == 0) {
                log.warn("낙관적 락 충돌 - retry 필요 (재시도 횟수: {})", context.getRetryCount());
                throw new IllegalStateException("낙관적 락 충돌 - 삭제 실패");
            }

            log.debug("CommentLikeActivity commentLikes remove 완료 - commentLikeId: {}", commentLikeId);
            return null;
        }, context -> {
            log.error("CommentLikeActivity 낙관적 락 충돌 - 최대 재시도 실패, userId: {}", userId);
            throw new RestException(ErrorCode.OPTIMISTIC_LOCK_MAX_RETRY_EXCEEDED, Map.of(
                "userId", userId,
                "commentLikeId", commentLikeId,
                "message", "댓글 좋아요 삭제 시 낙관적 락 충돌로 인해 활동 기록을 업데이트할 수 없습니다."
            ));
        });
    }
}
