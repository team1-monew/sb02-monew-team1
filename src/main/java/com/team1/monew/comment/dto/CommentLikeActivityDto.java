package com.team1.monew.comment.dto;

import java.time.Instant;

public record CommentLikeActivityDto(
    Long id,
    Instant createdAt,
    Long commentId,
    Long articleId,
    String articleTitle,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) {

}
