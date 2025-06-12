package com.team1.monew.article.event.listener;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.entity.ArticleInterest;
import com.team1.monew.article.repository.ArticleInterestRepository;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.event.KeywordAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeywordAddedEventHandler {

    private final ArticleRepository articleRepository;
    private final ArticleInterestRepository articleInterestRepository;

    @EventListener
    @Transactional
    public void handleKeywordAddedEvent(KeywordAddedEvent event) {
        String keywordStr = event.getKeyword();
        Interest interest = event.getInterest();

        List<Article> articles = articleRepository.findByTitleContainingOrSummaryContaining(keywordStr, keywordStr);

        for (Article article : articles) {
            if (!articleInterestRepository.existsByArticleIdAndInterestId(article.getId(), interest.getId())) {
                articleInterestRepository.save(new ArticleInterest(interest, article));
                log.info("ArticleInterest 저장 완료 - articleId: {}, interestId: {}", article.getId(), interest.getId());
            } else {
                log.info("중복된 ArticleInterest 존재 - articleId: {}, interestId: {}", article.getId(), interest.getId());
            }
        }
    }
}