package com.team1.monew.comment.dto;

import java.time.LocalDateTime;

public record CommentLikeActivityDto(
    Long id,
    LocalDateTime createdAt,
    Long commentId,
    Long articleId,
    String articleTitle,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    LocalDateTime commentCreatedAt
) {

}
