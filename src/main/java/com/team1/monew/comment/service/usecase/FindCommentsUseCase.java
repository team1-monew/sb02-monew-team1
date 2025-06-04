package com.team1.monew.comment.service.usecase;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.common.dto.CursorPageResponse;

public interface FindCommentsUseCase {
    CursorPageResponse<CommentDto> findCommentsByArticleId(CommentSearchCondition condition);
}
