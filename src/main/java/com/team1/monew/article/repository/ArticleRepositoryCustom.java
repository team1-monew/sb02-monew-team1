package com.team1.monew.article.repository;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.common.dto.CursorPageResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepositoryCustom {
    CursorPageResponse<ArticleDto> searchArticles(
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

    List<ArticleDto> findAllCreatedYesterday();
}