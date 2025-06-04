package com.team1.monew.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 6, max = 20)
    String password
) {
  public static class Builder {
    private String email;
    private String password;

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public UserLoginRequest build() {
      return new UserLoginRequest(email, password);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
