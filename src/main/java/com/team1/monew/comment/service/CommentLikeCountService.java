package com.team1.monew.comment.service;

public interface CommentLikeCountService {

    void updateLikeCountByDeletedUser(Long userId);

    Long updateLikeCountByCommentId(Long commentId);

}
