package com.team1.monew.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest (
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 20)
    String nickname,

    @NotBlank
    @Size(min = 6, max = 20)
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
