package com.team1.monew.comment.controller.api;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentLikeDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.dto.CommentUpdateRequest;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name="댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

    @Operation(summary = "댓글 목록 조회", description =
        "조건에 맞는 댓글 목록을 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CursorPageResponse<CommentDto>> findCommentsByArticleId(
        @Parameter(description = "기사 ID", required = true) @RequestParam Long articleId,
        @Parameter(description = "정렬 기준 (createdAt, likeCount 등)", required = true) @RequestParam String orderBy,
        @Parameter(description = "정렬 방향 (ASC, DESC)", required = true) @RequestParam String direction,
        @Parameter(description = "커서 값", required = false) @RequestParam(required = false) String cursor,
        @Parameter(description = "보조 커서(createdAt) 값", required = false) @RequestParam(required = false) LocalDateTime after,
        @Parameter(description = "커서 페이지 크기", required = false) @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "댓글 등록", description =
        "새로운 댓글을 등록합니다.", responses = {
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommentDto> create(@RequestBody @Valid CommentRegisterRequest request);

    @Operation(summary = "댓글 좋아요", description =
        "댓글 좋아요를 등록합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "댓글 좋아요 성공"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommentLikeDto> like(
        @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "댓글 좋아요 취소", description =
        "댓글 좋아요를 취소합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "댓글 좋아요 취소 성공"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> unlike(
        @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "댓글 논리 삭제", description =
        "댓글을 논리적으로 삭제합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> softDelete(
        @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "댓글 정보 수정", description =
        "댓글의 내용을 수정합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CommentDto> update(
        @RequestBody @Valid CommentUpdateRequest request,
        @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "댓글 물리 삭제", description =
        "댓글을 물리적으로 삭제합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> hardDelete(@Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId);
}
