package com.team1.monew.article.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorResponse;

@Tag(name = "뉴스 기사 관리", description = "뉴스 기사 관련 API")
public interface ArticleApi {

    @PostMapping("/{articleId}/article-views")
    @Operation(summary = "기사 조회 기록 추가", description = "기사의 조회 기록을 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공적으로 조회 기록을 추가했습니다."),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "기사 또는 사용자가 존재하지 않습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ArticleViewDto> recordArticleView(@PathVariable Long articleId, @RequestHeader("Monew-Request-User-ID") Long userId);

    @GetMapping
    @Operation(summary = "기사 목록 조회", description = "검색 조건에 맞는 기사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "기사 목록을 성공적으로 반환했습니다."),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "기사를 찾을 수 없습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CursorPageResponse<ArticleDto>> getArticles(@RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long interestId,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) String publishDateFrom,
        @RequestParam(required = false) String publishDateTo,
        @RequestParam(defaultValue = "publishDate") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String after,
        @RequestHeader(value = "Monew-Request-User-ID", required = false) Long userId);

    @GetMapping("/sources")
    @Operation(summary = "출처 목록 조회", description = "기사의 출처 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "출처 목록을 성공적으로 반환했습니다.")
    ResponseEntity<List<String>> getSources();

    @GetMapping("/restore")
    @Operation(summary = "기사 복구", description = "삭제된 기사를 복구합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "기사 복구가 완료되었습니다."),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> restoreArticles(@RequestParam(required = false) String from, @RequestParam(required = false) String to);

    @DeleteMapping("/{articleId}")
    @Operation(summary = "기사 논리 삭제", description = "기사를 논리적으로 삭제하였습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "기사 논리 삭제가 완료되었습니다."),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "기사를 찾을 수 없습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteArticle(@PathVariable Long articleId);

    @DeleteMapping("/{articleId}/hard")
    @Operation(summary = "기사 하드 삭제", description = "기사를 하드 삭제하였습니다. 데이터베이스에서 완전히 삭제됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "기사 하드 삭제가 완료되었습니다."),
        @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "기사를 찾을 수 없습니다.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> hardDeleteArticle(@PathVariable Long articleId);
}
