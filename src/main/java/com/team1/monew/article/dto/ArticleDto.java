package com.team1.monew.article.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ArticleDto(
    Long id,
    String source,
    String sourceUrl,
    String title,
    LocalDateTime publishDate,
    String summary,
    Long commentCount,
    Long viewCount,
    boolean viewedByMe
) {

}