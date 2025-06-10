package com.team1.monew.notification.repository;

import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import org.springframework.data.domain.Slice;

public interface NotificationRepositoryCustom {
  Slice<NotificationDto> getAllByCursorRequest(NotificationCursorRequest request);
}
