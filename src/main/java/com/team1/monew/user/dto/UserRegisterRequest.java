package com.team1.monew.user.dto;

public record UserRegisterRequest (
    String email,
    String nickname,
    String password
){
  public static class Builder {
    private String email;
    private String nickname;
    private String password;

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder nickname(String nickname) {
      this.nickname = nickname;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public UserRegisterRequest build() {
      return new UserRegisterRequest(email, nickname, password);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
