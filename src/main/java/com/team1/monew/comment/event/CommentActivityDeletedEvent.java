package com.team1.monew.comment.event;

public record CommentActivityDeletedEvent(
    Long userId,
    Long commentId
){

}
