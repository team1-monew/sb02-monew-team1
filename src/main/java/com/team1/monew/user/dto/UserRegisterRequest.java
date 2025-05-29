package com.team1.monew.user.dto;

import jakarta.validation.constraints.Email;

public record UserRegisterRequest (
    String email,
    String nickname,
    String password
){

}
