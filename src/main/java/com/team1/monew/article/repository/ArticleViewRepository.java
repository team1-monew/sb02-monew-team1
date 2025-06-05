package com.team1.monew.article.repository;

import com.team1.monew.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleViewRepository extends JpaRepository<ArticleView, Long> {

  @Query("SELECT CASE WHEN COUNT(av) > 0 THEN TRUE ELSE FALSE END FROM ArticleView av WHERE av.article.id = :articleId AND av.viewedBy.id = :viewedById")
  boolean existsByArticleIdAndViewedById(Long articleId, Long viewedById);

  void deleteByArticleId(Long articleId);
}
