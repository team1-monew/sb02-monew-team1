package com.team1.monew.article.dto;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ArticleViewDto(
    Long id,
    Long viewedBy,
    LocalDateTime createdAt,
    Long articleId,
    String source,
    String sourceUrl,
    String articleTitle,
    LocalDateTime articlePublishedDate,
    String articleSummary,
    Long articleCommentCount,
    Long articleViewCount
) {

}