package com.team1.monew.article.service;

import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.article.collector.NewsCollector;
import com.team1.monew.article.collector.ChosunNewsCollector;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.entity.*;
import com.team1.monew.article.event.NewArticlesCollectedEvent;
import com.team1.monew.article.mapper.ArticleViewMapper;
import com.team1.monew.article.repository.ArticleInterestRepository;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.article.repository.ArticleRepositoryCustom;
import com.team1.monew.article.repository.ArticleViewRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleRepositoryCustom articleRepositoryCustom;
  private final ArticleViewRepository articleViewRepository;
  private final ArticleInterestRepository articleInterestRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final NewsCollector naverNewsCollector;
  private final ChosunNewsCollector chosunNewsCollector;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void collectAndSaveNaverArticles(Interest interest, Keyword keyword) {
    log.info("📝 네이버 기사 수집 시작: 관심사 = {}, 키워드 = {}", interest.getName(), keyword.getKeyword());

    List<CollectedArticleDto> collectedArticles = naverNewsCollector.collect(interest, keyword);

    log.info("📝 네이버 기사 수집 완료: 수집된 기사 수 = {}", collectedArticles.size());

    saveArticles(collectedArticles, interest);

    log.info("📝 네이버 기사 저장 완료: 관심사 = {}, 키워드 = {}", interest.getName(), keyword.getKeyword());

    eventPublisher.publishEvent(new NewArticlesCollectedEvent(interest, collectedArticles));
  }

  @Transactional
  public void collectAndSaveChosunArticles(Interest interest, Keyword keyword) {
    log.info("📝 조선일보 기사 수집 시작: 관심사 = {}, 키워드 = {}", interest.getName(), keyword.getKeyword());

    List<CollectedArticleDto> collectedArticles = chosunNewsCollector.collect(interest, keyword);

    String kw = keyword.getKeyword().toLowerCase();

    List<CollectedArticleDto> filtered = collectedArticles.stream()
        .filter(dto -> dto.title().toLowerCase().contains(kw)
            || dto.summary().toLowerCase().contains(kw))
        .toList();

    log.info("📝 조선일보 기사 필터링 완료: 필터된 기사 수 = {}", filtered.size());

    saveArticles(filtered, interest);

    log.info("📝 조선일보 기사 저장 완료: 관심사 = {}, 키워드 = {}", interest.getName(), keyword.getKeyword());

    eventPublisher.publishEvent(new NewArticlesCollectedEvent(interest, collectedArticles));
  }

  private void saveArticles(List<CollectedArticleDto> collectedArticles, Interest interest) {
    log.info("📝 기사 저장 시작: 총 기사 수 = {}", collectedArticles.size());

    for (CollectedArticleDto dto : collectedArticles) {
      if (articleRepository.existsBySourceUrl(dto.sourceUrl())) {
        log.warn("⚠️ 이미 저장된 기사: {}", dto.sourceUrl());
        continue;
      }

      log.info("📝 저장 중: 기사 제목 = {}, 발행일 = {}", dto.title(), dto.publishDate());

      Article article = Article.builder()
          .title(dto.title())
          .summary(dto.summary())
          .source(dto.source())
          .sourceUrl(dto.sourceUrl())
          .publishDate(dto.publishDate())
          .viewCount(0L)
          .createdAt(LocalDateTime.now())
          .build();

      ArticleInterest relation = new ArticleInterest(interest, article);
      article.addArticleInterest(relation);

      articleRepository.save(article);

      log.info("📝 기사 저장 완료: {}", dto.title());
    }

    log.info("📝 기사 저장 완료: 총 저장된 기사 수 = {}", collectedArticles.size());
  }

  @Override
  @Transactional
  public ArticleViewDto recordView(Long articleId, Long userId) {
    log.info("📝 기사 조회 시작 : articleId = {}, userId = {}", articleId, userId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("userId", userId, "detail", "User not found")));

    boolean viewExists = articleViewRepository.existsByArticleIdAndViewedById(articleId, userId);

    ArticleView articleView = null;
    if (!viewExists) {
      articleView = new ArticleView(article, user);
      articleViewRepository.save(articleView);

      article.increaseViewCount();
      articleRepository.save(article);
    } else {
      articleView = new ArticleView(article, user);
      articleViewRepository.save(articleView);
    }

    Long commentCount = commentRepository.countByArticleIdAndIsDeletedFalse(article.getId());

    log.info("📝 기사 조회 완료: articleId = {}, userId = {}", articleId, userId);
    return ArticleViewMapper.toDto(articleView, commentCount);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<ArticleDto> getArticles(
          String keyword,
          Long interestId,
          List<String> sourceIn,
          LocalDate publishDateFrom,
          LocalDate publishDateTo,
          String orderBy,
          String direction,
          String cursor,
          int limit,
          String after,
          Long userId
  ) {
    return articleRepositoryCustom.searchArticles(
            keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
            orderBy, direction, cursor, limit, after, userId
    );
  }

  @Override
  public List<String> getSources() {
    return articleRepository.findAll().stream()
        .map(Article::getSource)
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void restoreArticles(LocalDateTime from, LocalDateTime to) {
    List<Article> deletedArticles = articleRepository.findAll().stream()
        .filter(Article::isDeleted)
        .filter(a -> !a.getCreatedAt().isBefore(from) && !a.getCreatedAt().isAfter(to))
        .toList();

    if (deletedArticles.isEmpty()) {
      throw new RestException(ErrorCode.NOT_FOUND,
          Map.of("from", from, "to", to, "detail", "No deleted articles found"));
    }

    for (Article article : deletedArticles) {
      article.restore();
    }
  }

  @Override
  @Transactional
  public void deleteArticle(Long articleId) {
    log.info("📝 기사 삭제 시작: articleId = {}", articleId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    commentRepository.findByArticleId(articleId)
        .forEach(Comment::delete);

    article.markDeleted();
    log.info("📝 기사 삭제 처리 완료: {}", articleId);
  }

  @Override
  @Transactional
  public void hardDeleteArticle(Long articleId) {
    log.info("📝 기사 물리 삭제 시작: articleId = {}", articleId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    commentRepository.deleteByArticleId(articleId);
    articleViewRepository.deleteByArticleId(articleId);
    articleInterestRepository.deleteByArticleId(articleId);

    articleRepository.delete(article);
    log.info("📝 기사 물리 삭제 완료: articleId = {}", articleId);
  }
}
