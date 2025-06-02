package com.team1.monew.article.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleRestoreResultDto(
    LocalDateTime restoreDate,
    List<String> restoredArticleIds,
    long restoredArticleCount
) {

}