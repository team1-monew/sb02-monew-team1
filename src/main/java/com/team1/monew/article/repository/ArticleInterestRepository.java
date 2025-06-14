package com.team1.monew.article.repository;

import com.team1.monew.article.entity.ArticleInterest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleInterestRepository extends CrudRepository<ArticleInterest, Long> {

  void deleteByArticleId(Long articleId);

  boolean existsByArticleIdAndInterestId(Long articleId, Long interestId);

  @Modifying
  @Query("DELETE FROM ArticleInterest ai WHERE ai.article.id = :articleId AND ai.interest.id = :interestId")
  void deleteByArticleIdAndInterestId(@Param("articleId") Long articleId, @Param("interestId") Long interestId);

  @Query("SELECT ai.article.id FROM ArticleInterest ai JOIN ai.article a " +
          "WHERE ai.interest.id = :interestId AND " +
          "(a.title LIKE %:keyword% OR a.summary LIKE %:keyword%)")
  List<Long> findArticleIdsByInterestIdAndKeyword(@Param("interestId") Long id, @Param("keyword") String removedKeyword);
}