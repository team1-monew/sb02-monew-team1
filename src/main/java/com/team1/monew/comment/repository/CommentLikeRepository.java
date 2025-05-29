package com.team1.monew.comment.repository;

import com.team1.monew.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByComment_IdAndLikedBy_Id(Long commentId, Long userId);

}
