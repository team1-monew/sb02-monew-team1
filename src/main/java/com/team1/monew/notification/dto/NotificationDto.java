package com.team1.monew.notification.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean confirmed,
    Long userId,
    String content,
    String resourceType,
    Long resourceId
) {

}