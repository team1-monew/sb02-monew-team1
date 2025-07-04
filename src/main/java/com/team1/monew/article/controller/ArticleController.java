package com.team1.monew.article.controller;

import com.team1.monew.article.controller.api.ArticleApi;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.service.ArticleService;
import com.team1.monew.common.dto.CursorPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController implements ArticleApi {

  private final ArticleService articleService;

  @Override
  @PostMapping("/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> recordArticleView(
      @PathVariable Long articleId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {

    log.info("📝 기사 조회 기록 요청 : articleId = {}, userId = {}", articleId, userId);

    ArticleViewDto articleViewDto = articleService.recordView(articleId, userId);

    log.info("📝 기사 조회 기록 요청 완료: articleId = {}, userId = {}", articleId, userId);

    return ResponseEntity.ok(articleViewDto);
  }

  @Override
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

    LocalDateTime fromDate = parseStartDate(publishDateFrom);
    LocalDateTime toDate = parseEndDate(publishDateTo);

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

  @Override
  @GetMapping("/sources")
  public ResponseEntity<List<String>> getSources() {
    log.info("📝 출처 목록 조회 요청");

    List<String> sources = articleService.getSources();

    log.info("📝 출처 목록 조회 요청 완료: sources count = {}", sources.size());

    return ResponseEntity.ok(sources);
  }

  @Override
  @GetMapping("/restore")
  public ResponseEntity<Void> restoreArticles(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {

    log.info("🛠️ 기사 복구 요청 시작: from = {}, to = {}", from, to);

    LocalDateTime fromDate = parseStartDate(from);
    LocalDateTime toDate = parseEndDate(to);

    log.info("🛠️ 기사 복구 요청 완료: from = {}, to = {}", fromDate, toDate);

    articleService.restoreArticles(fromDate, toDate);

    return ResponseEntity.noContent().build();
  }

  @Override
  @DeleteMapping("/{articleId}")
  public ResponseEntity<Void> deleteArticle(
      @PathVariable Long articleId) {

    log.info("📝 기사 논리 삭제 요청: articleId = {}", articleId);

    articleService.deleteArticle(articleId);

    log.info("📝 기사 논리 삭제 요청 완료: articleId = {}", articleId);

    return ResponseEntity.noContent().build();
  }

  @Override
  @DeleteMapping("/{articleId}/hard")
  public ResponseEntity<Void> hardDeleteArticle(
      @PathVariable Long articleId) {

    log.info("📝 기사 하드 삭제 요청: articleId = {}", articleId);

    articleService.hardDeleteArticle(articleId);

    log.info("📝 기사 하드 삭제 요청 완료: articleId = {}", articleId);

    return ResponseEntity.noContent().build();
  }

  private LocalDateTime parseStartDate(String rawDate) {
    if (rawDate == null || rawDate.isBlank()) return null;

    try {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
      LocalDateTime date = LocalDateTime.parse(rawDate, isoFormatter);
      return date.toLocalDate().atStartOfDay();
    } catch (DateTimeParseException e) {
      try {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(rawDate, dateFormatter);
        return date.atStartOfDay();
      } catch (DateTimeParseException e2) {
        log.warn("⚠️ 날짜 파싱 실패: 입력값 = '{}', 예외 메시지 = {}", rawDate, e2.getMessage());
        return null;
      }
    }
  }

  private LocalDateTime parseEndDate(String rawDate) {
    if (rawDate == null || rawDate.isBlank()) return null;

    try {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
      LocalDateTime date = LocalDateTime.parse(rawDate, isoFormatter);
      return date.toLocalDate().atTime(23, 59, 59);
    } catch (DateTimeParseException e) {
      try {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(rawDate, dateFormatter);
        return date.atTime(23, 59, 59);
      } catch (DateTimeParseException e2) {
        log.warn("⚠️ 날짜 파싱 실패: 입력값 = '{}', 예외 메시지 = {}", rawDate, e2.getMessage());
        return null;
      }
    }
  }
}
