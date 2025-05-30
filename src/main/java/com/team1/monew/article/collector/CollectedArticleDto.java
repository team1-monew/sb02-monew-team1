package com.team1.monew.article.collector;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CollectedArticleDto(
    String title,
    String summary,
    String sourceUrl,
    String source,
    LocalDateTime publishDate
) {

}
