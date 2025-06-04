package com.team1.monew.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank
    @Size(max = 20)
    String nickname
) {
  public static class Builder {
    private String nickname;

    public Builder nickname(String nickname) {
      this.nickname = nickname;
      return this;
    }

    public UserUpdateRequest build() {
      return new UserUpdateRequest(nickname);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
