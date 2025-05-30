package com.team1.monew.comment.dto;

import java.time.LocalDateTime;

public record CommentLikeDto(
    Long id,
    Long likedBy,
    LocalDateTime createdAt,
    Long commentId,
    Long articleId,
    Long commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    LocalDateTime commentCreatedAt
) {

}
