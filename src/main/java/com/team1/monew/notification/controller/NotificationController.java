package com.team1.monew.notification.controller;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.notification.service.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
      @RequestParam(required = false, defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) LocalDateTime after,
      @RequestParam(defaultValue = "20") int limit,
      @RequestHeader(value = "Monew-Request-User-ID") Long userId
  ) {
    log.info("알림 목록 조회 요청 - userId: {}, direction: {}, cursor: {}, after: {}, limit: {}",
        userId, direction, cursor, after, limit);

    NotificationCursorRequest request = NotificationCursorRequest.builder()
        .direction(direction)
        .cursor(cursor)
        .after(after)
        .limit(limit)
        .userId(userId)
        .build();

    CursorPageResponse<NotificationDto> response = notificationService.getAllNotifications(request);

    return ResponseEntity.ok(response);
  }
}
