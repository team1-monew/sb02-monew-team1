package com.team1.monew.article.event;

import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.interest.entity.Interest;
import java.util.List;

public class NewArticlesCollectedEvent {
  private final Interest interest;
  private final List<CollectedArticleDto> articles;

  public NewArticlesCollectedEvent(Interest interest, List<CollectedArticleDto> articles) {
    this.interest = interest;
    this.articles = articles;
  }

  public Interest getInterest() { return interest; }
  public List<CollectedArticleDto> getArticles() { return articles; }
}