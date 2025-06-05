package com.team1.monew.comment.repository;

import com.team1.monew.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Long countByArticleIdAndIsDeletedFalse(Long articleId);

    List<Comment> findByArticleId(Long articleId);

    void deleteByArticleId(Long articleId);

    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.article
        WHERE c.user.id = :userId
          AND c.isDeleted = false
        ORDER BY c.createdAt DESC
    """)
    List<Comment> findTop10ByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
}
