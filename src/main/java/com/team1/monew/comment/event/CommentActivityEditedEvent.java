package com.team1.monew.comment.event;

public record CommentActivityEditedEvent(
    Long userId,
    Long commentId,
    String newContent
){

}
