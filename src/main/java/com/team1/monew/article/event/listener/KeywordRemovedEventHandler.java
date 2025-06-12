package com.team1.monew.article.event.listener;

import com.team1.monew.article.repository.ArticleInterestRepository;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.event.KeywordRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeywordRemovedEventHandler {

    private final ArticleInterestRepository articleInterestRepository;

    @EventListener
    @Transactional
    public void handleKeywordRemovedEvent(KeywordRemovedEvent event) {
        Interest interest = event.getInterest();
        String removedKeyword = event.getKeyword();

        List<Long> articleIds = articleInterestRepository.findArticleIdsByInterestIdAndKeyword(interest.getId(), removedKeyword);

        for (Long articleId : articleIds) {
            articleInterestRepository.deleteByArticleIdAndInterestId(articleId, interest.getId());
            log.info("ArticleInterest 삭제 완료 - articleId: {}, interestId: {}", articleId, interest.getId());
        }
    }
}
