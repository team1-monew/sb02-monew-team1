package com.team1.monew.article.repository;

import com.team1.monew.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ArticleViewRepository extends JpaRepository<ArticleView, Long> {

  void deleteByArticleId(Long articleId);
}
