package com.team1.monew.useractivity.controller.api;

import com.team1.monew.exception.ErrorResponse;
import com.team1.monew.useractivity.dto.UserActivityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name="사용자 활동 내역 관리", description = "사용자 활동 내역 관리 API")
public interface UserActivityApi {


    @Operation(summary = "사용자 활동 내역 조회", description =
        "사용자 ID로 활동 내역을 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "사용자 활동 내역 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserActivityDto> findUserActivity(@Parameter(description = "사용자 ID", required = true) @PathVariable Long userId);

}
