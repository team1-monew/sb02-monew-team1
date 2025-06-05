package com.team1.monew.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
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
