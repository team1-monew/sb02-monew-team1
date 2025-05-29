package com.team1.monew.notification.dto;

import java.time.Instant;

public record NotificationDto(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    boolean confirmed,
    Long userId,
    String content,
    String resourceType,
    Long resourceId
) {

}