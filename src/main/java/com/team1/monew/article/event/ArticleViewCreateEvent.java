package com.team1.monew.article.event;

import com.team1.monew.article.dto.ArticleViewDto;
import lombok.Builder;

@Builder
public record ArticleViewCreateEvent(
    Long userId,
    ArticleViewDto articleViewDto
) {

}
