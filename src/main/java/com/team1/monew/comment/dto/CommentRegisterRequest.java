package com.team1.monew.comment.dto;

public record CommentRegisterRequest(
    Long articleId,
    Long userId,
    String content
) {

}
