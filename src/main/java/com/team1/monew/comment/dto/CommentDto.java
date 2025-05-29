package com.team1.monew.comment.dto;

import java.time.Instant;

public record CommentDto(
    Long id,
    Long articleId,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    boolean likedByMe,
    Instant createdAt
) {

}
