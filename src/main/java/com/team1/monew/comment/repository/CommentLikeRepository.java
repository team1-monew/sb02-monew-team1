package com.team1.monew.comment.repository;

import com.team1.monew.comment.entity.CommentLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Long countByCommentId(Long commentId);

    Optional<CommentLike> findByComment_IdAndLikedBy_Id(Long commentId, Long userId);

    boolean existsByComment_IdAndLikedBy_Id(Long commentId, Long userId);

    List<CommentLike> findAllByLikedById(Long userId);

    @Query("""
        SELECT cl
        FROM CommentLike cl
        JOIN FETCH cl.comment c
        JOIN FETCH c.article
        WHERE cl.likedBy.id = :userId
        ORDER BY cl.createdAt DESC
        """)
    List<CommentLike> findWithCommentByLikedById(@Param("userId") Long userId, Pageable pageable);


}
