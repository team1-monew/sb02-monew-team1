package com.team1.monew.article.service;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import java.time.Instant;
import java.util.List;

public interface ArticleService {

  ArticleViewDto recordView(String articleId, String userId);

  List<ArticleDto> getArticles(
      String keyword,
      String interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      String requestUserId
  );

  List<String> getSources();

  void restoreArticles(Instant from, Instant to);

  void deleteArticle(String articleId);

  void hardDeleteArticle(String articleId);
}
