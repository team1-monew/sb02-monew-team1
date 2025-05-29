package com.team1.monew.comment.repository;

import com.team1.monew.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<Comment, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

}
