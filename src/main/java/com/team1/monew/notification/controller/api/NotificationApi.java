package com.team1.monew.notification.controller.api;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorResponse;
import com.team1.monew.notification.dto.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name="알림 관리", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회", description = "알림 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/notifications")
    ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        @Parameter(name = "direction", description = "정렬 방향", in = ParameterIn.QUERY)
        @RequestParam(required = false) String direction,

        @Parameter(name = "cursor", description = "커서 값", in = ParameterIn.QUERY)
        @RequestParam(required = false) String cursor,

        @Parameter(name = "after", description = "보조 커서(createdAt) 값", in = ParameterIn.QUERY)
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String after,

        @Parameter(name = "limit", description = "커서 페이지 크기", required = true, example = "50", in = ParameterIn.QUERY)
        @RequestParam int limit,

        @Parameter(name = "Monew-Request-User-ID", description = "요청자 ID", required = true, in = ParameterIn.HEADER)
        @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "전체 알림 확인", description = "전체 알림을 한번에 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "전체 알림 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/api/notifications")
    ResponseEntity<Void> markAllNotificationsAsRead(
        @Parameter(name = "Monew-Request-User-ID", description = "요청자 ID", required = true, in = ParameterIn.HEADER)
        @RequestHeader("Monew-Request-User-ID") Long userId
    );

    @Operation(summary = "알림 확인", description = "지정한 알림을 확인 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/api/notifications/{notificationId}")
    ResponseEntity<Void> markNotificationsAsRead(
        @Parameter(name = "notificationId", description = "알림 ID", required = true, in = ParameterIn.PATH)
        @PathVariable Long notificationId,

        @Parameter(name = "Monew-Request-User-ID", description = "요청자 ID", required = true, in = ParameterIn.HEADER)
        @RequestHeader("Monew-Request-User-ID") Long userId
    );
}