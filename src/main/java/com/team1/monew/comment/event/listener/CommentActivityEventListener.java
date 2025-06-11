package com.team1.monew.comment.event.listener;

import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.event.CommentActivityCreatedEvent;
import com.team1.monew.comment.event.CommentActivityDeletedEvent;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.useractivity.document.CommentActivity;
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
public class CommentActivityEventListener {

    private final MongoTemplate mongoTemplate;
    private final RetryTemplate retryTemplate;

    @Async
    @EventListener
    public void handleCommentActivityCreatedEvent(CommentActivityCreatedEvent event) {
        Long userId = event.comment().userId();
        Long commentId = event.comment().id();
        CommentActivityDto comment = event.comment();
        log.debug("CommentActivityCreatedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        retryTemplate.execute(context -> {
            CommentActivity activity = mongoTemplate.findById(userId, CommentActivity.class);
            if (activity == null) {
                log.warn("CommentActivity 문서 없음 - userId: {}", userId);
                return null;
            }

            Long currentVersion = activity.getVersion();

            Query query = Query.query(Criteria.where("_id").is(userId).and("version").is(currentVersion));
            Update update = new Update()
                .push("comments").atPosition(0).value(comment)
                .inc("version", 1)
                .set("updatedAt", LocalDateTime.now());

            var result = mongoTemplate.updateFirst(query, update, CommentActivity.class);

            if (result.getModifiedCount() == 0) {
                log.warn("낙관적 락 충돌 - retry 필요 (재시도 횟수: {})", context.getRetryCount());
                throw new IllegalStateException("낙관적 락 충돌 - 업데이트 실패");
            }

            log.debug("CommentActivity comments add 완료 - commentId: {}", commentId);
            return null;
        }, context -> {
            log.error("CommentActivity 낙관적 락 충돌 - 최대 재시도 실패, userId: {}", userId);
            throw new RestException(ErrorCode.OPTIMISTIC_LOCK_MAX_RETRY_EXCEEDED, Map.of(
                "userId", userId,
                "commentId", commentId,
                "message", "댓글 생성 시 낙관적 락 충돌로 인해 댓글 활동 기록을 업데이트할 수 없습니다."
            ));
        });
    }

    @Async
    @EventListener
    public void handleCommentActivityDeletedEvent(CommentActivityDeletedEvent event) {
        Long userId = event.userId();
        Long commentId = event.commentId();
        log.debug("CommentActivityDeletedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        retryTemplate.execute(context -> {
            CommentActivity activity = mongoTemplate.findById(userId, CommentActivity.class);
            if (activity == null) {
                log.warn("CommentActivity 문서 없음 - userId: {}", userId);
                return null;
            }

            Long currentVersion = activity.getVersion();

            Query query = Query.query(Criteria.where("_id").is(userId).and("version").is(currentVersion));
            Update update = new Update()
                .pull("comments", Query.query(Criteria.where("id").is(commentId)).getQueryObject())
                .inc("version", 1)
                .set("updatedAt", LocalDateTime.now());

            var result = mongoTemplate.updateFirst(query, update, CommentActivity.class);

            if (result.getModifiedCount() == 0) {
                log.warn("낙관적 락 충돌 - retry 필요 (재시도 횟수: {})", context.getRetryCount());
                throw new IllegalStateException("낙관적 락 충돌 - 삭제 실패");
            }

            log.debug("CommentActivity comments remove 완료 - commentId: {}", commentId);
            return null;
        }, context -> {
            log.error("CommentActivity 낙관적 락 충돌 - 최대 재시도 실패, userId: {}", userId);
            throw new RestException(ErrorCode.OPTIMISTIC_LOCK_MAX_RETRY_EXCEEDED, Map.of(
                "userId", userId,
                "commentId", commentId,
                "message", "댓글 삭제 시 낙관적 락 충돌로 인해 댓글 활동 기록을 업데이트할 수 없습니다."
            ));
        });
    }
}
