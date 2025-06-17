package com.team1.monew.notification.controller;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "20") int limit,
      @RequestHeader(value = "Monew-Request-User-ID") Long userId
  ) {
    log.info("알림 목록 조회 요청 - userId: {}, direction: {}, cursor: {}, after: {}, limit: {}",
        userId, direction, cursor, after, limit);

    NotificationCursorRequest request = NotificationCursorRequest.builder()
        .direction(direction)
        .cursor(parseToLocalDateTime(cursor) != null ? parseToLocalDateTime(cursor).toString() : null)
        .after(parseToLocalDateTime(after))
        .limit(limit)
        .userId(userId)
        .build();

    CursorPageResponse<NotificationDto> response = notificationService.getAllNotifications(request);

    return ResponseEntity.ok(response);
  }

  @PatchMapping
  public ResponseEntity<Void> markAllNotificationsAsRead(
      @RequestHeader(value = "Monew-Request-User-ID") Long userId
  ) {
    log.info("알림 목록 전체 확인 요청 - userId: {}", userId);

    notificationService.confirmAll(userId);

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<Void> markNotificationsAsRead(
      @PathVariable Long notificationId,
      @RequestHeader(value = "Monew-Request-User-ID") Long userId
  ) {
    log.info("알림 목록 확인 요청 - userId: {}", userId);

    notificationService.confirm(notificationId);

    return ResponseEntity.ok().build();
  }

  private LocalDateTime parseToLocalDateTime(String datetimeString) {
    if (datetimeString == null || datetimeString.isBlank()) return null;

    try {
      // 우선 OffsetDateTime으로 시도 (시간대 정보 포함)
      return OffsetDateTime.parse(datetimeString).toLocalDateTime();
    } catch (DateTimeParseException e1) {
      try {
        // 시간대 정보가 없는 경우 (LocalDateTime)
        return LocalDateTime.parse(datetimeString);
      } catch (DateTimeParseException e2) {
        return null;
      }
    }
  }
}