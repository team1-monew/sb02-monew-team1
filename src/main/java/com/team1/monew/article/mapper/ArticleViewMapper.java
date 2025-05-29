package com.team1.monew.article.mapper;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.entity.ArticleView;

public class ArticleViewMapper {

  public static ArticleViewDto toDto(ArticleView articleView, Long commentCount) {
    return ArticleViewDto.builder()
        .id(articleView.getId())
        .viewedBy(articleView.getViewedBy().getId())
        .createdAt(articleView.getCreatedAt())
        .articleId(articleView.getArticle().getId())
        .source(articleView.getArticle().getSource())
        .sourceUrl(articleView.getArticle().getSourceUrl())
        .articleTitle(articleView.getArticle().getTitle())
        .articlePublishedDate(articleView.getArticle().getPublishDate())
        .articleSummary(articleView.getArticle().getSummary())
        .articleCommentCount(commentCount)
        .articleViewCount(articleView.getArticle().getViewCount())
        .build();
  }
}
