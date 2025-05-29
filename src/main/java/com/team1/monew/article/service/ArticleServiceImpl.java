package com.team1.monew.article.service;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.entity.Article;
import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.article.mapper.ArticleMapper;
import com.team1.monew.article.mapper.ArticleViewMapper;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.article.repository.ArticleViewRepository;
import com.team1.monew.comment.Repository.CommentRepository;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;

  @Override
  @Transactional
  public ArticleViewDto recordView(String articleId, String userId) {
    Article article = articleRepository.findById(Long.valueOf(articleId))
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    User user = userRepository.findById(Long.valueOf(userId))
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("userId", userId, "detail", "User not found")));

    ArticleView articleView = new ArticleView(article, user);
    articleViewRepository.save(articleView);

    article.increaseViewCount();
    articleRepository.save(article);

    Long commentCount = commentRepository.countByArticleId(article.getId());
    return ArticleViewMapper.toDto(articleView, commentCount);
  }

  @Override
  public List<ArticleDto> getArticles(
      String keyword,
      String interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      String requestUserId) {

    // TODO: 검색어, 관심사, 출처, 날짜 필터링 및 커서 기반 페이지네이션, 정렬 로직 적용
    List<Article> articles = articleRepository.findAll();

    return articles.stream()
        .filter(article -> !article.isDeleted())
        .map(article -> ArticleMapper.toDto(article, 0L, false))
        .collect(Collectors.toList());
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
  public void restoreArticles(Instant from, Instant to) {
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
  public void deleteArticle(String articleId) {
    Article article = articleRepository.findById(Long.valueOf(articleId))
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    article.markDeleted();
  }

  @Override
  @Transactional
  public void hardDeleteArticle(String articleId) {
    Article article = articleRepository.findById(Long.valueOf(articleId))
        .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND,
            Map.of("articleId", articleId, "detail", "Article not found")));

    articleRepository.delete(article);
  }
}
