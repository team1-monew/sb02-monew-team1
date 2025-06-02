package com.team1.monew.user.dto;

public record UserUpdateRequest(
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
