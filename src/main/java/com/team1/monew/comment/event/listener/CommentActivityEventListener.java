package com.team1.monew.comment.event.listener;

import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.event.CommentActivityCreatedEvent;
import com.team1.monew.comment.event.CommentActivityDeletedEvent;
import com.team1.monew.comment.event.CommentActivityEditedEvent;
import com.team1.monew.useractivity.document.CommentActivity;
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
public class CommentActivityEventListener {

    private final MongoTemplate mongoTemplate;

    @Async
    @EventListener
    public void handleCommentActivityCreatedEvent(CommentActivityCreatedEvent event) {
        Long userId = event.comment().userId();
        Long commentId = event.comment().id();
        CommentActivityDto comment = event.comment();
        log.debug("CommentActivityCreatedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        Query query = Query.query(Criteria.where("_id").is(userId));
        Update update = new Update()
            .push("comments")
            .atPosition(0)
            .value(comment);

        mongoTemplate.updateFirst(query, update, CommentActivity.class);

        log.debug("CommentActivity comments add - commentId: {}", commentId);
    }

    @Async
    @EventListener
    public void handleCommentActivityDeletedEvent(CommentActivityDeletedEvent event) {
        log.debug("CommentActivityDeletedEvent 수신 - userId: {}, commentId: {}", event.userId(), event.commentId());

        Query query = Query.query(Criteria.where("_id").is(event.userId()));

        Update update = new Update()
            .pull("comments", Query.query(Criteria.where("id").is(event.commentId())).getQueryObject());

        mongoTemplate.updateFirst(query, update, CommentActivity.class);

        log.debug("CommentActivity comments remove - commentId: {}", event.commentId());
    }

    @Async
    @EventListener
    public void handleCommentActivityEditedEvent(CommentActivityEditedEvent event) {
        Long userId = event.userId();
        Long commentId = event.commentId();
        log.debug("CommentActivityUpdatedEvent 수신 - userId: {}, commentId: {}", userId, commentId);

        // CommentActivity 문서에서 댓글 내용 수정
        Query query = Query.query(Criteria.where("_id").is(userId).and("comments.id").is(commentId));
        Update update = new Update().set("comments.$.content", event.newContent());

        mongoTemplate.updateFirst(query, update, CommentActivity.class);
        log.debug("CommentActivity comments update - commentId: {}", commentId);

        // CommentLikeActivity 문서들에서 해당 댓글의 content 수정
        Query commentLikeActivityQuery = Query.query(Criteria.where("commentLikes.commentId").is(commentId));
        Update commentLikeActivityUpdate = new Update()
            .set("commentLikes.$[elem].commentContent", event.newContent())
            .filterArray("elem.commentId", commentId);

        mongoTemplate.updateMulti(commentLikeActivityQuery, commentLikeActivityUpdate, CommentLikeActivity.class);
        log.debug("CommentLikeActivity commentContent update - commentId: {}", commentId);
    }
}
