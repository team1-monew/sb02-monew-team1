package com.team1.monew.article.mapper;

import com.team1.monew.article.dto.CollectedArticleDto;
import java.time.LocalDateTime;

public class CollectedArticleMapper {

  public static CollectedArticleDto toDto(String title, String summary, String sourceUrl, String source, LocalDateTime publishDate) {
    return CollectedArticleDto.builder()
        .title(title)
        .summary(summary)
        .sourceUrl(sourceUrl)
        .source(source)
        .publishDate(publishDate)
        .build();
  }
}