package com.team1.monew.article.repository;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

  boolean existsBySourceUrl(String link);

  @Query("SELECT a FROM Article a WHERE a.title LIKE %:titleKeyword% OR a.summary LIKE %:summaryKeyword%")
  List<Article> findByTitleContainingOrSummaryContaining(@Param("titleKeyword") String titleKeyword, @Param("summaryKeyword") String summaryKeyword);
}
