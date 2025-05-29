package com.team1.monew.comment.dto;

import java.time.Instant;

public record CommentLikeDto(
    Long id,
    Long likedBy,
    Instant createdAt,
    Long commentId,
    Long articleId,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) {

}
