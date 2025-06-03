package com.team1.monew.comment.mapper;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentOrderBy;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.common.dto.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {
    public CursorPageResponse<CommentDto> toPageResponse(
        List<CommentDto> content,
        CommentSearchCondition condition,
        boolean hasNext
    ) {
        CommentDto last = content.isEmpty() ? null : content.get(content.size() - 1);

        String nextCursor = null;
        if (last != null) {
            nextCursor = condition.orderBy().equals(CommentOrderBy.CREATED_AT)
                ? last.createdAt().toString()
                : last.likeCount().toString();
        }

        LocalDateTime nextAfter = last != null ? last.createdAt() : null;

        return new CursorPageResponse<>(
            content,
            nextCursor,
            nextAfter,
            (long) content.size(),
            null,
            hasNext
        );
    }
}
