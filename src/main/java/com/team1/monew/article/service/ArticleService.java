package com.team1.monew.article.service;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService {

  ArticleViewDto recordView(Long articleId, Long userId);

  CursorPageResponse<ArticleDto> getArticles(
          String keyword,
          Long interestId,
          List<String> sourceIn,
          LocalDateTime publishDateFrom,
          LocalDateTime publishDateTo,
          String orderBy,
          String direction,
          String cursor,
          int limit,
          String after,
          Long userId
  );

  List<String> getSources();

  void restoreArticles(LocalDateTime from, LocalDateTime to);

  void deleteArticle(Long articleId);

  void hardDeleteArticle(Long articleId);

  void collectAndSaveNaverArticles(Interest interest, Keyword keyword);

  void collectAndSaveChosunArticles(Interest interest, Keyword keyword);
}
