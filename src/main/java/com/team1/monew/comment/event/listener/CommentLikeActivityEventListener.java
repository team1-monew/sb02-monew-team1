package com.team1.monew.comment.event.listener;

import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.event.CommentLikeActivityCreatedEvent;
import com.team1.monew.comment.event.CommentLikeActivityDeletedEvent;
import com.team1.monew.useractivity.document.CommentLikeActivity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeActivityEventListener {

    private final MongoTemplate mongoTemplate;

    @Async
    @EventListener
    public void handleCommentLikeActivityCreatedEvent(CommentLikeActivityCreatedEvent event) {
        Long userId = event.likedById();
        Long commentId = event.commentLike().commentId();
        CommentLikeActivityDto commentLike = event.commentLike();
        log.debug("CommentLikeActivityCreatedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        Query query = Query.query(Criteria.where("_id").is(userId));
        Update update = new Update()
            .push("commentLikes")
            .atPosition(0)
            .value(commentLike);

        mongoTemplate.updateFirst(query, update, CommentLikeActivity.class);

        log.debug("CommentLikeActivity commentLikes add - commentId: {}", commentId);
    }

    @Async
    @EventListener
    public void handleCommentLikeActivityDeletedEvent(CommentLikeActivityDeletedEvent event) {
        log.debug("CommentActivityDeletedEvent 수신 - userId: {}, commentLikeId: {}", event.userId(), event.commentLikeId());

        Query query = Query.query(Criteria.where("_id").is(event.userId()));

        Update update = new Update()
            .pull("commentLikes", Query.query(Criteria.where("id").is(event.commentLikeId())).getQueryObject());

        mongoTemplate.updateFirst(query, update, CommentLikeActivity.class);

        log.debug("CommentActivity commentLikes remove - commentLikeId: {}", event.commentLikeId());
    }
}
