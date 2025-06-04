package com.team1.monew.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import org.springframework.data.domain.Sort;

@Builder
public record CommentSearchCondition(
    Long articleId,
    CommentOrderBy orderBy,
    Sort.Direction direction,
    String cursor,
    LocalDateTime after,
    int limit,
    Long userId
) {
    public static CommentSearchCondition fromParams(
        Long articleId,
        String orderBy,
        String direction,
        String cursor,
        LocalDateTime after,
        int limit,
        Long userId
    ) {
        return new CommentSearchCondition(
            articleId,
            CommentOrderBy.from(orderBy),
            parseSortDirection(direction),
            cursor,
            after,
            limit,
            userId
        );
    }

    private static Sort.Direction parseSortDirection(String direction) {
        return Sort.Direction.valueOf(direction.toUpperCase());
    }
}
