package com.team1.monew.comment.service.usecase;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentUpdateRequest;

public interface UpdateCommentUseCase {
    CommentDto update(CommentUpdateRequest request, Long CommentId, Long userId);
}
