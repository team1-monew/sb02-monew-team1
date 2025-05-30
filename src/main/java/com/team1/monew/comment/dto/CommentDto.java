package com.team1.monew.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CommentDto(
    Long id,
    Long articleId,
    Long userId,
    String userNickname,
    String content,
    Long likeCount,
    boolean likedByMe,
    LocalDateTime createdAt
) {

}
