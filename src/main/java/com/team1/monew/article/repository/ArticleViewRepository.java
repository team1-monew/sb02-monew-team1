package com.team1.monew.article.repository;

import com.team1.monew.article.entity.ArticleView;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleViewRepository extends JpaRepository<ArticleView, Long> {

  @Query("SELECT CASE WHEN COUNT(av) > 0 THEN TRUE ELSE FALSE END FROM ArticleView av WHERE av.article.id = :articleId AND av.viewedBy.id = :viewedById")
  boolean existsByArticleIdAndViewedById(Long articleId, Long viewedById);

  void deleteByArticleId(Long articleId);


  @Query("SELECT av FROM ArticleView av "
      + "LEFT JOIN FETCH av.article "
      + "WHERE av.viewedBy.id = :userId "
      + "ORDER BY av.createdAt DESC")
  List<ArticleView> findTop10ArticleViewByUserId(Long userId, Pageable pageable);

  // todo: isDeleted, createdAt 관련 인덱스 필요 - 추후 생성 후 성능 측정
  // articleView 테이블 - viewedBy + createdAt DESC 인덱스
  // article 테이블 - isDeleted 인덱스
  @Query("SELECT av FROM ArticleView av "
      + "LEFT JOIN FETCH av.article a "
      + "WHERE av.viewedBy.id = :userId "
      + "AND a.isDeleted = false "
      + "ORDER BY av.createdAt DESC")
  List<ArticleView> findValidArticleViewsByUserIdOrderByCreatedAt(Long userId);
}
