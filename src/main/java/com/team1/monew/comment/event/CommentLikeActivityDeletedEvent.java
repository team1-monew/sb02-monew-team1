package com.team1.monew.comment.event;

public record CommentLikeActivityDeletedEvent(
    Long userId,
    Long commentLikeId
){

}
