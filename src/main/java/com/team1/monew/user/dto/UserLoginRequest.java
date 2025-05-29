package com.team1.monew.user.dto;

import jakarta.validation.constraints.Email;

public record UserLoginRequest(
    String email,
    String password
) {

}
