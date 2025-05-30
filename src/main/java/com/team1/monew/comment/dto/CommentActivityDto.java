package com.team1.monew.comment.dto;

import java.time.LocalDateTime;

public record CommentActivityDto(
    Long id,
    Long articleId,
    String articleTitle,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    LocalDateTime createdAt
) {

}
