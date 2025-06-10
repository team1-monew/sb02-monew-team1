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
  public static class Builder {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean confirmed;
    private Long userId;
    private String content;
    private String resourceType;
    private Long resourceId;


    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public Builder confirmed(boolean confirmed) {
      this.confirmed = confirmed;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder resourceType(String resourceType) {
      this.resourceType = resourceType;
      return this;
    }

    public Builder resourceId(Long resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public NotificationDto build() {
      return new NotificationDto(id, createdAt, updatedAt, confirmed, userId, content, resourceType, resourceId);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}