package com.team1.monew.comment.service;

import com.team1.monew.comment.service.usecase.DeleteCommentUseCase;
import com.team1.monew.comment.service.usecase.FindCommentsUseCase;
import com.team1.monew.comment.service.usecase.RegisterCommentUseCase;
import com.team1.monew.comment.service.usecase.UpdateCommentUseCase;

public interface CommentService extends RegisterCommentUseCase,
                                        UpdateCommentUseCase,
                                        DeleteCommentUseCase,
                                        FindCommentsUseCase
{

}
