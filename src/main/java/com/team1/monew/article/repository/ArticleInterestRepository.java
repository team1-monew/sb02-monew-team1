package com.team1.monew.article.repository;

import com.team1.monew.article.entity.ArticleInterest;
import org.springframework.data.repository.CrudRepository;

public interface ArticleInterestRepository extends CrudRepository<ArticleInterest, Long> {

  void deleteByArticleId(Long articleId);
}