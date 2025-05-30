package com.team1.monew.comment.service.usecase;

public interface DeleteCommentUseCase {
    void softDelete(Long commentId, Long userId);
}
