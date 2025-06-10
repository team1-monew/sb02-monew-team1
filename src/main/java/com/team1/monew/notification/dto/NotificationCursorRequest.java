package com.team1.monew.notification.dto;

import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;

public record NotificationCursorRequest(
    Sort.Direction direction,
    String cursor,
    LocalDateTime after,
    int limit,
    Long userId
) {
  public static class Builder {
    private Sort.Direction direction;
    private String cursor;
    private LocalDateTime after;
    private int limit;
    private Long userId;

    public Builder direction(String direction) {
      this.direction = Sort.Direction.valueOf(direction.toUpperCase());
      return this;
    }

    public Builder cursor(String cursor) {
      this.cursor = cursor;
      return this;
    }

    public Builder after(LocalDateTime after) {
      this.after = after;
      return this;
    }

    public Builder limit(int limit) {
      this.limit = limit;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public NotificationCursorRequest build() {
      return new NotificationCursorRequest(direction, cursor, after, limit, userId);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
