package com.team1.monew.article.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ArticleViewDto(
    Long id,
    Long viewedBy,
    Instant createdAt,
    Long articleId,
    String source,
    String sourceUrl,
    String articleTitle,
    Instant articlePublishedDate,
    String articleSummary,
    Long articleCommentCount,
    Long articleViewCount
) {

}