package com.team1.monew.article.controller;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.service.ArticleService;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> recordArticleView(
          @PathVariable Long articleId,
          @RequestHeader("Monew-Request-User-ID") Long userId) {

    log.info("📝 기사 조회 기록 요청 : articleId = {}, userId = {}", articleId, userId);

    ArticleViewDto articleViewDto = articleService.recordView(articleId, userId);

    log.info("📝 기사 조회 기록 요청 완료: articleId = {}, userId = {}", articleId, userId);

    return ResponseEntity.ok(articleViewDto);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<ArticleDto>> getArticles(
          @RequestParam(required = false) String keyword,
          @RequestParam(required = false) Long interestId,
          @RequestParam(required = false) List<String> sourceIn,
          @RequestParam(required = false) String publishDateFrom,
          @RequestParam(required = false) String publishDateTo,
          @RequestParam(defaultValue = "publishDate") String orderBy,
          @RequestParam(defaultValue = "DESC") String direction,
          @RequestParam(required = false) String cursor,
          @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
          @RequestParam(required = false) String after,
          @RequestHeader(value = "Monew-Request-User-ID", required = false) Long userId
  ) {

    log.info("📝 기사 목록 조회 요청 시작: keyword={}, interestId={}, sourceIn={}, publishDateFrom={}, publishDateTo={}, orderBy={}, direction={}, cursor={}, limit={}, after={}, userId={}",
            keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, limit, after, userId);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    LocalDate fromDate = parseDateSafely(publishDateFrom, formatter);
    LocalDate toDate = parseDateSafely(publishDateTo, formatter);

    CursorPageResponse<ArticleDto> response = articleService.getArticles(
            keyword,
            interestId,
            sourceIn,
            fromDate,
            toDate,
            orderBy,
            direction,
            cursor,
            limit,
            after,
            userId
    );

    log.info("📝 기사 목록 조회 요청 완료: 반환된 기사 수 = {}", response.content().size());

    return ResponseEntity.ok(response);
  }

  private LocalDate parseDateSafely(String rawDate, DateTimeFormatter formatter) {
    if (rawDate == null || rawDate.isBlank()) return null;
    try {
      return LocalDateTime.parse(rawDate, formatter).toLocalDate();
    } catch (DateTimeParseException e) {
      log.warn("⚠️ 날짜 파싱 실패: 입력값 = '{}', 예외 메시지 = {}", rawDate, e.getMessage());
      return null;
    }
  }

  @GetMapping("/sources")
  public ResponseEntity<List<String>> getSources() {
    log.info("📝 출처 목록 조회 요청");

    List<String> sources = articleService.getSources();

    log.info("📝 출처 목록 조회 요청 완료: sources count = {}", sources.size());

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
