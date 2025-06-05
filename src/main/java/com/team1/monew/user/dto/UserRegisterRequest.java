package com.team1.monew.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest (
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 20)
    String nickname,

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z0-9!@#$%^&*(),.?\":{}|<>]{6,20}$",
        message = "비밀번호는 6~20자이며 숫자와 특수문자를 포함해야 합니다."
    )
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
