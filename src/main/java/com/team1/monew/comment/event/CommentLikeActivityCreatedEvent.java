package com.team1.monew.comment.event;

import com.team1.monew.comment.dto.CommentLikeActivityDto;

public record CommentLikeActivityCreatedEvent(
    CommentLikeActivityDto commentLike,
    Long likedById
){

}
