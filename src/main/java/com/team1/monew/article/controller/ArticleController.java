package com.team1.monew.article.controller;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> recordArticleView(
          @PathVariable Long articleId,
          @RequestParam Long userId) {

    log.info("📝 기사 조회 기록 요청 : articleId = {}, userId = {}", articleId, userId);

    ArticleViewDto articleViewDto = articleService.recordView(articleId, userId);

    log.info("📝 기사 조회 기록 요청 완료: articleId = {}, userId = {}", articleId, userId);

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
      @PathVariable Long articleId) {

    log.info("📝 기사 삭제 요청: articleId = {}", articleId);

    articleService.deleteArticle(articleId);

    log.info("📝 기사 삭제 요청 완료: articleId = {}", articleId);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{articleId}/hard")
  public ResponseEntity<Void> hardDeleteArticle(
      @PathVariable Long articleId) {

    log.info("📝 기사 물리 삭제 요청: articleId = {}", articleId);

    articleService.hardDeleteArticle(articleId);

    log.info("📝 기사 물리 삭제 요청 완료: articleId = {}", articleId);

    return ResponseEntity.noContent().build();
  }
}
