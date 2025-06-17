package com.team1.monew.subscription.controller.api;

import com.team1.monew.exception.ErrorResponse;
import com.team1.monew.subscription.dto.SubscriptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name="구독 관리", description = "관심사 구독 관리 관련 API")
public interface SubscriptionApi {

    @Operation(summary = "관심사 구독", description =
        "관심사를 구독합니다.", responses = {
        @ApiResponse(responseCode = "201", description = "구독 등록 성공"),
        @ApiResponse(responseCode = "404", description = "관심사 구독 실패 - 해당 관심사나 유저가 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "관심사 구독 실패 - 이미 구독한 관심사", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SubscriptionDto> create(
        @Parameter(description = "관심사 ID", required = true) @PathVariable("interestId") Long interestId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId);


    @Operation(summary = "관심사 구독 취소", description =
        "관심사 구독을 취소합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "구독 취소 성공"),
        @ApiResponse(responseCode = "404", description = "관심사 구독 실패 - 해당 관심사가 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(
        @Parameter(description = "관심사 ID", required = true) @PathVariable("interestId") Long interestId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Monew-Request-User-ID") Long userId);
}
