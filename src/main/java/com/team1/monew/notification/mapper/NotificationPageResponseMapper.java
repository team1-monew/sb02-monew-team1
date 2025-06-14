package com.team1.monew.notification.mapper;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationPageResponseMapper {
  public CursorPageResponse<NotificationDto> toPageResponse(
      List<NotificationDto> content,
      NotificationCursorRequest request,
      long totalElements,
      boolean hasNext
  ) {
    if (content == null || content.isEmpty()) {
      return new CursorPageResponse<>(
          content,
          null,
          null,
          (long) request.limit(),
          totalElements,
          hasNext
      );
    }

    NotificationDto lastNotificationDto = content.get(content.size() - 1);

    String nextCursor = lastNotificationDto.createdAt().toString();

    LocalDateTime nextAfter = lastNotificationDto.createdAt();

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextAfter,
        (long) request.limit(),
        totalElements,
        hasNext
    );
  }
}
