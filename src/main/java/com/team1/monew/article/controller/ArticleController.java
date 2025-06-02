package com.team1.monew.article.controller;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> recordArticleView(
      @PathVariable String articleId,
      @RequestParam String userId) {

    ArticleViewDto articleViewDto = articleService.recordView(articleId, userId);

    return ResponseEntity.ok(articleViewDto);
  }

  @GetMapping
  public ResponseEntity<List<ArticleDto>> getArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String interestId,
      @RequestParam(required = false) List<String> sourceIn,
      @RequestParam(required = false) LocalDateTime publishDateFrom,
      @RequestParam(required = false) LocalDateTime publishDateTo,
      @RequestParam(required = false, defaultValue = "publishDate") String orderBy,
      @RequestParam(required = false, defaultValue = "desc") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) LocalDateTime after,
      @RequestParam(required = false, defaultValue = "20") int limit,
      @RequestParam(required = false) String requestUserId) {

    List<ArticleDto> articles = articleService.getArticles(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId);

    return ResponseEntity.ok(articles);
  }

  @GetMapping("/sources")
  public ResponseEntity<List<String>> getSources() {
    List<String> sources = articleService.getSources();

    return ResponseEntity.ok(sources);
  }

  @GetMapping("/restore")
  public ResponseEntity<Void> restoreArticles(
      @RequestParam LocalDateTime from,
      @RequestParam LocalDateTime to) {

    articleService.restoreArticles(from, to);

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{articleId}")
  public ResponseEntity<Void> deleteArticle(
      @PathVariable String articleId) {

    articleService.deleteArticle(articleId);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{articleId}/hard")
  public ResponseEntity<Void> hardDeleteArticle(
      @PathVariable String articleId) {

    articleService.hardDeleteArticle(articleId);

    return ResponseEntity.noContent().build();
  }
}
