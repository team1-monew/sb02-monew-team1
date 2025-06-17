package com.team1.monew.interest.controller.api;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorResponse;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestUpdateRequest;
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

@Tag(name="관심사 관리", description = "관심사 관리 관련 API")
public interface InterestApi {
    @Operation(summary = "관심사 생성", description =
        "새로운 관심사를 등록합니다.", responses = {
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "409", description = "관심사 등록 실패 - 유사도 80% 이상", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<InterestDto> create(
        @RequestBody @Valid InterestRegisterRequest interestRegisterRequest,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "관심사 목록 조회", description = "조건에 맞는 관심사 목록을 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CursorPageResponse<InterestDto>> getInterests(
        @Parameter(description = "정렬 방향 (ASC, DESC)", required = false, example = "DESC") @RequestParam(name = "direction", defaultValue = "DESC") String direction,
        @Parameter(description = "페이지 크기", required = false, example = "10") @RequestParam(name = "limit", defaultValue = "10") int limit,
        @Parameter(description = "정렬 기준 (subscriberCount, createdAt 등)", required = false, example = "subscriberCount") @RequestParam(name = "orderBy", defaultValue = "subscriberCount") String orderBy,
        @Parameter(description = "키워드 필터", required = false, example = "여행") @RequestParam(name = "keyword", required = false) String keyword,
        @Parameter(description = "커서 값", required = false) @RequestParam(name = "cursor", required = false) String cursor,
        @Parameter(description = "보조 커서 (createdAt)", required = false) @RequestParam(name = "after", required = false) LocalDateTime after,
        @Parameter(description = "요청자 ID", required = true)  @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "관심사 수정", description = "기존 관심사의 이름 및 키워드를 수정합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 오류)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 관심사를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<InterestDto> update(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "관심사 수정 요청 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = InterestUpdateRequest.class))
        )
        @RequestBody @Valid InterestUpdateRequest interestUpdateRequest,
        @Parameter(description = "관심사 ID", required = true) @PathVariable("interestId") Long interestId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "관심사 삭제", description = "해당 ID의 관심사를 삭제합니다.", responses = {
        @ApiResponse(responseCode = "204", description = "삭제 성공 (내용 없음)"),
        @ApiResponse(responseCode = "404", description = "해당 관심사를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(
        @Parameter(description = "관심사 ID", required = true)
        @PathVariable("interestId") Long interestId,

        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Monew-Request-User-ID") Long userId
    );




}
