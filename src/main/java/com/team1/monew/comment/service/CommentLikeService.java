package com.team1.monew.comment.service;

import com.team1.monew.comment.dto.CommentLikeDto;

public interface CommentLikeService {

    CommentLikeDto like(Long commentId, Long userId);

    void unlike(Long commentId, Long userId);

}
