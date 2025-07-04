package com.team1.monew.article.scheduler;

import com.team1.monew.article.service.ArticleService;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.repository.InterestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NewsCollectorScheduler {

  private final InterestRepository interestRepository;
  private final ArticleService articleService;

  @Scheduled(cron = "0 0 0 * * *")
  public void collectNaverNews() {
    List<Interest> interests = interestRepository.findAllWithKeywords();

    for (Interest interest : interests) {
      for (Keyword keyword : interest.getKeywords()) {
        articleService.collectAndSaveNaverArticles(interest, keyword);
      }
    }
  }

  @Scheduled(cron = "0 10 0 * * *")
  public void collectChosunNews() {
    List<Interest> interests = interestRepository.findAllWithKeywords();

    for (Interest interest : interests) {
      for (Keyword keyword : interest.getKeywords()) {
        articleService.collectAndSaveChosunArticles(interest, keyword);
      }
    }
  }
}
