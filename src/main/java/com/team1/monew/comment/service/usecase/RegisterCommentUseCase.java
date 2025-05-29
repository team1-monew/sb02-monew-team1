package com.team1.monew.comment.service.usecase;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;

public interface RegisterCommentUseCase {
    CommentDto register(CommentRegisterRequest request);
}
