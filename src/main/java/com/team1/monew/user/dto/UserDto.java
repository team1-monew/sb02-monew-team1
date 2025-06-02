package com.team1.monew.user.dto;

import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String email,
    String nickname,
    LocalDateTime createdAt
) {
  public static class Builder {
    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder nickname(String nickname) {
      this.nickname = nickname;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public UserDto build() {
      return new UserDto(id, email, nickname, createdAt);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
