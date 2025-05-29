package com.team1.monew.article.dto;

import java.time.Instant;
import java.util.List;

public record ArticleRestoreResultDto(
    Instant restoreDate,
    List<String> restoredArticleIds,
    long restoredArticleCount
) {

}