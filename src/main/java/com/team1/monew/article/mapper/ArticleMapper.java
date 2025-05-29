package com.team1.monew.article.mapper;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.entity.Article;
import java.time.Instant;

public class ArticleMapper {

  public static ArticleDto toDto(Article article, Long commentCount, boolean viewedByMe) {
    return ArticleDto.builder()
        .id(article.getId())
        .source(article.getSource())
        .sourceUrl(article.getSourceUrl())
        .title(article.getTitle())
        .publishDate(article.getPublishDate())
        .summary(article.getSummary())
        .commentCount(commentCount)
        .viewCount(article.getViewCount())
        .viewedByMe(viewedByMe)
        .build();
  }

  public static Article toEntity(ArticleDto dto) {
    return Article.builder()
        .source(dto.source())
        .sourceUrl(dto.sourceUrl())
        .title(dto.title())
        .publishDate(dto.publishDate())
        .summary(dto.summary())
        .createdAt(Instant.now())
        .build();
  }
}
