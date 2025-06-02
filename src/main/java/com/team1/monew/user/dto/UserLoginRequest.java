package com.team1.monew.user.dto;

public record UserLoginRequest(
    String email,
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
