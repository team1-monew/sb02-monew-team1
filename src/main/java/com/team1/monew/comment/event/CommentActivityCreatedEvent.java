package com.team1.monew.comment.event;

import com.team1.monew.comment.dto.CommentActivityDto;

public record CommentActivityCreatedEvent(
    CommentActivityDto comment
){

}
